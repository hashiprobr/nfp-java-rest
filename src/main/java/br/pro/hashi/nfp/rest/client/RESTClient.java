package br.pro.hashi.nfp.rest.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamRequestContent;
import org.eclipse.jetty.client.util.MultiPartRequestContent;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpFields.Mutable;
import org.eclipse.jetty.http.HttpTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import br.pro.hashi.nfp.rest.client.exception.ClientException;
import br.pro.hashi.nfp.rest.client.exception.ExecutionSendException;
import br.pro.hashi.nfp.rest.client.exception.FileClientException;
import br.pro.hashi.nfp.rest.client.exception.InterruptedSendException;
import br.pro.hashi.nfp.rest.client.exception.TimeoutSendException;

public class RESTClient {
	private static final RESTClientFactory FACTORY = new RESTClientFactory();

	public static RESTClientFactory factory() {
		return FACTORY;
	}

	private final Logger logger;
	private final RESTClientFactory factory;
	private final String url;
	private final Gson gson;
	private final int timeout;
	private final HttpClient client;

	RESTClient(RESTClientFactory factory, String url, Gson gson, int timeout, HttpClient client) {
		this.logger = LoggerFactory.getLogger(RESTClient.class);
		this.factory = factory;
		this.url = url;
		this.gson = gson;
		this.timeout = timeout;
		this.client = client;
	}

	public void start() {
		if (client.isRunning()) {
			return;
		}
		logger.info("Starting REST client...");
		try {
			client.start();
		} catch (Exception exception) {
			throw new ClientException(exception);
		}
		logger.info("REST client started");
	}

	public void stop() {
		if (!client.isRunning()) {
			return;
		}
		logger.info("Stopping REST client...");
		try {
			client.stop();
		} catch (Exception exception) {
			throw new ClientException(exception);
		}
		logger.info("REST client stopped");
	}

	private String encodePaths(String uri) {
		Stream<String> stream = Stream.of(uri.split("/"))
				.map((path) -> URLDecoder.decode(path, StandardCharsets.UTF_8))
				.map((path) -> URLEncoder.encode(path, StandardCharsets.UTF_8).replace("+", "%20"));
		return String.join("/", stream.toList());
	}

	private String encodeQuery(String subItem) {
		return URLEncoder.encode(URLDecoder.decode(subItem, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
	}

	private Request request(String method, String uri) {
		if (uri == null) {
			throw new IllegalArgumentException("Request URI cannot be null");
		}
		uri = uri.strip();
		if (!uri.startsWith("/")) {
			throw new IllegalArgumentException("Request URI must start with a slash");
		}
		int index = uri.indexOf('?');
		if (index == -1) {
			uri = encodePaths(uri);
		} else {
			String prefix = uri.substring(0, index);
			String suffix = uri.substring(index + 1);
			if (suffix.isEmpty()) {
				uri = encodePaths(prefix);
			} else {
				String[] items = suffix.split("&");
				for (int i = 0; i < items.length; i++) {
					String item = items[i];
					int subIndex = item.indexOf('=');
					if (subIndex == -1) {
						items[i] = encodeQuery(item);
					} else {
						String name = item.substring(0, subIndex);
						String value = item.substring(subIndex + 1);
						items[i] = "%s=%s".formatted(encodeQuery(name), encodeQuery(value));
					}
				}
				uri = "%s?%s".formatted(encodePaths(prefix), String.join("&", items));
			}
		}
		Request request = client.newRequest("%s%s".formatted(url, uri));
		return request.method(method).timeout(timeout, TimeUnit.SECONDS);
	}

	private Response send(Request request) {
		if (!client.isRunning()) {
			start();
		}
		ContentResponse response;
		try {
			response = request.send();
		} catch (ExecutionException exception) {
			throw new ExecutionSendException(exception);
		} catch (TimeoutException exception) {
			throw new TimeoutSendException(exception);
		} catch (InterruptedException exception) {
			throw new InterruptedSendException(exception);
		}
		return new Response(factory, response, gson);
	}

	private Response sendRequest(String method, String uri) {
		return send(request(method, uri));
	}

	private Response sendRequest(String method, String uri, Object body, Map<String, String> paths) {
		if (body == null) {
			throw new IllegalArgumentException("Body cannot be null");
		}
		String requestBody = gson.toJson(body);
		Response response;
		if (paths == null) {
			Consumer<Mutable> consumer = (fields) -> fields.add("Content-Type", "application/json");
			Request.Content content = new StringRequestContent(requestBody);
			response = send(request(method, uri).headers(consumer).body(content));
		} else {
			HttpTester.Request fields;
			MultiPartRequestContent content = new MultiPartRequestContent();
			for (String name : paths.keySet()) {
				String path = paths.get(name);
				InputStream stream;
				try {
					stream = new FileInputStream(path);
				} catch (FileNotFoundException exception) {
					throw new FileClientException(exception);
				}
				Path fileName = Paths.get(path).getFileName();
				fields = new HttpTester.Request();
				content.addFilePart(name, fileName.toString(), new InputStreamRequestContent(stream), fields);
			}
			fields = new HttpTester.Request();
			fields.add("Content-Type", "application/json");
			content.addFieldPart("body", new StringRequestContent(requestBody), fields);
			content.close();
			response = send(request(method, uri).body(content));
		}
		return response;
	}

	public Response head(String uri) {
		return sendRequest("HEAD", uri);
	}

	public Response get(String uri) {
		return sendRequest("GET", uri);
	}

	public Response post(String uri, Object body, Map<String, String> paths) {
		return sendRequest("POST", uri, body, paths);
	}

	public Response post(String uri, Object body) {
		return post(uri, body, null);
	}

	public Response put(String uri, Object body, Map<String, String> paths) {
		return sendRequest("PUT", uri, body, paths);
	}

	public Response put(String uri, Object body) {
		return put(uri, body, null);
	}

	public Response patch(String uri, Object body, Map<String, String> paths) {
		return sendRequest("PATCH", uri, body, paths);
	}

	public Response patch(String uri, Object body) {
		return patch(uri, body, null);
	}

	public Response delete(String uri) {
		return sendRequest("DELETE", uri);
	}

	public Response options(String uri) {
		return sendRequest("OPTIONS", uri);
	}
}
