package br.pro.hashi.nfp.rest.server.exception;

import jakarta.servlet.http.HttpServletResponse;

public class UnsupportedMediaTypeException extends ResponseException {
	private static final long serialVersionUID = 495318832065551798L;

	public UnsupportedMediaTypeException(String message) {
		super(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, message);
	}
}
