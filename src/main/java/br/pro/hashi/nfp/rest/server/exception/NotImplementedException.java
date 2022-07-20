package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class NotImplementedException extends ResponseServerException {
	private static final long serialVersionUID = -6885017237325571339L;

	public NotImplementedException(String message) {
		super(HttpServletResponse.SC_NOT_IMPLEMENTED, message);
	}
}
