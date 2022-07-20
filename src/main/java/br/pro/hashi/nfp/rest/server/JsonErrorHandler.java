package br.pro.hashi.nfp.rest.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.BufferUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JsonErrorHandler extends ErrorHandler {
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "*");
		response.addHeader("Access-Control-Allow-Headers", "*");
		String message = (String) request.getAttribute(Dispatcher.ERROR_MESSAGE);
		if (message == null) {
			message = baseRequest.getResponse().getReason();
		}
		if (message != null) {
			response.setContentType("text/plain");
			try {
				PrintWriter writer = response.getWriter();
				writer.print(message);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		baseRequest.setHandled(true);
	}

	@Override
	public ByteBuffer badMessageError(int status, String reason, HttpFields.Mutable fields) {
		return BufferUtil.toBuffer(reason);
	}
}
