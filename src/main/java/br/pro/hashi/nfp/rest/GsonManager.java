package br.pro.hashi.nfp.rest;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonManager {
	private final Map<String, Gson> gsons;

	public GsonManager() {
		this.gsons = new HashMap<>();
	}

	private GsonBuilder builder(String converterPrefix) {
		GsonBuilder builder = new GsonBuilder();
		Reflections reflections = new Reflections(converterPrefix);
		for (AbstractConverter<?, ?> converter : reflections.getSubInstancesOf(AbstractConverter.class)) {
			Type type = converter.getType();
			builder.registerTypeAdapter(type, converter.serializer());
			builder.registerTypeAdapter(type, converter.deserializer());
		}
		return builder;
	}

	private Gson put(String converterPrefix, GsonBuilder builder) {
		Gson gson = builder
				.serializeNulls()
				.setPrettyPrinting()
				.create();
		gsons.put(converterPrefix, gson);
		return gson;
	}

	public Gson get(String converterPrefix) {
		if (converterPrefix == null) {
			throw new IllegalArgumentException("Converter prefix cannot be null");
		}
		converterPrefix = converterPrefix.strip();
		if (converterPrefix.isEmpty()) {
			throw new IllegalArgumentException("Converter prefix cannot be blank");
		}
		Gson gson = gsons.get(converterPrefix);
		if (gson == null) {
			GsonBuilder builder = builder(converterPrefix);
			gson = put(converterPrefix, builder);
		}
		return gson;
	}

	public Gson get() {
		Gson gson = gsons.get(null);
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
			gson = put(null, builder);
		}
		return gson;
	}
}
