package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class UnauthorizedException extends ResponseServerException {
	private static final long serialVersionUID = 596981786929791574L;

	public UnauthorizedException(String message) {
		super(HttpServletResponse.SC_UNAUTHORIZED, message);
	}
}
