package br.pro.hashi.nfp.rest.server.exception;

public abstract class ArgumentException extends RuntimeException {
	private static final long serialVersionUID = 3210813138574495889L;

	protected ArgumentException(String message) {
		super(message);
	}
}
