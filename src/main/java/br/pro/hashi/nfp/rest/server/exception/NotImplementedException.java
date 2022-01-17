package br.pro.hashi.nfp.rest.server.exception;

public class NotImplementedException extends NotAllowedException {
	private static final long serialVersionUID = -6885017237325571339L;

	public NotImplementedException(String method) {
		super(method, "not implemented");
	}
}
