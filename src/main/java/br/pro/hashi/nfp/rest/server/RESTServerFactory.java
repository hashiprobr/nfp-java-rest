package br.pro.hashi.nfp.rest.server;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;

import com.google.gson.Gson;

import br.pro.hashi.nfp.rest.GsonManager;
import br.pro.hashi.nfp.rest.Reflections;

public class RESTServerFactory extends GsonManager {
	private static final int PORT = 8080;

	private final Map<String, Map<String, Endpoint<?>>> maps;

	RESTServerFactory() {
		this.maps = new HashMap<>();
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
			Reflections reflections = new Reflections(endpointPrefix);
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
		Gson gson = get(converterPrefix);
		return build(endpointPrefix, gson, port);
	}

	public RESTServer build(String endpointPrefix, String converterPrefix) {
		return build(endpointPrefix, converterPrefix, PORT);
	}

	public RESTServer build(String endpointPrefix, int port) {
		Gson gson = get();
		return build(endpointPrefix, gson, port);
	}

	public RESTServer build(String endpointPrefix) {
		return build(endpointPrefix, PORT);
	}
}
