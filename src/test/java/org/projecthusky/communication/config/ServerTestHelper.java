package org.projecthusky.communication.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <!-- @formatter:off -->
 * <div class="en">Helping class for client testing with simulated server</div>
 * <div class="de"></div>
 * <div class="fr"></div>
 * <div class="it"></div>
 * <!-- @formatter:on -->
 */
public abstract class ServerTestHelper extends InitializerTestHelper {

	private static Logger logger = LoggerFactory.getLogger(ServerTestHelper.class);

	private static HttpServer server;

	private static int httpPort;

	private static ServerBootstrap bootstrap;

	protected static int getHttpPort() {
		return httpPort;
	}

	protected static HttpServer getServer() {
		return server;
	}

	public static HttpRequestHandler registerHandler() {
		return null;
	}

	@BeforeAll
	public static void setUpBefore() throws IOException {
		final SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(15000, TimeUnit.MILLISECONDS).setTcpNoDelay(true).build();

		bootstrap = ServerBootstrap.bootstrap();
		server = bootstrap//
				// .setServerInfo("Test/1.1").setSocketConfig(socketConfig)
				.register("*", new HttpRequestHandler() {

					@Override
					public void handle(ClassicHttpRequest request, ClassicHttpResponse response,
							HttpContext context) throws HttpException, IOException {
						logger.debug("The request %s", request.getRequestUri());
						response.setCode(500);
						response.setEntity(new StringEntity("Hello this is a testserver"));

					}

				}).create();

		server.start();
		httpPort = server.getLocalPort();

	}

	@AfterAll
	public static void tearDownAfter() {
		server.stop();
	}

}
