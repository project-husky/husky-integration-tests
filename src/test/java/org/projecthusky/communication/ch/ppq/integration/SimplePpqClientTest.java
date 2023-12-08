package org.projecthusky.communication.ch.ppq.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.camel.CamelContext;
import org.projecthusky.communication.ch.ppq.TestApplication;
import org.projecthusky.communication.ch.ppq.api.PrivacyPolicyQuery;
import org.projecthusky.communication.ch.ppq.api.config.PpClientConfig;
import org.projecthusky.communication.ch.ppq.impl.PrivacyPolicyQueryBuilderImpl;
import org.projecthusky.communication.ch.ppq.impl.PrivacyPolicyQueryResponseImpl;
import org.projecthusky.communication.ch.ppq.impl.clients.ClientFactoryCh;
import org.projecthusky.communication.ch.ppq.impl.clients.SimplePpqClient;
import org.projecthusky.communication.ch.ppq.impl.config.PpClientConfigBuilderImpl;
import org.projecthusky.xua.hl7v3.InstanceIdentifier;
import org.projecthusky.xua.hl7v3.impl.InstanceIdentifierBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehealth.ipf.commons.audit.AuditContext;
import org.openehealth.ipf.commons.ihe.xacml20.Xacml20Utils;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(value = SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = { TestApplication.class })
@EnableAutoConfiguration
@Disabled
class SimplePpqClientTest {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private AuditContext auditContext;
	
	@Value(value = "${test.ppq.uri:https://ehealthsuisse.ihe-europe.net:10443/ppq-repository}")
	private String urlToPpq;
	
	@Value(value = "${test.ppq.keystore.file:src/test/resources/testKeystore.jks}")
	private String clientKeyStore;
	@Value(value = "${test.ppq.keystore.password:changeit}")
	private String clientKeyStorePass;
	@Value(value = "${test.ppq.keystore.type:JKS}")
	private String clientKeyStoreType;
	
	@Value(value = "${test.truststore.file:src/test/resources/truststore.p12}")
	private String clientTrustStore;
	@Value(value = "${test.truststore.password:changeit}")
	private String clientTrustStorePass;
	@Value(value = "${test.truststore.type:pkcs12}")
	private String clientTrustStoreType;

	/**
	 * This method initializes IPF and OpenSAML XACML modules and sets key- and
	 * truststore.
	 */
	@BeforeEach
	public void setup() throws JAXBException {
		try {
			InitializationService.initialize();
			Xacml20Utils.initializeHerasaf();

			System.setProperty("javax.net.ssl.keyStore", clientKeyStore);
			System.setProperty("javax.net.ssl.keyStorePassword", clientKeyStorePass);
			System.setProperty("javax.net.ssl.keyStoreType", clientKeyStoreType);
			System.setProperty("javax.net.ssl.trustStore", clientTrustStore);
			System.setProperty("javax.net.ssl.trustStorePassword", clientTrustStorePass);
			System.setProperty("javax.net.ssl.trustStoreType", clientTrustStoreType);
		} catch (InitializationException e1) {
			e1.printStackTrace();
		}
	}

	@Test
	void testQueryPolicyWithUnknownPid() throws Exception {
		// initialize client to query policies
		PpClientConfig config = new PpClientConfigBuilderImpl().url(urlToPpq).clientKeyStore(clientKeyStore)
				.clientKeyStorePassword(clientKeyStorePass).create();
		SimplePpqClient client = ClientFactoryCh.getPpqClient(config);
		client.setCamelContext(camelContext);
		client.setAuditContext(auditContext);

		InstanceIdentifier instanceIdentifier = new InstanceIdentifierBuilder().buildObject();
		instanceIdentifier.setExtension("123");
		instanceIdentifier.setRoot("2.16.756.5.30.1.127.3.10.3");

		// create query object with unknown person ID
		PrivacyPolicyQuery query = new PrivacyPolicyQueryBuilderImpl().instanceIdentifier(instanceIdentifier)
				.issueInstant(new GregorianCalendar()).version("2.0").id(UUID.randomUUID().toString()).create();

		// query policies
		PrivacyPolicyQueryResponseImpl response = (PrivacyPolicyQueryResponseImpl) client.send(null, query);

		// check if request failed
		assertNotNull(response);
		assertNotNull(response.getWrappedObject());
		assertNotNull(response.getWrappedObject().getStatus());
		assertNotNull(response.getWrappedObject().getStatus().getStatusCode());
		assertNotNull(response.getWrappedObject().getStatus().getStatusMessage());
		assertEquals("urn:e-health-suisse:2015:error:not-holder-of-patient-policies",
				response.getWrappedObject().getStatus().getStatusCode().getValue());

		assertNotNull(response.getWrappedObject().getAssertions());
	}

	@Test
	void testQueryPolicyWithUnknownPolicySetId() throws Exception {

		// initialize client to query policies
		PpClientConfig config = new PpClientConfigBuilderImpl().url(urlToPpq).clientKeyStore(clientKeyStore)
				.clientKeyStorePassword(clientKeyStorePass).create();
		SimplePpqClient client = ClientFactoryCh.getPpqClient(config);
		client.setCamelContext(camelContext);
		client.setAuditContext(auditContext);

		// create query object without details
		PrivacyPolicyQuery query = new PrivacyPolicyQueryBuilderImpl()
				.issueInstant(new GregorianCalendar()).version("2.0").id(UUID.randomUUID().toString()).create();

		// query policies
		PrivacyPolicyQueryResponseImpl response = (PrivacyPolicyQueryResponseImpl) client
				.send(null, query);

		// check if request failed
		assertNotNull(response);
		assertNotNull(response.getWrappedObject());
		assertNotNull(response.getWrappedObject().getStatus());
		assertNotNull(response.getWrappedObject().getStatus().getStatusCode());
		assertNotNull(response.getWrappedObject().getStatus().getStatusMessage());
		assertEquals("urn:e-health-suisse:2015:error:not-holder-of-patient-policies",
				response.getWrappedObject().getStatus().getStatusCode().getValue());

		assertEquals("The PolicySet with the given PolicySetIdReference does not exist",
				response.getWrappedObject().getStatus().getStatusMessage().getValue());

		assertNotNull(response.getWrappedObject().getAssertions());
	}

	/**
	 * This test checks the behavior of the
	 * {@link SimplePpqClient#send(org.projecthusky.xua.core.SecurityHeaderElement, PrivacyPolicyQuery)
	 * when querying policies.
	 * 
	 * @throws Exception
	 */
	@Test
	void testQueryHcpPolicyWithPolicy() throws Exception {

		// initialize client to query policies
		PpClientConfig config = new PpClientConfigBuilderImpl().url(urlToPpq).clientKeyStore(clientKeyStore)
				.clientKeyStorePassword(clientKeyStorePass).create();
		SimplePpqClient client = ClientFactoryCh.getPpqClient(config);
		client.setCamelContext(camelContext);
		client.setAuditContext(auditContext);

		// set identifier for whom the policies are to be queried
		InstanceIdentifier instanceIdentifier = new InstanceIdentifierBuilder().buildObject();
		instanceIdentifier.setExtension("761337610411265304");
		instanceIdentifier.setRoot("2.16.756.5.30.1.127.3.10.3");

		// create query object
		PrivacyPolicyQuery query = new PrivacyPolicyQueryBuilderImpl().instanceIdentifier(instanceIdentifier)
				.issueInstant(new GregorianCalendar()).version("2.0").id(UUID.randomUUID().toString()).create();

		// query policies
		PrivacyPolicyQueryResponseImpl response = (PrivacyPolicyQueryResponseImpl) client.send(null, query);

		// check if request was successful
		assertNotNull(response);
		assertNotNull(response.getWrappedObject());
		assertNotNull(response.getWrappedObject().getStatus());
		assertNotNull(response.getWrappedObject().getStatus().getStatusCode());
		assertNotNull(response.getWrappedObject().getStatus().getStatusMessage());
		assertEquals("urn:oasis:names:tc:SAML:2.0:status:Success",
				response.getWrappedObject().getStatus().getStatusCode().getValue());

		// check if policy assertions are returned
		assertNotNull(response.getWrappedObject().getAssertions());

	}


}
