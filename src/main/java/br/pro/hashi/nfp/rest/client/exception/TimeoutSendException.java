package br.pro.hashi.nfp.rest.client.exception;

import java.util.concurrent.TimeoutException;

public class TimeoutSendException extends SendException {
	private static final long serialVersionUID = -8423299703291965148L;

	public TimeoutSendException(TimeoutException exception) {
		super("Client timed out", exception);
	}
}
