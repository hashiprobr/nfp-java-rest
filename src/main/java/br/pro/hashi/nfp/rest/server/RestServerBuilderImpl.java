package br.pro.hashi.nfp.rest.server;

import org.eclipse.jetty.server.Server;

class RestServerBuilderImpl implements RestServerBuilder {
	private final String name;
	private int port;

	RestServerBuilderImpl(String name) {
		this.name = name;
		this.port = 8080;
	}

	@Override
	public RestServerBuilderImpl at(int port) {
		this.port = port;
		return this;
	}

	@Override
	public RestServer build() {
		Server server = new Server(port);
		server.setHandler(new Handler(name));
		server.setErrorHandler(new JsonErrorHandler());
		return new RestServer(port, server);
	}
}
