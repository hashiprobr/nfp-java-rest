package br.pro.hashi.nfp.rest.server.exception;

public class ResponseServerException extends ServerException {
	private static final long serialVersionUID = 7351199399053886266L;

	private final int status;

	public ResponseServerException(int status, String message) {
		super(message);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
