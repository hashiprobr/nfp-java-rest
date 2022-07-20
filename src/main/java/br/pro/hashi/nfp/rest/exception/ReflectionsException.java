package br.pro.hashi.nfp.rest.exception;

public class ReflectionsException extends RuntimeException {
	private static final long serialVersionUID = 2227426281280355934L;

	public ReflectionsException(Exception exception) {
		super(exception);
	}
}
