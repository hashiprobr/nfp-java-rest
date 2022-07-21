package br.pro.hashi.nfp.rest.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.SortedMap;

class MapType implements ParameterizedType {
	private final Type[] types;

	MapType(Type type) {
		this.types = new Type[2];
		this.types[0] = String.class;
		this.types[1] = type;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return types;
	}

	@Override
	public Type getRawType() {
		return SortedMap.class;
	}

	@Override
	public Type getOwnerType() {
		return null;
	}
}
