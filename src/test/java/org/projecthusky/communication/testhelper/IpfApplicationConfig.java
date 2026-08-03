package org.projecthusky.communication.testhelper;

import org.apache.camel.spring.boot.vault.CyberArkVaultAutoConfiguration;
import org.openehealth.ipf.commons.ihe.ws.cxf.payload.InPayloadLoggerInterceptor;
import org.openehealth.ipf.commons.ihe.ws.cxf.payload.OutPayloadLoggerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class, CyberArkVaultAutoConfiguration.class })
public class IpfApplicationConfig {
	
	@Value(value = "${test.log.file:log/TestEHC.log}")
	private String logFile;

	@Bean
	InPayloadLoggerInterceptor serverInLogger() {
		return new InPayloadLoggerInterceptor(logFile);
	}

	@Bean
	OutPayloadLoggerInterceptor serverOutLogger() {
		return new OutPayloadLoggerInterceptor(logFile);
	}

}
