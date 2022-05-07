package br.pro.hashi.nfp.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamRequestContent;
import org.eclipse.jetty.client.util.MultiPartRequestContent;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpFields.Mutable;
import org.eclipse.jetty.http.HttpTester;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.nfp.rest.client.exception.ClientException;
import br.pro.hashi.nfp.rest.client.exception.ExecutionSendException;
import br.pro.hashi.nfp.rest.client.exception.IOClientException;
import br.pro.hashi.nfp.rest.client.exception.InterruptedSendException;
import br.pro.hashi.nfp.rest.client.exception.TimeoutSendException;
import br.pro.hashi.nfp.rest.server.exception.ResponseException;

public abstract class EndpointTest {
	private String url;
	private int timeout;
	private Gson gson;
	private HttpClient client;

	protected final void start(String url, int timeout) {
		this.url = url;
		this.timeout = timeout;
		this.gson = new GsonBuilder()
				.serializeNulls()
				.setPrettyPrinting()
				.create();
		this.client = new HttpClient();
		try {
			this.client.start();
		} catch (Exception exception) {
			throw new ClientException(exception);
		}
	}

	protected final void start(String url) {
		start(url, 10);
	}

	private String encode(String subItem) {
		return URLEncoder.encode(subItem, StandardCharsets.UTF_8);
	}

	private Request request(String method, String uri) {
		int index = uri.indexOf("?");
		if (index != -1) {
			String args = uri.substring(index + 1);
			uri = uri.substring(0, index);
			if (!args.isBlank()) {
				String[] items = args.split("&");
				for (int i = 0; i < items.length; i++) {
					int subIndex = items[i].indexOf("=");
					if (subIndex == -1) {
						items[i] = encode(items[i]);
					} else {
						String name = items[i].substring(0, subIndex);
						String value = items[i].substring(subIndex + 1);
						items[i] = "%s=%s".formatted(encode(name), encode(value));
					}
				}
				uri = "%s?%s".formatted(uri, String.join("&", items));
			}
		}
		Request request = client.newRequest("%s%s".formatted(url, uri));
		return request.method(method).timeout(timeout, TimeUnit.SECONDS);
	}

	private String send(Request request) {
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
		int status = response.getStatus();
		String responseBody = response.getContentAsString();
		if (status != 200) {
			throw new ResponseException(status, responseBody);
		}
		return responseBody;
	}

	private String sendRequest(String method, String uri) {
		return send(request(method, uri));
	}

	private String sendRequest(String method, String uri, String requestBody) {
		Consumer<Mutable> consumer = fields -> fields.add("Content-Type", "application/json");
		Request.Content content = new StringRequestContent(requestBody);
		return send(request(method, uri).headers(consumer).body(content));
	}

	private String sendRequest(String method, String uri, String requestBody, Map<String, String> paths) {
		HttpTester.Request fields;
		MultiPartRequestContent content = new MultiPartRequestContent();
		for (String name : paths.keySet()) {
			File file = new File(paths.get(name));
			InputStream stream;
			try {
				stream = new FileInputStream(file);
			} catch (FileNotFoundException exception) {
				throw new IOClientException(exception);
			}
			fields = new HttpTester.Request();
			fields.add("Content-Type", "application/octet-stream");
			content.addFilePart(name, null, new InputStreamRequestContent(stream), fields);
		}
		fields = new HttpTester.Request();
		fields.add("Content-Type", "application/json");
		content.addFieldPart("body", new StringRequestContent(requestBody), fields);
		content.close();
		return send(request(method, uri).body(content));
	}

	protected final String toJson(Object body) {
		return gson.toJson(body);
	}

	protected final String get(String uri) {
		return sendRequest("GET", uri);
	}

	protected final String post(String uri, String requestBody, Map<String, String> paths) {
		return sendRequest("POST", uri, requestBody, paths);
	}

	protected final String post(String uri, String requestBody) {
		return sendRequest("POST", uri, requestBody);
	}

	protected final String put(String uri, String requestBody, Map<String, String> paths) {
		return sendRequest("PUT", uri, requestBody, paths);
	}

	protected final String put(String uri, String requestBody) {
		return sendRequest("PUT", uri, requestBody);
	}

	protected final String delete(String uri) {
		return sendRequest("DELETE", uri);
	}

	protected final <T> T fromJson(String responseBody, Type type) {
		return gson.fromJson(responseBody, type);
	}

	protected final <T> List<T> fromJsonList(String responseBody, Type type) {
		return gson.fromJson(responseBody, new ListType(type));
	}

	protected final void stop() {
		try {
			client.stop();
		} catch (Exception exception) {
			throw new ClientException(exception);
		}
	}
}
