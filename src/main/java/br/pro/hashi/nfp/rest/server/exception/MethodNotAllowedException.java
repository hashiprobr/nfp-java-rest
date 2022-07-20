package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class MethodNotAllowedException extends ResponseServerException {
	private static final long serialVersionUID = -2190575041663940027L;

	public MethodNotAllowedException(String message) {
		super(HttpServletResponse.SC_METHOD_NOT_ALLOWED, message);
	}
}
