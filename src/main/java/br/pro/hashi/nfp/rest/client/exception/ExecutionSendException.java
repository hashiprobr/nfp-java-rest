package br.pro.hashi.nfp.rest.client.exception;

import java.util.concurrent.ExecutionException;

public class ExecutionSendException extends SendException {
	private static final long serialVersionUID = 5807135258415333047L;

	public ExecutionSendException(ExecutionException exception) {
		super("Client execution failed", exception);
	}
}
