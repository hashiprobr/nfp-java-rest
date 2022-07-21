package br.pro.hashi.nfp.rest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public final class Converter {
	private Converter() {
	}

	private static abstract class ToNumber<J extends Number, T> extends AbstractConverter<J, T> {
		@Override
		protected final JsonElement wrapNotNull(Number value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}
	}

	private static abstract class ToCollection<C, T> extends AbstractConverter<C, T> {
		protected final Type elementType;

		protected ToCollection() {
			Class<?> type = getClass();
			Class<?> ancestor = type.getSuperclass();
			while (!ancestor.equals(ToCollection.class)) {
				type = ancestor;
				ancestor = type.getSuperclass();
			}
			ParameterizedType genericType = (ParameterizedType) type.getGenericSuperclass();
			Type[] types = genericType.getActualTypeArguments();
			genericType = (ParameterizedType) types[0];
			types = genericType.getActualTypeArguments();
			this.elementType = types[types.length - 1];
		}
	}

	public static abstract class ToBoolean<T> extends AbstractConverter<Boolean, T> {
		@Override
		protected final JsonElement wrapNotNull(Boolean value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}

		@Override
		protected final Boolean unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBoolean();
		}
	}

	public static abstract class ToByte<T> extends ToNumber<Byte, T> {
		@Override
		protected final Byte unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsByte();
		}
	}

	public static abstract class ToShort<T> extends ToNumber<Short, T> {
		@Override
		protected final Short unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsShort();
		}
	}

	public static abstract class ToInt<T> extends ToNumber<Integer, T> {
		@Override
		protected final Integer unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsInt();
		}
	}

	public static abstract class ToLong<T> extends ToNumber<Long, T> {
		@Override
		protected final Long unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsLong();
		}
	}

	public static abstract class ToFloat<T> extends ToNumber<Float, T> {
		@Override
		protected final Float unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsFloat();
		}
	}

	public static abstract class ToDouble<T> extends ToNumber<Double, T> {
		@Override
		protected final Double unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsDouble();
		}
	}

	public static abstract class ToBigInteger<T> extends ToNumber<BigInteger, T> {
		@Override
		protected final BigInteger unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBigInteger();
		}
	}

	public static abstract class ToBigDecimal<T> extends ToNumber<BigDecimal, T> {
		@Override
		protected final BigDecimal unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsBigDecimal();
		}
	}

	public static abstract class ToString<T> extends AbstractConverter<String, T> {
		@Override
		protected final JsonElement wrapNotNull(String value, JsonSerializationContext context) {
			return new JsonPrimitive(value);
		}

		@Override
		protected final String unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			return value.getAsString();
		}
	}

	public static abstract class ToList<E, T> extends ToCollection<List<E>, T> {
		@Override
		protected final JsonElement wrapNotNull(List<E> value, JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			for (E element : value) {
				array.add(context.serialize(element));
			}
			return array;
		}

		@Override
		protected final List<E> unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			List<E> list = new ArrayList<>();
			for (JsonElement element : value.getAsJsonArray()) {
				list.add(context.deserialize(element, elementType));
			}
			return list;
		}
	}

	public static abstract class ToMap<V, T> extends ToCollection<SortedMap<String, V>, T> {
		@Override
		protected final JsonElement wrapNotNull(SortedMap<String, V> value, JsonSerializationContext context) {
			JsonObject object = new JsonObject();
			for (String key : value.keySet()) {
				object.add(key, context.serialize(value.get(key)));
			}
			return object;
		}

		@Override
		protected final SortedMap<String, V> unwrapNotNull(JsonElement value, JsonDeserializationContext context) {
			JsonObject object = value.getAsJsonObject();
			SortedMap<String, V> map = new TreeMap<>();
			for (String key : object.keySet()) {
				map.put(key, context.deserialize(object.get(key), elementType));
			}
			return map;
		}
	}
}
