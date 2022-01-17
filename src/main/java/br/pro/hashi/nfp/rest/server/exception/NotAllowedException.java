package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public abstract class NotAllowedException extends ResponseException {
	private static final long serialVersionUID = 6403408557112093146L;

	protected NotAllowedException(String method, String suffix) {
		super(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "%s %s".formatted(method.toUpperCase(), suffix));
	}
}
