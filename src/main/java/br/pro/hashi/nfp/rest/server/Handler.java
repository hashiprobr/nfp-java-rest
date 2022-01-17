package br.pro.hashi.nfp.rest.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.nfp.rest.server.exception.BadRequestException;
import br.pro.hashi.nfp.rest.server.exception.IOServerException;
import br.pro.hashi.nfp.rest.server.exception.NotFoundException;
import br.pro.hashi.nfp.rest.server.exception.NotSupportedException;
import br.pro.hashi.nfp.rest.server.exception.ResponseException;
import br.pro.hashi.nfp.rest.server.exception.ServerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class Handler extends AbstractHandler {
	private final Gson gson;
	private final Map<String, Endpoint<?>> endpoints;
	private final char[] buffer;

	public Handler(String name, Logger logger) {
		super();
		this.gson = new GsonBuilder()
				.serializeNulls()
				.setPrettyPrinting()
				.create();
		this.endpoints = new HashMap<>();
		Reflections reflections = new Reflections(name);
		try {
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
		} catch (ReflectionsException exception) {
			logger.warning("Could not find subclasses of Endpoint in %s".formatted(name));
		}
		this.buffer = new char[8192];
	}

	private String read(HttpServletRequest request) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = request.getReader();
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

	@Override
	public final void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		String responseBody;
		String uri = request.getRequestURI().replaceAll("/{2,}", "/");
		int length = uri.length();
		String suffix = "";
		if (length > 4) {
			if (length == 5) {
				if (uri == "/list" || uri == "/file") {
					suffix = uri;
					uri = "/";
				}
			} else {
				if (uri.endsWith("/list") || uri.endsWith("/file")) {
					suffix = uri.substring(length - 5);
					uri = uri.substring(0, length - 5);
				}
			}
		}
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
				if (values.length < 1) {
					throw new BadRequestException("Arg %s must have a value".formatted(name));
				}
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
				switch (suffix) {
				case "/file":
					body = endpoint.getFile(args);
					break;
				case "/list":
					body = endpoint.getList(args);
					break;
				default:
					body = endpoint.get(args);
				}
				break;
			case "POST":
				switch (suffix) {
				case "/file":
					body = endpoint.postFile(args, request.getInputStream());
					break;
				case "/list":
					body = endpoint.doPostList(args, read(request));
					break;
				default:
					body = endpoint.doPost(args, read(request));
				}
				break;
			case "PUT":
				switch (suffix) {
				case "/file":
					body = endpoint.putFile(args, request.getInputStream());
					break;
				case "/list":
					body = endpoint.doPutList(args, read(request));
					break;
				default:
					body = endpoint.doPut(args, read(request));
				}
				break;
			case "DELETE":
				switch (suffix) {
				case "/file":
					body = endpoint.deleteFile(args);
					break;
				case "/list":
					body = endpoint.deleteList(args);
					break;
				default:
					body = endpoint.delete(args);
				}
				break;
			case "OPTIONS":
				body = "";
				break;
			default:
				throw new NotSupportedException(method);
			}
			response.setStatus(HttpServletResponse.SC_OK);
			if (body instanceof String) {
				response.setContentType("text/plain");
				responseBody = (String) body;
			} else {
				response.setContentType("application/json");
				responseBody = gson.toJson(body);
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
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException exception) {
			throw new IOServerException(exception);
		}
		writer.print(responseBody);
		baseRequest.setHandled(true);
	}
}
