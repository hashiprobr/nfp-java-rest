package br.pro.hashi.nfp.rest.server;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.nfp.rest.GsonFactory;
import br.pro.hashi.nfp.rest.SimpleReflections;

public class RESTServerFactory {
	private static final int PORT = 8080;

	private final Map<String, Map<String, Endpoint<?>>> maps;
	private final Map<String, Gson> gsons;
	private final GsonFactory factory;

	public RESTServerFactory() {
		this.maps = new HashMap<>();
		this.gsons = new HashMap<>();
		this.factory = new GsonFactory();
	}

	private Gson put(String converterPrefix, GsonBuilder builder) {
		Gson gson = builder
				.serializeNulls()
				.setPrettyPrinting()
				.create();
		gsons.put(converterPrefix, gson);
		return gson;
	}

	public RESTServer build(String endpointPrefix, Gson gson, int port) {
		if (endpointPrefix == null) {
			throw new IllegalArgumentException("Endpoint prefix cannot be null");
		}
		endpointPrefix = endpointPrefix.strip();
		if (endpointPrefix.isEmpty()) {
			throw new IllegalArgumentException("Endpoint prefix cannot be blank");
		}

		Map<String, Endpoint<?>> endpoints = maps.get(endpointPrefix);
		if (endpoints == null) {
			endpoints = new HashMap<>();
			SimpleReflections reflections = new SimpleReflections(endpointPrefix);
			for (Endpoint<?> endpoint : reflections.getSubInstancesOf(Endpoint.class)) {
				String uri = endpoint.getUri();
				if (endpoints.containsKey(uri)) {
					throw new IllegalArgumentException("Multiple endpoints with URI %s".formatted(uri));
				}
				endpoint.setGson(gson);
				endpoints.put(uri, endpoint);
			}
			maps.put(endpointPrefix, endpoints);
		}

		Server server = new Server(port);
		server.setHandler(new Handler(endpoints, gson));
		server.setErrorHandler(new JsonErrorHandler());
		return new RESTServer(port, server);
	}

	public RESTServer build(String endpointPrefix, Gson gson) {
		return build(endpointPrefix, gson, PORT);
	}

	public RESTServer build(String endpointPrefix, String converterPrefix, int port) {
		if (converterPrefix == null) {
			throw new IllegalArgumentException("Converter prefix cannot be null");
		}
		converterPrefix = converterPrefix.strip();
		if (converterPrefix.isEmpty()) {
			throw new IllegalArgumentException("Converter prefix cannot be blank");
		}
		Gson gson = gsons.get(converterPrefix);
		if (gson == null) {
			GsonBuilder builder = factory.build(converterPrefix);
			gson = put(converterPrefix, builder);
		}
		return build(endpointPrefix, gson, port);
	}

	public RESTServer build(String endpointPrefix, String converterPrefix) {
		return build(endpointPrefix, converterPrefix, PORT);
	}

	public RESTServer build(String endpointPrefix, int port) {
		Gson gson = gsons.get(null);
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
			gson = put(null, builder);
		}
		return build(endpointPrefix, gson, port);
	}

	public RESTServer build(String endpointPrefix) {
		return build(endpointPrefix, PORT);
	}
}
