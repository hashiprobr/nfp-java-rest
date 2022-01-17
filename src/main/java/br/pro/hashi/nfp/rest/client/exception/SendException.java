package br.pro.hashi.nfp.rest.client.exception;

public abstract class SendException extends RuntimeException {
	private static final long serialVersionUID = 6337487369867484042L;

	protected SendException(String prefix, Exception exception) {
		super("%s: %s".formatted(prefix, exception.getMessage()));
	}
}
