package br.pro.hashi.nfp.rest.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.BufferUtil;

import br.pro.hashi.nfp.rest.server.exception.IOServerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class JsonErrorHandler extends ErrorHandler {
	@Override
	public final void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		String message = (String) request.getAttribute(Dispatcher.ERROR_MESSAGE);
		if (message == null) {
			message = baseRequest.getResponse().getReason();
		}
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "*");
		response.addHeader("Access-Control-Allow-Headers", "*");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException exception) {
			throw new IOServerException(exception);
		}
		writer.print(message);
		baseRequest.setHandled(true);
	}

	@Override
	public final ByteBuffer badMessageError(int status, String reason, HttpFields.Mutable fields) {
		return BufferUtil.toBuffer(reason);
	}
}
