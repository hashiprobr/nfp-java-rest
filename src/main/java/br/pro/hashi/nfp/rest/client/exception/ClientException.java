package br.pro.hashi.nfp.rest.client.exception;

public class ClientException extends RuntimeException {
	private static final long serialVersionUID = 1951855267298633685L;

	public ClientException(Exception exception) {
		super(exception);
	}
}
