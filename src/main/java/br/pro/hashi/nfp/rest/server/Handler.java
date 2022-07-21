package br.pro.hashi.nfp.rest.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

import br.pro.hashi.nfp.rest.server.exception.BadRequestException;
import br.pro.hashi.nfp.rest.server.exception.MethodNotAllowedException;
import br.pro.hashi.nfp.rest.server.exception.NotFoundException;
import br.pro.hashi.nfp.rest.server.exception.ResponseServerException;
import br.pro.hashi.nfp.rest.server.exception.UnsupportedMediaTypeException;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

class Handler extends AbstractHandler {
	private final Map<String, Endpoint<?>> endpoints;
	private final Gson gson;
	private final char[] buffer;
	private final MultipartConfigElement element;
	private final Base64.Decoder decoder;

	Handler(Map<String, Endpoint<?>> endpoints, Gson gson) {
		this.endpoints = endpoints;
		this.gson = gson;
		this.buffer = new char[8192];
		this.element = new MultipartConfigElement("");
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

	private Map.Entry<String, Files> split(HttpServletRequest request) throws ServletException, IOException {
		request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, element);
		InputStream bodyStream = null;
		Files files = new Files();
		for (Part part : request.getParts()) {
			String name = part.getName();
			if (name == null) {
				throw new BadRequestException("Request part must have a name");
			}
			name = name.strip();
			if (name.isEmpty()) {
				throw new BadRequestException("Request part name cannot be blank");
			}
			String type = part.getContentType();
			if (name.equals("body")) {
				if (bodyStream == null) {
					if (type == null) {
						throw new BadRequestException("Request part named body must have a type");
					}
					type = type.strip();
					if (!type.startsWith("application/json")) {
						throw new BadRequestException("Request part named body must be application/json");
					}
					bodyStream = part.getInputStream();
				} else {
					throw new BadRequestException("Multipart request must have only one part named body");
				}
			} else {
				InputStream fileStream = part.getInputStream();
				if (type != null && type.contains(";base64")) {
					files.put(name, decoder.wrap(fileStream));
				} else {
					files.put(name, fileStream);
				}
			}
		}
		if (bodyStream == null) {
			throw new BadRequestException("Multipart request must have one part named body");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(bodyStream));
		return new AbstractMap.SimpleEntry<>(read(reader), files);
	}

	private boolean multipart(HttpServletRequest request) {
		String type = request.getContentType();
		if (type == null) {
			throw new BadRequestException("Request must have a type");
		}
		type = type.strip();
		if (type.startsWith("application/json")) {
			return false;
		}
		if (type.startsWith("multipart/form-data")) {
			return true;
		}
		throw new UnsupportedMediaTypeException("Request must be application/json or multipart/form-data");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		String key = "";
		String uri = request.getRequestURI().strip();
		int index = uri.indexOf('/', 1);
		if (index != -1) {
			key = uri.substring(index + 1).strip();
			uri = uri.substring(0, index).strip();
			while (key.endsWith("/")) {
				key = key.substring(0, key.length() - 1).strip();
			}
		}
		key = URLDecoder.decode(key, StandardCharsets.UTF_8).strip();
		uri = URLDecoder.decode(uri, StandardCharsets.UTF_8).strip();

		String responseBody;
		try {
			Endpoint<?> endpoint = endpoints.get(uri);
			if (endpoint == null) {
				throw new NotFoundException("Endpoint with URI %s does not exist".formatted(uri));
			}

			Map<String, String[]> map = request.getParameterMap();
			Args args = new Args();
			for (String name : map.keySet()) {
				String[] values = map.get(name);
				name = name.strip();
				if (name.isEmpty()) {
					throw new BadRequestException("Arg name cannot be blank");
				}
				if (values.length < 1) {
					throw new BadRequestException("Arg %s must have one value".formatted(name));
				}
				if (values.length > 1) {
					throw new BadRequestException("Arg %s must have only one value".formatted(name));
				}
				String value = values[0].strip();
				if (value.isEmpty()) {
					throw new BadRequestException("Arg %s cannot have a blank value".formatted(name));
				}
				args.put(name, value);
			}

			String method = request.getMethod();
			Object body;
			switch (method) {
			case "HEAD":
			case "GET":
				if (key.isEmpty()) {
					body = endpoint.get(args);
				} else {
					body = endpoint.get(key, args);
				}
				break;
			case "POST":
				if (key.isEmpty()) {
					if (multipart(request)) {
						Map.Entry<String, Files> pair = split(request);
						body = endpoint.doPost(pair.getKey(), pair.getValue(), args);
					} else {
						body = endpoint.doPost(read(request), args);
					}
				} else {
					throw new MethodNotAllowedException("Method POST with key not allowed");
				}
				break;
			case "PUT":
				if (key.isEmpty()) {
					if (multipart(request)) {
						Map.Entry<String, Files> pair = split(request);
						body = endpoint.doPut(pair.getKey(), pair.getValue(), args);
					} else {
						body = endpoint.doPut(read(request), args);
					}
				} else {
					throw new MethodNotAllowedException("Method PUT with key not allowed");
				}
				break;
			case "PATCH":
				if (key.isEmpty()) {
					if (multipart(request)) {
						Map.Entry<String, Files> pair = split(request);
						body = endpoint.doPatch(pair.getKey(), pair.getValue(), args);
					} else {
						body = endpoint.doPatch(read(request), args);
					}
				} else {
					throw new MethodNotAllowedException("Method PATCH with key not allowed");
				}
				break;
			case "DELETE":
				if (key.isEmpty()) {
					body = endpoint.delete(args);
				} else {
					body = endpoint.delete(key, args);
				}
				break;
			case "OPTIONS":
				body = null;
				break;
			default:
				throw new MethodNotAllowedException("Method %s not allowed".formatted(method));
			}
			if (method.equals("POST")) {
				response.setStatus(HttpServletResponse.SC_CREATED);
			} else {
				if (method.equals("OPTIONS") || body != null) {
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				}
			}
			if (body == null) {
				if (method.equals("HEAD")) {
					response.setContentLength(0);
				}
				responseBody = null;
			} else {
				if (body instanceof String) {
					response.setContentType("text/plain");
					responseBody = (String) body;
				} else {
					response.setContentType("application/json");
					responseBody = gson.toJson(body);
				}
				if (method.equals("HEAD")) {
					response.setContentLength(responseBody.getBytes().length);
					responseBody = null;
				}
			}
		} catch (ResponseServerException exception) {
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
			try {
				PrintWriter writer = response.getWriter();
				writer.print(responseBody);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		baseRequest.setHandled(true);
	}
}
