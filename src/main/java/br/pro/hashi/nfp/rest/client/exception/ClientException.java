package br.pro.hashi.nfp.rest.client.exception;

public class ClientException extends RuntimeException {
	private static final long serialVersionUID = -6371785626435515382L;

	public ClientException(Exception exception) {
		super(exception);
	}

	public ClientException(String message) {
		super(message);
	}
}
