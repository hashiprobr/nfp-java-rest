package br.pro.hashi.nfp.rest;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;

public class GsonFactory {
	public GsonBuilder build(String converterPrefix) {
		GsonBuilder builder = new GsonBuilder();
		SimpleReflections reflections = new SimpleReflections(converterPrefix);
		for (AbstractConverter<?, ?> converter : reflections.getSubInstancesOf(AbstractConverter.class)) {
			Type type = converter.getType();
			builder.registerTypeAdapter(type, converter.serializer());
			builder.registerTypeAdapter(type, converter.deserializer());
		}
		return builder;
	}
}
