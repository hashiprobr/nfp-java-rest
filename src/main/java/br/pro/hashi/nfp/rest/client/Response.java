package br.pro.hashi.nfp.rest.client;

import java.lang.reflect.Type;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.jetty.client.api.ContentResponse;

import com.google.gson.Gson;

public class Response {
	private final RESTClientFactory factory;
	private final int status;
	private final String type;
	private final String body;
	private final Gson gson;

	Response(RESTClientFactory factory, ContentResponse response, Gson gson) {
		this.factory = factory;
		this.status = response.getStatus();
		this.type = response.getMediaType();
		this.body = response.getContentAsString();
		this.gson = gson;
	}

	public int getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public String getBody() {
		return body;
	}

	public <T> T fromJson(Type type) {
		return gson.fromJson(body, type);
	}

	public <T> List<T> fromJsonArray(Type type) {
		return fromJson(factory.getListTypeOf(type));
	}

	public <T> SortedMap<String, T> fromJsonObject(Type type) {
		return fromJson(factory.getMapTypeOf(type));
	}
}
