package br.pro.hashi.nfp.rest.client.exception;

public class InterruptedSendException extends SendException {
	private static final long serialVersionUID = 2047391061807326034L;

	public InterruptedSendException(InterruptedException exception) {
		super("Client execution interrupted", exception);
	}
}
