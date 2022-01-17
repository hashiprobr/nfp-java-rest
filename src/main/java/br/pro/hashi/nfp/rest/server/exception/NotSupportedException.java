package br.pro.hashi.nfp.rest.server.exception;

public class NotSupportedException extends NotAllowedException {
	private static final long serialVersionUID = -466154788644340539L;

	public NotSupportedException(String method) {
		super(method, "not supported");
	}
}
