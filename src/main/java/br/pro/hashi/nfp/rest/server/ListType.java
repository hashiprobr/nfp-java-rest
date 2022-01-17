package br.pro.hashi.nfp.rest.server;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public final class ListType implements ParameterizedType {
	private final Type[] types;

	public ListType(Type type) {
		this.types = new Type[1];
		this.types[0] = type;
	}

	@Override
	public final Type[] getActualTypeArguments() {
		return types;
	}

	@Override
	public final Type getRawType() {
		return List.class;
	}

	@Override
	public final Type getOwnerType() {
		return null;
	}
}
