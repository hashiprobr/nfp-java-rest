package br.pro.hashi.nfp.rest.server.exception;

public class ServerException extends RuntimeException {
	private static final long serialVersionUID = -8882188560010653148L;

	public ServerException(Exception exception) {
		super(exception);
	}

	public ServerException(String message) {
		super(message);
	}
}
