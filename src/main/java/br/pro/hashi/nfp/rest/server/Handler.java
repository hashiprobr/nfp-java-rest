package br.pro.hashi.nfp.rest.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.nfp.rest.server.exception.BadRequestException;
import br.pro.hashi.nfp.rest.server.exception.IOServerException;
import br.pro.hashi.nfp.rest.server.exception.NotFoundException;
import br.pro.hashi.nfp.rest.server.exception.NotSupportedException;
import br.pro.hashi.nfp.rest.server.exception.ResponseException;
import br.pro.hashi.nfp.rest.server.exception.ServerException;
import br.pro.hashi.nfp.rest.server.exception.UnsupportedMediaTypeException;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

class Handler extends AbstractHandler {
	private final Gson gson;
	private final Map<String, Endpoint<?>> endpoints;
	private final char[] buffer;
	private MultipartConfigElement element;
	private Base64.Decoder decoder;

	Handler(String name) {
		super();
		if (name == null) {
			throw new ServerException("Name cannot be null");
		}
		if (name.isBlank()) {
			throw new ServerException("Name cannot be blank");
		}
		Reflections reflections = new Reflections(name);
		this.gson = new GsonBuilder()
				.serializeNulls()
				.setPrettyPrinting()
				.create();
		this.endpoints = new HashMap<>();
		for (Class<?> type : reflections.getSubTypesOf(Endpoint.class)) {
			Constructor<?> constructor;
			try {
				constructor = type.getConstructor();
			} catch (NoSuchMethodException exception) {
				throw new ServerException("Class %s must have a public no-argument constructor".formatted(type.getName()));
			}
			Endpoint<?> endpoint;
			try {
				endpoint = (Endpoint<?>) constructor.newInstance();
			} catch (InvocationTargetException exception) {
				throw new ServerException(exception);
			} catch (IllegalAccessException exception) {
				throw new ServerException(exception);
			} catch (InstantiationException exception) {
				throw new ServerException(exception);
			}
			endpoint.setGson(this.gson);
			this.endpoints.put(endpoint.getUri(), endpoint);
		}
		this.buffer = new char[8192];
		this.element = new MultipartConfigElement("target");
		this.decoder = Base64.getDecoder();
	}

	private String read(BufferedReader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append('\n');
		}
		int length;
		while ((length = reader.read(buffer, 0, buffer.length)) != -1) {
			builder.append(buffer, 0, length);
		}
		return builder.toString();
	}

	private String read(HttpServletRequest request) throws IOException {
		BufferedReader reader = request.getReader();
		return read(reader);
	}

	private boolean multipart(HttpServletRequest request) {
		String type = request.getContentType();
		if (type == null) {
			throw new BadRequestException("Request must have a type");
		}
		if (type.startsWith("application/json")) {
			return false;
		} else if (type.startsWith("multipart/form-data")) {
			return true;
		} else {
			throw new UnsupportedMediaTypeException("Request must be application/json or multipart/form-data");
		}
	}

	private AbstractMap.SimpleEntry<String, Files> split(HttpServletRequest request) throws ServletException, IOException {
		InputStream keyStream = null;
		Files files = new Files();
		request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, element);
		for (Part part : request.getParts()) {
			String type = part.getContentType();
			if (type == null) {
				throw new BadRequestException("Multipart must have types");
			}
			if (type.startsWith("application/json")) {
				if (keyStream == null) {
					keyStream = part.getInputStream();
				} else {
					throw new BadRequestException("Multipart must have only one application/json");
				}
			} else if (type.startsWith("application/octet-stream")) {
				String name = part.getName();
				InputStream fileStream = part.getInputStream();
				if (type.contains("base64")) {
					files.put(name, decoder.wrap(fileStream));
				} else {
					files.put(name, fileStream);
				}
			} else {
				throw new UnsupportedMediaTypeException("Multipart must have only application/json and application/octet-stream");
			}
		}
		if (keyStream == null) {
			throw new BadRequestException("Multipart must have one application/json");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(keyStream));
		return new AbstractMap.SimpleEntry<>(read(reader), files);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		String uri = request.getRequestURI();
		boolean list = uri.endsWith("/list");
		if (list) {
			int length = uri.length();
			if (length == 5) {
				uri = "/";
			} else {
				uri = uri.substring(0, length - 5);
			}
		}
		String responseBody;
		try {
			Endpoint<?> endpoint;
			if (endpoints.containsKey(uri)) {
				endpoint = endpoints.get(uri);
			} else {
				throw new NotFoundException("Endpoint %s does not exist".formatted(uri));
			}
			Map<String, String[]> map = request.getParameterMap();
			Args args = new Args();
			for (String name : map.keySet()) {
				if (name.isBlank()) {
					throw new BadRequestException("Arg name cannot be blank");
				}
				String[] values = map.get(name);
				if (values.length > 1) {
					throw new BadRequestException("Arg %s must have a single value".formatted(name));
				}
				String value = values[0];
				if (value.isBlank()) {
					throw new BadRequestException("Arg %s cannot have a blank value".formatted(name));
				}
				args.put(name, value);
			}
			String method = request.getMethod();
			Object body;
			switch (method) {
			case "GET":
				if (list) {
					body = endpoint.getList(args);
				} else {
					body = endpoint.get(args);
				}
				break;
			case "POST":
				if (list) {
					throw new NotSupportedException("post");
				} else {
					if (multipart(request)) {
						AbstractMap.SimpleEntry<String, Files> pair = split(request);
						body = endpoint.doPost(args, pair.getKey(), pair.getValue());
					} else {
						body = endpoint.doPost(args, read(request));
					}
				}
				break;
			case "PUT":
				if (list) {
					throw new NotSupportedException("put");
				} else {
					if (multipart(request)) {
						AbstractMap.SimpleEntry<String, Files> pair = split(request);
						body = endpoint.doPut(args, pair.getKey(), pair.getValue());
					} else {
						body = endpoint.doPut(args, read(request));
					}
				}
				break;
			case "DELETE":
				if (list) {
					body = endpoint.deleteList(args);
				} else {
					body = endpoint.delete(args);
				}
				break;
			case "OPTIONS":
				body = null;
				break;
			default:
				throw new NotSupportedException(method);
			}
			if (body == null) {
				if (method.equals("OPTIONS")) {
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				}
				responseBody = null;
			} else {
				if (method.equals("POST")) {
					response.setStatus(HttpServletResponse.SC_CREATED);
				} else {
					response.setStatus(HttpServletResponse.SC_OK);
				}
				if (body instanceof String) {
					response.setContentType("text/plain");
					responseBody = (String) body;
				} else {
					response.setContentType("application/json");
					responseBody = gson.toJson(body);
				}
			}
		} catch (ResponseException exception) {
			response.setStatus(exception.getStatus());
			response.setContentType("text/plain");
			responseBody = exception.getMessage();
		} catch (Exception exception) {
			exception.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("text/plain");
			responseBody = "Internal server error";
		}
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "*");
		response.addHeader("Access-Control-Allow-Headers", "*");
		if (responseBody != null) {
			PrintWriter writer;
			try {
				writer = response.getWriter();
			} catch (IOException exception) {
				throw new IOServerException(exception);
			}
			writer.print(responseBody);
		}
		baseRequest.setHandled(true);
	}
}
