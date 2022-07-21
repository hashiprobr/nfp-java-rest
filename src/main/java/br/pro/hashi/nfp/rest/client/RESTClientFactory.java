package br.pro.hashi.nfp.rest.client;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;

import com.google.gson.Gson;

import br.pro.hashi.nfp.rest.GsonManager;

public class RESTClientFactory extends GsonManager {
	private static final int TIMEOUT = 10;

	private final Map<Type, ListType> listTypes;
	private final Map<Type, MapType> mapTypes;

	RESTClientFactory() {
		this.listTypes = new HashMap<>();
		this.mapTypes = new HashMap<>();
	}

	ListType getListTypeOf(Type type) {
		ListType listType = listTypes.get(type);
		if (listType == null) {
			listType = new ListType(type);
			listTypes.put(type, listType);
		}
		return listType;
	}

	MapType getMapTypeOf(Type type) {
		MapType mapType = mapTypes.get(type);
		if (mapType == null) {
			mapType = new MapType(type);
			mapTypes.put(type, mapType);
		}
		return mapType;
	}

	public RESTClient build(String url, Gson gson, int timeout) {
		if (url == null) {
			throw new IllegalArgumentException("Request URL cannot be null");
		}
		url = url.strip();
		if (url.isEmpty()) {
			throw new IllegalArgumentException("Request URL cannot be blank");
		}
		HttpClient client = new HttpClient();
		return new RESTClient(this, url, gson, timeout, client);
	}

	public RESTClient build(String url, Gson gson) {
		return build(url, gson, TIMEOUT);
	}

	public RESTClient build(String url, String converterPrefix, int timeout) {
		Gson gson = get(converterPrefix);
		return build(url, gson, timeout);
	}

	public RESTClient build(String url, String converterPrefix) {
		return build(url, converterPrefix, TIMEOUT);
	}

	public RESTClient build(String url, int timeout) {
		Gson gson = get();
		return build(url, gson, timeout);
	}

	public RESTClient build(String url) {
		return build(url, TIMEOUT);
	}
}
