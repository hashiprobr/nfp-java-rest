package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class BadRequestException extends ResponseException {
	private static final long serialVersionUID = -2381191863286236154L;

	public BadRequestException(String message) {
		super(HttpServletResponse.SC_BAD_REQUEST, message);
	}
}
