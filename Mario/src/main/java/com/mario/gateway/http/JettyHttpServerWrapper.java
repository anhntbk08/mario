package com.mario.gateway.http;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.mario.config.serverwrapper.HttpServerWrapperConfig;
import com.mario.gateway.serverwrapper.BaseServerWrapper;

public class JettyHttpServerWrapper extends BaseServerWrapper {

	private ServletContextHandler handler;
	private Server server;

	@Override
	public void init() {
		if (!(getConfig() instanceof HttpServerWrapperConfig)) {
			throw new RuntimeException(
					"Illegal config, expected for " + HttpServerWrapperConfig.class.getName() + " instance");
		}

		Server server = new Server(((HttpServerWrapperConfig) this.getConfig()).getPort());
		server.setHandler(this.getHandler());

		this.server = server;
	}

	private ServletContextHandler getHandler() {
		if (this.handler == null && this.getConfig() != null && this.getConfig() instanceof HttpServerWrapperConfig) {
			HttpServerWrapperConfig config = (HttpServerWrapperConfig) this.getConfig();
			ServletContextHandler handler = new ServletContextHandler(config.getOptions());
			if (config.getOptions() > 0 && config.getSessionTimeout() > 0) {
				handler.getSessionHandler().getSessionManager().setMaxInactiveInterval(config.getSessionTimeout());
			}
			this.handler = handler;
		}
		return this.handler;
	}

	public boolean isRunning() {
		return this.server != null && this.server.isRunning();
	}

	@Override
	public synchronized void start() {
		if (this.isRunning()) {
			throw new IllegalStateException("Server is already running");
		}
		try {
			this.server.start();
			getLogger().info(this.getConfig().getName() + " - http server wrapper started at "
					+ ((HttpServerWrapperConfig) this.getConfig()).getPort());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void stop() {
		if (this.isRunning()) {
			try {
				this.server.stop();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new IllegalStateException("Server has been stopped");
		}
	}

	public void addServlet(ServletHolder holder, String path) {
		this.getHandler().addServlet(holder, path);
	}

	public SessionHandler getSessionHandler() {
		return this.handler.getSessionHandler();
	}
}
