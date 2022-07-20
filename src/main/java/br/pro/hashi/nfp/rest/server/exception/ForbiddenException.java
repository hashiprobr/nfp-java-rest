package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class ForbiddenException extends ResponseServerException {
	private static final long serialVersionUID = -4670590499506598051L;

	public ForbiddenException(String message) {
		super(HttpServletResponse.SC_FORBIDDEN, message);
	}
}
