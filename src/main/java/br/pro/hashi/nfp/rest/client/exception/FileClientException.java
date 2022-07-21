package br.pro.hashi.nfp.rest.client.exception;

import java.io.IOException;

public class FileClientException extends ClientException {
	private static final long serialVersionUID = -6971752868485974924L;

	public FileClientException(IOException exception) {
		super(exception);
	}
}
