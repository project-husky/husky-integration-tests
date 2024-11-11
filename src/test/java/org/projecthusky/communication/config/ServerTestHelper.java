package org.projecthusky.communication.config;

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
