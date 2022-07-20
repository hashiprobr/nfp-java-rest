package br.pro.hashi.nfp.rest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

abstract class AbstractConverter<J, T> {
	private final Type type;

	protected AbstractConverter() {
		Class<?> type = getClass();
		Class<?> ancestor = type.getSuperclass();
		while (!ancestor.equals(AbstractConverter.class)) {
			type = ancestor;
			ancestor = type.getSuperclass();
		}
		ParameterizedType genericType = (ParameterizedType) type.getGenericSuperclass();
		Type[] types = genericType.getActualTypeArguments();
		this.type = types[1];
	}

	private JsonElement wrap(J value, JsonSerializationContext context) {
		if (value == null) {
			return JsonNull.INSTANCE;
		}
		return wrapNotNull(value, context);
	}

	private J unwrap(JsonElement value, JsonDeserializationContext context) {
		if (value == JsonNull.INSTANCE) {
			return null;
		}
		return unwrapNotNull(value, context);
	}

	protected abstract JsonElement wrapNotNull(J value, JsonSerializationContext context);

	protected abstract J unwrapNotNull(JsonElement value, JsonDeserializationContext context);

	Type getType() {
		return type;
	}

	JsonSerializer<T> serializer() {
		return new JsonSerializer<T>() {
			@Override
			public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
				return wrap(AbstractConverter.this.serialize(src), context);
			}
		};
	}

	JsonDeserializer<T> deserializer() {
		return new JsonDeserializer<T>() {
			@Override
			public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
				return AbstractConverter.this.deserialize(unwrap(json, context));
			}
		};
	}

	public abstract J serialize(T value);

	public abstract T deserialize(J value);
}
