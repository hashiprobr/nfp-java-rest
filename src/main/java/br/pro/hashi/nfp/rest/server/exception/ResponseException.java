package br.pro.hashi.nfp.rest.server.exception;

public class ResponseException extends RuntimeException {
	private static final long serialVersionUID = 5513926761601351962L;

	private final int status;

	public ResponseException(int status, String message) {
		super(message);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
