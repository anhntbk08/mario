package com.mario.gateway.http;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.servlet.ServletHolder;

import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.HttpGatewayConfig;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.HttpMessage;
import com.mario.gateway.AbstractGateway;
import com.mario.gateway.serverwrapper.HasServerWrapper;
import com.mario.worker.MessageEventFactory;
import com.mario.worker.MessageHandlingWorkerPool;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;

public class HttpGateway extends AbstractGateway<HttpGatewayConfig>
		implements HasServerWrapper<JettyHttpServerWrapper> {
	private JettyHttpServerWrapper serverWrapper;

	private ServletHolder holder = new ServletHolder(new HttpServlet() {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		}

		@Override
		protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			if (req.getRequestURI().endsWith("favicon.ico")) {
				return;
			}
			HttpGateway.this.handle(req, resp);
		};
	});

	protected MessageHandlingWorkerPool createWorkerPool() {
		MessageHandlingWorkerPool messageHandlingWorkerPool = new MessageHandlingWorkerPool();
		if (this.getConfig().getWorkerPoolConfig() == null) {
			getLogger().info("Http gateway " + this.getName() + ", configured in " + this.getExtensionName()
					+ " has an empty worker pool config, using sync mode as default");
		} else {
			messageHandlingWorkerPool.setConfig(this.getConfig().getWorkerPoolConfig());
		}
		return messageHandlingWorkerPool;
	}

	@Override
	public void setServer(JettyHttpServerWrapper serverWrapper) {
		this.serverWrapper = serverWrapper;
	}

	@Override
	protected void _init() {
		if (this.getConfig().getWorkerPoolConfig() == null) {
			this.getConfig().setAsync(false);
		}
		if (this.getConfig().isUseMultipath()) {
			getLogger().debug("Http gateway " + this.getName() + " using multipath...");
			this.holder.getRegistration().setMultipartConfig(new MultipartConfigElement(this.getConfig().getPath()));
		}
		this.serverWrapper.addServlet(holder, this.getConfig().getPath());
	}

	private void handle(final HttpServletRequest request, final HttpServletResponse response) {

		// DecimalFormat df = new DecimalFormat("0.##");

		// long startTime = System.nanoTime();
		if (getConfig().isAsync()) {
			this.publishToWorkers(request.startAsync());
		} else {
			HttpMessage message = new HttpMessage();
			getDeserializer().decode(request, message);
			message.setResponse(response);
			publishToWorkers(message);
		}

		// getLogger().debug("Time: {}μs",
		// df.format(Double.valueOf(System.nanoTime() - startTime) / 1e3));

		// try {
		// context.getResponse().getWriter().append(context.getRequest().getParameter("key"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// context.complete();
	}

	@Override
	protected void _start() throws Exception {
		if (!this.serverWrapper.isRunning()) {
			this.serverWrapper.start();
		}
		getLogger().info("Http gateway listening on location: " + this.getConfig().getPath() + ", server: "
				+ this.getConfig().getServerWrapperName() + ", worker pool: " + this.getConfig().getWorkerPoolConfig());
	}

	@Override
	protected void _stop() throws Exception {
		if (this.serverWrapper != null && this.serverWrapper.isRunning()) {
			this.serverWrapper.stop();
		}
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {
		if (result instanceof PuNull) {
			return;
		}

		AsyncContext asyncContext = null;
		HttpServletResponse response = null;
		if (message instanceof HttpMessage) {
			asyncContext = ((HttpMessage) message).getContext();
			if (asyncContext != null) {
				response = (HttpServletResponse) asyncContext.getResponse();
			} else {
				response = (HttpServletResponse) ((HttpMessage) message).getResponse();
			}
		}

		if (this.getConfig().getHeaders().size() > 0) {
			for (Entry<String, String> header : this.getConfig().getHeaders().entrySet()) {
				response.setHeader(header.getKey(), header.getValue());
			}
		}
		if (this.getConfig().getContentType() != null) {
			response.setContentType(this.getConfig().getContentType());
		}
		if (this.getConfig().getEncoding() != null) {
			response.setCharacterEncoding(this.getConfig().getEncoding());
		}

		try {
			if (result != null) {
				response.getWriter().write(result.toJSON());
			}
		} catch (IOException e) {
			try {
				getLogger().error("unable to write response to context, try to write error", e);
				response.setStatus(500);
				response.getWriter().write(getFullStacktrace(e));
			} catch (IOException e1) {
				getLogger().error("unable to write response to context", e1);
			}
		} finally {
			if (asyncContext != null) {
				asyncContext.complete();
			}
		}
	}

	@Override
	public void onHandleError(Message message, Throwable exception) {
		String stacktrace = getFullStacktrace(exception);
		stacktrace = stacktrace != null ? stacktrace : "null";
		getLogger().error("Error while handling http request:\n--- MESSAGE ---\n{}\n--- STACKTRACE ---\n{}",
				message.getData(), stacktrace == null ? exception : stacktrace);
		AsyncContext asyncContext = null;
		HttpServletResponse response = null;
		if (message instanceof HttpMessage) {
			asyncContext = ((HttpMessage) message).getContext();
			if (asyncContext != null) {
				response = (HttpServletResponse) asyncContext.getResponse();
			} else {
				response = (HttpServletResponse) ((HttpMessage) message).getResponse();
			}
		}

		if (this.getConfig().getHeaders().size() > 0) {
			for (Entry<String, String> header : this.getConfig().getHeaders().entrySet()) {
				response.setHeader(header.getKey(), header.getValue());
			}
		}
		if (this.getConfig().getContentType() != null) {
			response.setContentType(this.getConfig().getContentType());
		}
		if (this.getConfig().getEncoding() != null) {
			response.setCharacterEncoding(this.getConfig().getEncoding());
		}

		try {
			response.setStatus(500);
			response.getWriter().write(stacktrace);
		} catch (Exception e) {
			getLogger().error("Unable to write response to context", e);
		} finally {
			if (asyncContext != null) {
				asyncContext.complete();
			}
		}
	}

	@Override
	protected MessageEventFactory createEventFactory() {
		return new MessageEventFactory() {
			@Override
			public Message newInstance() {
				HttpMessage msg = new HttpMessage();
				msg.setGatewayType(GatewayType.HTTP);
				msg.setCallback(HttpGateway.this);
				msg.setGatewayType(GatewayType.HTTP);
				return msg;
			}
		};
	}

	public SessionManager getSessionManager() {
		return serverWrapper.getSessionHandler().getSessionManager();
	}

}
