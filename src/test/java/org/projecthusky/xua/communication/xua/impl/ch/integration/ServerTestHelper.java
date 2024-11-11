/*
 * This code is made available under the terms of the Eclipse Public License v1.0 
 * in the github project https://github.com/project-husky/husky there you also 
 * find a list of the contributors and the license information.
 * 
 * This project has been developed further and modified by the joined working group Husky 
 * on the basis of the eHealth Connector opensource project from June 28, 2021, 
 * whereas medshare GmbH is the initial and main contributor/author of the eHealth Connector.
 *
 */
package org.projecthusky.xua.communication.xua.impl.ch.integration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
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

	@BeforeAll
	public static void setUpBefore() throws IOException {
		final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(15000, TimeUnit.MILLISECONDS)
				.setTcpNoDelay(true).build();

		server = ServerBootstrap.bootstrap().setSocketConfig(socketConfig)
				.register("*", (httpRequest, httpResponse, httpContext) -> {
					logger.debug("The request {} {} {}", httpRequest.getMethod(), httpRequest.getPath(),
							httpRequest.getVersion());
					httpResponse.setCode(500);
					httpResponse.setHeader("Server", "Test/1.1");
					httpResponse.setEntity(new StringEntity("Hello this is a testserver"));
				}).create();

		server.start();
	}

	@AfterAll
	public static void tearDownAfter() {
		server.stop();
	}

}
