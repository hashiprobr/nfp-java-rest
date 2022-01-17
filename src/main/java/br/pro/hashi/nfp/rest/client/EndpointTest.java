package br.pro.hashi.nfp.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamRequestContent;
import org.eclipse.jetty.client.util.StringRequestContent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.nfp.rest.client.exception.ClientException;
import br.pro.hashi.nfp.rest.client.exception.ExecutionSendException;
import br.pro.hashi.nfp.rest.client.exception.IOClientException;
import br.pro.hashi.nfp.rest.client.exception.InterruptedSendException;
import br.pro.hashi.nfp.rest.client.exception.TimeoutSendException;
import br.pro.hashi.nfp.rest.server.ListType;
import br.pro.hashi.nfp.rest.server.exception.ResponseException;

public abstract class EndpointTest<T> {
	private String url;
	private Class<T> type;
	private ListType listType;
	private int timeout;
	private Gson gson;
	private HttpClient client;

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
						String name = items[i].substring(0, index);
						String value = items[i].substring(index + 1);
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
		Request.Content content = new StringRequestContent(requestBody);
		return send(request(method, uri).body(content));
	}

	private String sendRequest(String method, String uri, File file) {
		InputStream stream;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException exception) {
			throw new IOClientException(exception);
		}
		Request.Content content = new InputStreamRequestContent(stream);
		return send(request(method, uri).body(content));
	}

	@SuppressWarnings("unchecked")
	protected final void start(String url, int timeout) {
		this.url = url;

		ParameterizedType genericType = (ParameterizedType) getClass().getGenericSuperclass();
		Type[] types = genericType.getActualTypeArguments();
		this.type = (Class<T>) types[0];

		this.listType = new ListType(this.type);

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

	protected final String toJson(Object body) {
		return gson.toJson(body);
	}

	protected final String get(String uri) {
		return sendRequest("GET", uri);
	}

	protected final String post(String uri, String requestBody) {
		return sendRequest("POST", uri, requestBody);
	}

	protected final String put(String uri, String requestBody) {
		return sendRequest("PUT", uri, requestBody);
	}

	protected final String delete(String uri) {
		return sendRequest("DELETE", uri);
	}

	protected final String uploadPost(String uri, String path) {
		return sendRequest("POST", uri, new File(path));
	}

	protected final String uploadPut(String uri, String path) {
		return sendRequest("PUT", uri, new File(path));
	}

	protected final T fromJson(String responseBody) {
		return gson.fromJson(responseBody, type);
	}

	protected final List<T> listFromJson(String responseBody) {
		return gson.fromJson(responseBody, listType);
	}

	protected final <S> S fromJson(String responseBody, Type type) {
		return gson.fromJson(responseBody, type);
	}

	protected final void stop() {
		try {
			this.client.stop();
		} catch (Exception exception) {
			throw new ClientException(exception);
		}
	}
}
