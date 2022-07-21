package br.pro.hashi.nfp.rest.server;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import br.pro.hashi.nfp.rest.server.exception.BadRequestException;
import br.pro.hashi.nfp.rest.server.exception.NotImplementedException;

public abstract class Endpoint<T> {
	private final String uri;
	private final Type type;
	private final boolean plain;
	private Gson gson;

	protected Endpoint(String uri) {
		if (uri == null) {
			throw new IllegalArgumentException("Endpoint URI cannot be null");
		}
		uri = uri.strip();
		if (!uri.startsWith("/")) {
			throw new IllegalArgumentException("Endpoint URI must start with a slash");
		}
		if (uri.indexOf('/', 1) != -1) {
			throw new IllegalArgumentException("Endpoint URI must have only one slash");
		}
		this.uri = uri;

		Class<?> type = getClass();
		Class<?> ancestor = type.getSuperclass();
		while (!ancestor.equals(Endpoint.class)) {
			type = ancestor;
			ancestor = type.getSuperclass();
		}
		ParameterizedType genericType = (ParameterizedType) type.getGenericSuperclass();
		Type[] types = genericType.getActualTypeArguments();
		this.type = types[0];

		this.plain = this.type.equals(String.class);

		this.gson = null;
	}

	@SuppressWarnings("unchecked")
	private <S> S fromJson(String method, String requestBody, Type requestType) {
		S body;
		if (plain && !method.equals("PATCH")) {
			body = (S) requestBody;
		} else {
			try {
				body = gson.fromJson(requestBody, requestType);
			} catch (JsonSyntaxException exception) {
				throw new BadRequestException("Invalid %s body: %s".formatted(method, exception.getMessage()));
			}
			if (body == null) {
				throw new BadRequestException("%s must have a body".formatted(method));
			}
		}
		return body;
	}

	protected Object get(String key, Args args) {
		return get(key);
	}

	protected Object get(String key) {
		return "GET with key %s received".formatted(key);
	}

	protected Object get(Args args) {
		return get();
	}

	protected Object get() {
		return "GET received";
	}

	protected Object post(T body, Files files, Args args) {
		return post(body, files);
	}

	protected Object post(T body, Files files) {
		throw new NotImplementedException("Method POST with files not implemented");
	}

	protected Object post(T body, Args args) {
		return post(body);
	}

	protected Object post(T body) {
		throw new NotImplementedException("Method POST not implemented");
	}

	protected Object put(T body, Files files, Args args) {
		return put(body, files);
	}

	protected Object put(T body, Files files) {
		throw new NotImplementedException("Method PUT with files not implemented");
	}

	protected Object put(T body, Args args) {
		return put(body);
	}

	protected Object put(T body) {
		throw new NotImplementedException("Method PUT not implemented");
	}

	protected Object patch(Fields fields, Files files, Args args) {
		return patch(fields, files);
	}

	protected Object patch(Fields fields, Files files) {
		throw new NotImplementedException("Method PATCH with files not implemented");
	}

	protected Object patch(Fields fields, Args args) {
		return patch(fields);
	}

	protected Object patch(Fields fields) {
		throw new NotImplementedException("Method PATCH not implemented");
	}

	protected Object delete(String key, Args args) {
		return delete(key);
	}

	protected Object delete(String key) {
		throw new NotImplementedException("Method DELETE with key not implemented");
	}

	protected Object delete(Args args) {
		return delete();
	}

	protected Object delete() {
		throw new NotImplementedException("Method DELETE not implemented");
	}

	String getUri() {
		return uri;
	}

	void setGson(Gson gson) {
		this.gson = gson;
	}

	Object doPost(String requestBody, Files files, Args args) {
		T body = fromJson("POST", requestBody, type);
		if (files == null) {
			return post(body, args);
		} else {
			return post(body, files, args);
		}
	}

	Object doPost(String requestBody, Args args) {
		return doPost(requestBody, null, args);
	}

	Object doPut(String requestBody, Files files, Args args) {
		T body = fromJson("PUT", requestBody, type);
		if (files == null) {
			return put(body, args);
		} else {
			return put(body, files, args);
		}
	}

	Object doPut(String requestBody, Args args) {
		return doPut(requestBody, null, args);
	}

	Object doPatch(String requestBody, Files files, Args args) {
		Fields body = fromJson("PATCH", requestBody, Fields.class);
		if (files == null) {
			return patch(body, args);
		} else {
			return patch(body, files, args);
		}
	}

	Object doPatch(String requestBody, Args args) {
		return doPatch(requestBody, null, args);
	}
}
