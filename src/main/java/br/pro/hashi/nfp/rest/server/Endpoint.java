package br.pro.hashi.nfp.rest.server;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import br.pro.hashi.nfp.rest.server.exception.BadRequestException;
import br.pro.hashi.nfp.rest.server.exception.NotImplementedException;
import br.pro.hashi.nfp.rest.server.exception.ServerException;

public abstract class Endpoint<T> {
	private final String uri;
	private final Type type;
	private final boolean plain;
	private Gson gson;

	protected Endpoint(String uri) {
		if (uri == null) {
			throw new ServerException("URI cannot be null");
		}
		if (!uri.startsWith("/")) {
			throw new ServerException("URI must start with a slash");
		}
		if (uri.indexOf("/", 1) != -1) {
			throw new ServerException("URI cannot have more than one slash");
		}
		this.uri = uri;

		ParameterizedType genericType = (ParameterizedType) getClass().getGenericSuperclass();
		Type[] types = genericType.getActualTypeArguments();
		this.type = types[0];

		this.plain = this.type == String.class;

		this.gson = null;
	}

	@SuppressWarnings("unchecked")
	private <S> S fromJson(String method, String requestBody) {
		S body;
		if (plain) {
			body = (S) requestBody;
		} else {
			try {
				body = gson.fromJson(requestBody, type);
			} catch (JsonSyntaxException exception) {
				throw new BadRequestException("Invalid %s body: %s".formatted(method, exception.getMessage()));
			}
			if (body == null) {
				throw new BadRequestException("%s must have a body".formatted(method));
			}
		}
		return body;
	}

	String getUri() {
		return uri;
	}

	void setGson(Gson gson) {
		this.gson = gson;
	}

	Object doPost(Args args, String requestBody, HashMap<String, InputStream> streams) {
		T body = fromJson("POST", requestBody);
		return post(args, body, streams);
	}

	Object doPost(Args args, String requestBody) {
		T body = fromJson("POST", requestBody);
		return post(args, body);
	}

	Object doPut(Args args, String requestBody, HashMap<String, InputStream> streams) {
		T body = fromJson("PUT", requestBody);
		return put(args, body, streams);
	}

	Object doPut(Args args, String requestBody) {
		T body = fromJson("PUT", requestBody);
		return put(args, body);
	}

	protected T get(Args args) {
		throw new NotImplementedException("get");
	}

	protected List<T> getList(Args args) {
		throw new NotImplementedException("get");
	}

	protected Object post(Args args, T body, HashMap<String, InputStream> streams) {
		throw new NotImplementedException("post");
	}

	protected Object post(Args args, T body) {
		throw new NotImplementedException("post");
	}

	protected Object put(Args args, T body, HashMap<String, InputStream> streams) {
		throw new NotImplementedException("put");
	}

	protected Object put(Args args, T body) {
		throw new NotImplementedException("put");
	}

	protected Object delete(Args args) {
		throw new NotImplementedException("delete");
	}

	protected Object deleteList(Args args) {
		throw new NotImplementedException("delete");
	}
}
