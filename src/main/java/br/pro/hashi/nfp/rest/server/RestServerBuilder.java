package br.pro.hashi.nfp.rest.server;

public interface RestServerBuilder {
	RestServerBuilder at(int port);

	RestServer build();
}
