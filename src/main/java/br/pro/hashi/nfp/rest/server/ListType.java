package br.pro.hashi.nfp.rest.server;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ListType implements ParameterizedType {
	private final Type[] types;

	public ListType(Type type) {
		this.types = new Type[1];
		this.types[0] = type;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return types;
	}

	@Override
	public Type getRawType() {
		return List.class;
	}

	@Override
	public Type getOwnerType() {
		return null;
	}
}
