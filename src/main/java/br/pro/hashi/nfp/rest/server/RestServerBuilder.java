package br.pro.hashi.nfp.rest.server;

import org.eclipse.jetty.server.Server;

public class RestServerBuilder {
	private final String name;
	private int port;

	RestServerBuilder(String name) {
		this.name = name;
		this.port = 8080;
	}

	public RestServerBuilder withPort(int port) {
		this.port = port;
		return this;
	}

	public RestServer build() {
		Server server = new Server(port);
		server.setHandler(new Handler(name));
		server.setErrorHandler(new JsonErrorHandler());
		return new RestServer(port, server);
	}
}
