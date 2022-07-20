package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class NotFoundException extends ResponseServerException {
	private static final long serialVersionUID = -8883150847058021772L;

	public NotFoundException(String message) {
		super(HttpServletResponse.SC_NOT_FOUND, message);
	}
}
