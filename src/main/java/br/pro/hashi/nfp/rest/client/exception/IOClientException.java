package br.pro.hashi.nfp.rest.client.exception;

import java.io.IOException;

public class IOClientException extends ClientException {
	private static final long serialVersionUID = 2572977733826563811L;

	public IOClientException(IOException exception) {
		super(exception);
	}
}
