package br.pro.hashi.nfp.rest.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;

import br.pro.hashi.nfp.rest.server.exception.ServerException;

public class RestServer {
	private final int port;
	private final Handler handler;
	private final Server server;
	private boolean running;

	public RestServer(String name, int port) {
		if (name == null) {
			throw new ServerException("Name cannot be null");
		}
		if (name.isBlank()) {
			throw new ServerException("Name cannot be blank");
		}
		this.port = port;
		this.handler = new Handler(name);
		this.server = new Server(this.port);
		this.server.setHandler(this.handler);
		this.running = false;
	}

	public RestServer(String name) {
		this(name, 8080);
	}

	public void start(boolean useTunnel) {
		if (running) {
			throw new ServerException("This REST server is already running");
		}
		running = true;

		System.out.println("Starting REST server...");

		try {
			this.server.start();
		} catch (Exception exception) {
			throw new ServerException(exception);
		}

		String url;
		if (useTunnel) {
			NgrokClient ngrokClient = new NgrokClient.Builder()
					.build();
			CreateTunnel createTunnel = new CreateTunnel.Builder()
					.withAddr(port)
					.build();
			Tunnel tunnel = ngrokClient.connect(createTunnel);
			url = tunnel.getPublicUrl();
		} else {
			String address;
			try {
				address = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException exception) {
				throw new ServerException(exception);
			}
			if (port == 80) {
				url = "http://%s".formatted(address);
			} else {
				url = "http://%s:%d".formatted(address, port);
			}
		}

		System.out.println("REST server started on %s".formatted(url));
	}

	public void start() {
		start(false);
	}

	public void stop() {
		if (!running) {
			throw new ServerException("This REST server is not running");
		}
		running = false;
		System.out.println("Stopping REST server...");
		try {
			this.server.stop();
		} catch (Exception exception) {
			throw new ServerException(exception);
		}
		System.out.println("REST server stopped");
	}
}
