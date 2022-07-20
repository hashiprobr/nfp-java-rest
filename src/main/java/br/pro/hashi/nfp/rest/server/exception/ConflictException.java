package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class ConflictException extends ResponseServerException {
	private static final long serialVersionUID = 4995831025823880390L;

	public ConflictException(String message) {
		super(HttpServletResponse.SC_CONFLICT, message);
	}
}
