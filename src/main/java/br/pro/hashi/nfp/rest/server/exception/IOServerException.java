package br.pro.hashi.nfp.rest.server.exception;

import java.io.IOException;

public class IOServerException extends ServerException {
	private static final long serialVersionUID = 3981219369285441986L;

	public IOServerException(IOException exception) {
		super(exception);
	}
}
