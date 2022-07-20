package br.pro.hashi.nfp.rest.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;

import br.pro.hashi.nfp.rest.server.exception.ServerException;

public class RESTServer {
	private static final RESTServerFactory FACTORY = new RESTServerFactory();

	public static RESTServerFactory factory() {
		return FACTORY;
	}

	private final Logger logger;
	private final int port;
	private final Server server;
	private String url;

	RESTServer(int port, Server server) {
		this.logger = LoggerFactory.getLogger(RESTServer.class);
		this.port = port;
		this.server = server;
		this.url = null;
	}

	public String getUrl() {
		return url;
	}

	public void start(boolean useTunnel) {
		if (server.isRunning()) {
			return;
		}
		logger.info("Starting REST server...");
		try {
			server.start();
		} catch (Exception exception) {
			throw new ServerException(exception);
		}
		if (useTunnel) {
			NgrokClient client = new NgrokClient.Builder()
					.build();
			CreateTunnel create = new CreateTunnel.Builder()
					.withAddr(port)
					.build();
			Tunnel tunnel = client.connect(create);
			url = tunnel.getPublicUrl();
		} else {
			InetAddress address;
			try {
				address = InetAddress.getLocalHost();
			} catch (UnknownHostException exception) {
				throw new ServerException(exception);
			}
			String host = address.getHostAddress();
			url = "http://%s:%d".formatted(host, port);
		}
		logger.info("REST server started");
	}

	public void start() {
		start(false);
	}

	public void stop() {
		if (!server.isRunning()) {
			return;
		}
		logger.info("Stopping REST server...");
		url = null;
		try {
			server.stop();
		} catch (Exception exception) {
			throw new ServerException(exception);
		}
		logger.info("REST server stopped");
	}
}
