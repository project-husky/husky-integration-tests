package org.projecthusky.xua.communication.xua.impl.ch.integration;

import org.apache.commons.io.IOUtils;
import org.projecthusky.communication.testhelper.TestApplication;
import org.projecthusky.xua.authentication.AuthnRequest;
import org.projecthusky.xua.authentication.impl.AuthnRequestBuilderImpl;
import org.projecthusky.xua.communication.clients.XuaClient;
import org.projecthusky.xua.communication.clients.impl.ClientFactory;
import org.projecthusky.xua.communication.clients.impl.IdpSoapBindingClientByBasicAuth;
import org.projecthusky.xua.communication.config.XuaClientConfig;
import org.projecthusky.xua.communication.config.impl.IdpClientBasicAuthConfigBuilderImpl;
import org.projecthusky.xua.communication.config.impl.IdpClientBasicAuthConfigImpl;
import org.projecthusky.xua.communication.config.impl.XuaClientConfigBuilderImpl;
import org.projecthusky.xua.communication.xua.RequestType;
import org.projecthusky.xua.communication.xua.TokenType;
import org.projecthusky.xua.communication.xua.XUserAssertionResponse;
import org.projecthusky.xua.communication.xua.impl.AppliesToBuilderImpl;
import org.projecthusky.xua.communication.xua.impl.ch.XUserAssertionRequestBuilderChImpl;
import org.projecthusky.xua.core.SecurityHeaderElement;
import org.projecthusky.xua.deserialization.impl.AssertionDeserializerImpl;
import org.projecthusky.xua.exceptions.ClientSendException;
import org.projecthusky.xua.exceptions.DeserializeException;
import org.projecthusky.xua.exceptions.SerializeException;
import org.projecthusky.xua.hl7v3.PurposeOfUse;
import org.projecthusky.xua.hl7v3.Role;
import org.projecthusky.xua.hl7v3.impl.CodedWithEquivalentsBuilder;
import org.projecthusky.xua.saml2.Assertion;
import org.projecthusky.xua.saml2.impl.AttributeImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehealth.ipf.commons.ihe.xacml20.stub.saml20.assertion.AttributeStatementType;
import org.openehealth.ipf.commons.ihe.xacml20.stub.saml20.assertion.AttributeType;
import org.openehealth.ipf.commons.ihe.xacml20.stub.saml20.assertion.StatementAbstractType;
import org.opensaml.saml.saml2.core.impl.AttributeValueImpl;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The purpose of this test class is to check if the assertion query works for a
 * user.
 */
@ExtendWith(value = SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = { TestApplication.class })
@EnableAutoConfiguration
class XuaClientTest extends ServerTestHelper {

	@Value(value = "${test.xua.uri:https://ehealthsuisse.ihe-europe.net:10443/STS}")
	private String urlToXua;
	
	@Value(value = "${test.idp.uri:https://ehealthsuisse.ihe-europe.net/idp/profile/SAML2/SOAP/ECP}")
	private String urlToIdp;
	
	@Value(value = "${test.xua.keystore.file:src/test/resources/testKeystoreXua.jks}")
	private String clientKeyStore;
	@Value(value = "${test.xua.keystore.password:changeit}")
	private String clientKeyStorePass;
	@Value(value = "${test.xua.keystore.type:jks}")
	private String clientKeyStoreType;
	
	@Value(value = "${test.xua.assertion.tcu.file:src/test/resources/Assertion_Technical_User.xml}")
	private String xuaAssertionTCUFile;
	
	@Value(value = "${test.xua.assertion.pad.file:src/test/resources/Assertion_Policy_Administration.xml}")
	private String xuaAssertionPADFile;
	
	@Value(value = "${test.xua.assertion.dadm.file:src/test/resources/Assertion_Document_Administration.xml}")
	private String xuaAssertionDADMFile;
	
	/**
	 * This test checks the behavior of the
	 * {@link XuaClient#send(SecurityHeaderElement, org.projecthusky.xua.communication.xua.XUserAssertionRequest)
	 * when querying a XUA assertion for a registered assistant
	 * 
	 * @throws Exception
	 */
	@Test
	void testGetAssertionWithForAssistant() throws ClientSendException, DeserializeException, SAXException,
			IOException, ParserConfigurationException, SerializeException {

		// initialize XUA client to query XUA assertion
		XuaClientConfig xuaClientConfig = new XuaClientConfigBuilderImpl().clientKeyStore(clientKeyStore)
				.clientKeyStorePassword(clientKeyStorePass).clientKeyStoreType(clientKeyStoreType).url(urlToXua).create();

		XuaClient client = ClientFactory.getXuaClient(xuaClientConfig);

		// request IDP assertion with username ltieche and password azerty
		var idpAssertion = requestIdpAssertion("ltieche", "azerty");

		// set role of subject
		var role = new CodedWithEquivalentsBuilder().code(org.projecthusky.communication.ch.enums.stable.Role.ASSISTANT_CODE)
				.codeSystem("2.16.756.5.30.1.127.3.10.6")
				.displayName("Behandelnde(r)")
				.buildObject(Role.DEFAULT_NS_URI, Role.DEFAULT_ELEMENT_LOCAL_NAME,
						Role.DEFAULT_PREFIX);

		// set the purpose of use
		var purposeOfUse = new CodedWithEquivalentsBuilder().code("NORM").codeSystem("2.16.756.5.30.1.127.3.10.6")
				.displayName("Normal Access").buildObject(PurposeOfUse.DEFAULT_NS_URI,
						PurposeOfUse.DEFAULT_ELEMENT_LOCAL_NAME, PurposeOfUse.DEFAULT_PREFIX);

		// specify information about the principal
		var principalId = "7601002467373";
		var principalName = "Richard Reynolds";

		// set ID of patient with namespace EPR_SPID
		String resourceId = "761337610411265304^^^SPID&2.16.756.5.30.1.127.3.10.3&ISO";

		var xuaAssertionRequest = new XUserAssertionRequestBuilderChImpl().principal(principalId, principalName)
				.requestType(RequestType.WST_ISSUE).tokenType(TokenType.OASIS_WSS_SAML_PROFILE_11_SAMLV20)
				.appliesTo(new AppliesToBuilderImpl().address("https://localhost:17001/services/iti18").create())
				.purposeOfUse(purposeOfUse).subjectRole(role).resourceId(resourceId).create();

		// query XUA assertion
		List<XUserAssertionResponse> response = client.send(idpAssertion, xuaAssertionRequest);

		// check if assertion is returned
		assertNotNull(response);
		assertNotNull(response.get(0).getAssertion());

		// check if correct issuer is included in assertion
		assertEquals("https://ehealthsuisse.ihe-europe.net/STS", response.get(0).getAssertion().getIssuer().getValue());
		
		String actualRole = null;
		String actualPurposeOfUse = null;
		String actualSubjectId = null;
		String actualResourceId = null;

		for (StatementAbstractType statement : response.get(0).getAssertion()
				.getStatementOrAuthnStatementOrAuthzDecisionStatement()) {
			if (statement instanceof AttributeStatementType) {
				AttributeStatementType attributeStatment = (AttributeStatementType) statement;

				for (Object obj : attributeStatment.getAttributeOrEncryptedAttribute()) {
					if (obj instanceof AttributeType) {
						AttributeImpl attribute = (AttributeImpl) obj;

						if (attribute.isValueARole()) {
							actualRole = attribute.getValueAsRole().getCode();
						}

						if (attribute.isValueAPurposeOfUse()) {
							actualPurposeOfUse = attribute.getValueAsPurposeOfUse().getCode();
						}

						if (attribute.getName().equalsIgnoreCase("urn:oasis:names:tc:xspa:1.0:subject:subject-id")) {
							actualSubjectId = ((AttributeValueImpl) attribute.getWrappedObject().getAttributeValues()
									.get(0)).getTextContent();
						}

						if (attribute.getName().equalsIgnoreCase("urn:oasis:names:tc:xacml:2.0:resource:resource-id")) {
							actualResourceId = (((AttributeValueImpl) attribute.getWrappedObject().getAttributeValues()
									.get(0))).getTextContent();
						}
					}
				}
			}
		}

		// check attribute parameter of returned assertion
		assertEquals("HCP", actualRole);
		assertEquals(purposeOfUse.getCode(), actualPurposeOfUse);
		assertEquals("Richard Reynolds", actualSubjectId);
		assertEquals(resourceId, actualResourceId);

	}

	/**
	 * This test checks the behavior of the
	 * {@link XuaClient#send(SecurityHeaderElement, org.projecthusky.xua.communication.xua.XUserAssertionRequest)
	 * when querying a XUA assertion for a registered technical user
	 * 
	 * @throws Exception
	 */
	@Test
	void testGetAssertionWithForTechnicalUser()
			throws ClientSendException, DeserializeException, SAXException, IOException, ParserConfigurationException {

		// initialize XUA client to query XUA assertion
		XuaClientConfig xuaClientConfig = new XuaClientConfigBuilderImpl().clientKeyStore(clientKeyStore)
				.clientKeyStorePassword(clientKeyStorePass).clientKeyStoreType(clientKeyStoreType).url(urlToXua).create();

		XuaClient client = ClientFactory.getXuaClient(xuaClientConfig);

		try (InputStream is = new FileInputStream(new File(xuaAssertionTCUFile))) {

			var idpAssertion = new AssertionDeserializerImpl().fromXmlByteArray(IOUtils.toByteArray(is));

			// set role of subject
			var role = new CodedWithEquivalentsBuilder().code("TCU").codeSystem("2.16.756.5.30.1.127.3.10.6")
					.displayName("Behandelnde(r)")
					.buildObject(Role.DEFAULT_NS_URI, Role.DEFAULT_ELEMENT_LOCAL_NAME, Role.DEFAULT_PREFIX);

			// set the purpose of use
			var purposeOfUse = new CodedWithEquivalentsBuilder().code("AUTO").codeSystem("2.16.756.5.30.1.127.3.10.6")
					.displayName("Normal Access").buildObject(PurposeOfUse.DEFAULT_NS_URI,
							PurposeOfUse.DEFAULT_ELEMENT_LOCAL_NAME, PurposeOfUse.DEFAULT_PREFIX);

			// specify information about the principal
			var principalId = "7601000050717";
			var principalName = "Marc Loris Agpar";

			// set ID of patient with namespace EPR_SPID
			String resourceId = "761337610411265304^^^SPID&2.16.756.5.30.1.127.3.10.3&ISO";

			var xuaAssertionRequest = new XUserAssertionRequestBuilderChImpl().principal(principalId, principalName)
					.requestType(RequestType.WST_ISSUE).tokenType(TokenType.OASIS_WSS_SAML_PROFILE_11_SAMLV20)
					.appliesTo(new AppliesToBuilderImpl().address("https://localhost:17001/services/iti18").create())
					.purposeOfUse(purposeOfUse).subjectRole(role).resourceId(resourceId).create();

			// query XUA assertion
			List<XUserAssertionResponse> response = client.send(idpAssertion, xuaAssertionRequest);

			// check if assertion is returned
			assertNotNull(response);
			assertNotNull(response.get(0).getAssertion());

			// check if correct issuer is included in assertion
			assertEquals("https://ehealthsuisse.ihe-europe.net/STS",
					response.get(0).getAssertion().getIssuer().getValue());

			String actualRole = null;
			String actualPurposeOfUse = null;
			String actualSubjectId = null;
			String actualResourceId = null;

			for (StatementAbstractType statement : response.get(0).getAssertion()
					.getStatementOrAuthnStatementOrAuthzDecisionStatement()) {
				if (statement instanceof AttributeStatementType) {
					AttributeStatementType attributeStatment = (AttributeStatementType) statement;

					for (Object obj : attributeStatment.getAttributeOrEncryptedAttribute()) {
						if (obj instanceof AttributeType) {
							AttributeImpl attribute = (AttributeImpl) obj;

							if (attribute.isValueARole()) {
								actualRole = attribute.getValueAsRole().getCode();
							}

							if (attribute.isValueAPurposeOfUse()) {
								actualPurposeOfUse = attribute.getValueAsPurposeOfUse().getCode();
							}

							if (attribute.getName()
									.equalsIgnoreCase("urn:oasis:names:tc:xspa:1.0:subject:subject-id")) {
								actualSubjectId = ((AttributeValueImpl) attribute.getWrappedObject()
										.getAttributeValues().get(0)).getTextContent();
							}

							if (attribute.getName()
									.equalsIgnoreCase("urn:oasis:names:tc:xacml:2.0:resource:resource-id")) {
								actualResourceId = (((AttributeValueImpl) attribute.getWrappedObject()
										.getAttributeValues().get(0))).getTextContent();
							}

						}
					}
				}
			}

			// check attribute parameter of returned assertion
			assertEquals("HCP", actualRole);
			assertEquals(purposeOfUse.getCode(), actualPurposeOfUse);
			assertEquals("Marc Loris Agpar", actualSubjectId);
			assertEquals(resourceId, actualResourceId);

		}

	}

	/**
	 * This test checks the behavior of the
	 * {@link XuaClient#send(SecurityHeaderElement, org.projecthusky.xua.communication.xua.XUserAssertionRequest)
	 * when querying a XUA assertion for a policy administrator
	 * 
	 * @throws Exception
	 */
	@Test
	void testGetAssertionWithForPolicyAdministration()
			throws ClientSendException, DeserializeException, SAXException, IOException, ParserConfigurationException {

		// initialize XUA client to query XUA assertion
		XuaClientConfig xuaClientConfig = new XuaClientConfigBuilderImpl().clientKeyStore(clientKeyStore)
				.clientKeyStorePassword(clientKeyStorePass).clientKeyStoreType(clientKeyStoreType).url(urlToXua).create();

		XuaClient client = ClientFactory.getXuaClient(xuaClientConfig);

		try (InputStream is = new FileInputStream(new File(xuaAssertionPADFile))) {

			var idpAssertion = new AssertionDeserializerImpl().fromXmlByteArray(IOUtils.toByteArray(is));

			// set role of subject
			var role = new CodedWithEquivalentsBuilder().code("PADM").codeSystem("2.16.756.5.30.1.127.3.10.6")
					.displayName("Behandelnde(r)")
					.buildObject(Role.DEFAULT_NS_URI, Role.DEFAULT_ELEMENT_LOCAL_NAME, Role.DEFAULT_PREFIX);

			// set the purpose of use
			var purposeOfUse = new CodedWithEquivalentsBuilder().code("NORM").codeSystem("2.16.756.5.30.1.127.3.10.6")
					.displayName("Normal Access").buildObject(PurposeOfUse.DEFAULT_NS_URI,
							PurposeOfUse.DEFAULT_ELEMENT_LOCAL_NAME, PurposeOfUse.DEFAULT_PREFIX);

			// set ID of patient with namespace EPR_SPID
			String resourceId = "761337610411265304^^^SPID&2.16.756.5.30.1.127.3.10.3&ISO";

			var xuaAssertionRequest = new XUserAssertionRequestBuilderChImpl().requestType(RequestType.WST_ISSUE)
					.tokenType(TokenType.OASIS_WSS_SAML_PROFILE_11_SAMLV20).purposeOfUse(purposeOfUse).subjectRole(role)
					.resourceId(resourceId).create();

			// query XUA assertion
			List<XUserAssertionResponse> response = client.send(idpAssertion, xuaAssertionRequest);

			// check if assertion is returned
			assertNotNull(response);
			assertNotNull(response.get(0).getAssertion());

			// check if correct issuer is included in assertion
			assertEquals("https://ehealthsuisse.ihe-europe.net/STS",
					response.get(0).getAssertion().getIssuer().getValue());

			String actualRole = null;
			String actualPurposeOfUse = null;
			String actualSubjectId = null;
			String actualResourceId = null;

			for (StatementAbstractType statement : response.get(0).getAssertion()
					.getStatementOrAuthnStatementOrAuthzDecisionStatement()) {
				if (statement instanceof AttributeStatementType) {
					AttributeStatementType attributeStatment = (AttributeStatementType) statement;

					for (Object obj : attributeStatment.getAttributeOrEncryptedAttribute()) {
						if (obj instanceof AttributeType) {
							AttributeImpl attribute = (AttributeImpl) obj;

							if (attribute.isValueARole()) {
								actualRole = attribute.getValueAsRole().getCode();
							}

							if (attribute.isValueAPurposeOfUse()) {
								actualPurposeOfUse = attribute.getValueAsPurposeOfUse().getCode();
							}

							if (attribute.getName()
									.equalsIgnoreCase("urn:oasis:names:tc:xspa:1.0:subject:subject-id")) {
								actualSubjectId = ((AttributeValueImpl) attribute.getWrappedObject()
										.getAttributeValues().get(0)).getTextContent();
							}

							if (attribute.getName()
									.equalsIgnoreCase("urn:oasis:names:tc:xacml:2.0:resource:resource-id")) {
								actualResourceId = (((AttributeValueImpl) attribute.getWrappedObject()
										.getAttributeValues().get(0))).getTextContent();
							}

						}
					}
				}
			}

			// check attribute parameter of returned assertion
			assertEquals(role.getCode(), actualRole);
			assertEquals(purposeOfUse.getCode(), actualPurposeOfUse);
			assertEquals("Ivo Castineira", actualSubjectId);
			assertEquals(resourceId, actualResourceId);

		}
	}

	/**
	 * This test checks the behavior of the
	 * {@link XuaClient#send(SecurityHeaderElement, org.projecthusky.xua.communication.xua.XUserAssertionRequest)
	 * when querying a XUA assertion for a registered document administrator
	 * 
	 * @throws Exception
	 */
	@Test
	void testGetAssertionWithForDocumentAdministration()
			throws ClientSendException, DeserializeException, SAXException, IOException, ParserConfigurationException {

		// initialize XUA client to query XUA assertion
		XuaClientConfig xuaClientConfig = new XuaClientConfigBuilderImpl().clientKeyStore(clientKeyStore)
				.clientKeyStorePassword(clientKeyStorePass).clientKeyStoreType(clientKeyStoreType).url(urlToXua).create();

		XuaClient client = ClientFactory.getXuaClient(xuaClientConfig);

		try (InputStream is = new FileInputStream(new File(xuaAssertionDADMFile))) {

			var idpAssertion = new AssertionDeserializerImpl().fromXmlByteArray(IOUtils.toByteArray(is));

			// set role of subject
			var role = new CodedWithEquivalentsBuilder().code("DADM").codeSystem("2.16.756.5.30.1.127.3.10.6")
					.displayName("Behandelnde(r)")
					.buildObject(Role.DEFAULT_NS_URI, Role.DEFAULT_ELEMENT_LOCAL_NAME, Role.DEFAULT_PREFIX);

			// set the purpose of use
			var purposeOfUse = new CodedWithEquivalentsBuilder().code("NORM").codeSystem("2.16.756.5.30.1.127.3.10.6")
					.displayName("Normal Access").buildObject(PurposeOfUse.DEFAULT_NS_URI,
							PurposeOfUse.DEFAULT_ELEMENT_LOCAL_NAME, PurposeOfUse.DEFAULT_PREFIX);

			// set ID of patient with namespace EPR_SPID
			String resourceId = "761337610411265304^^^SPID&2.16.756.5.30.1.127.3.10.3&ISO";

			var xuaAssertionRequest = new XUserAssertionRequestBuilderChImpl().requestType(RequestType.WST_ISSUE)
					.tokenType(TokenType.OASIS_WSS_SAML_PROFILE_11_SAMLV20).purposeOfUse(purposeOfUse).subjectRole(role)
					.resourceId(resourceId).create();

			// query XUA assertion
			List<XUserAssertionResponse> response = client.send(idpAssertion, xuaAssertionRequest);

			// check if assertion is returned
			assertNotNull(response);
			assertNotNull(response.get(0).getAssertion());

			// check if correct issuer is included in assertion
			assertEquals("https://ehealthsuisse.ihe-europe.net/STS",
					response.get(0).getAssertion().getIssuer().getValue());

			String actualRole = null;
			String actualPurposeOfUse = null;
			String actualSubjectId = null;
			String actualResourceId = null;

			for (StatementAbstractType statement : response.get(0).getAssertion()
					.getStatementOrAuthnStatementOrAuthzDecisionStatement()) {
				if (statement instanceof AttributeStatementType) {
					AttributeStatementType attributeStatment = (AttributeStatementType) statement;

					for (Object obj : attributeStatment.getAttributeOrEncryptedAttribute()) {
						if (obj instanceof AttributeType) {
							AttributeImpl attribute = (AttributeImpl) obj;

							if (attribute.isValueARole()) {
								actualRole = attribute.getValueAsRole().getCode();
							}

							if (attribute.isValueAPurposeOfUse()) {
								actualPurposeOfUse = attribute.getValueAsPurposeOfUse().getCode();
							}

							if (attribute.getName()
									.equalsIgnoreCase("urn:oasis:names:tc:xspa:1.0:subject:subject-id")) {
								actualSubjectId = ((AttributeValueImpl) attribute.getWrappedObject()
										.getAttributeValues().get(0)).getTextContent();
							}

							if (attribute.getName()
									.equalsIgnoreCase("urn:oasis:names:tc:xacml:2.0:resource:resource-id")) {
								actualResourceId = (((AttributeValueImpl) attribute.getWrappedObject()
										.getAttributeValues().get(0))).getTextContent();
							}

						}
					}
				}
			}

			// check attribute parameter of returned assertion
			assertEquals(role.getCode(), actualRole);
			assertEquals(purposeOfUse.getCode(), actualPurposeOfUse);
			assertEquals("Käthi Weisskopf", actualSubjectId);
			assertEquals(resourceId, actualResourceId);
		}
	}

	/**
	 * This method requests IDP assertion of gazelle environment.
	 * 
	 * @param user     username
	 * @param password password
	 * 
	 * @return received IDP assertion
	 * @throws ClientSendException
	 */
	private Assertion requestIdpAssertion(String user, String password) throws ClientSendException {
		IdpClientBasicAuthConfigImpl idpClientConfig = new IdpClientBasicAuthConfigBuilderImpl()
				.basicAuthPassword(password).basicAuthUsername(user).url(urlToIdp).create();

		IdpSoapBindingClientByBasicAuth client = (IdpSoapBindingClientByBasicAuth) ClientFactory
				.getIdpClient(idpClientConfig);

		AuthnRequest authnRequest = new AuthnRequestBuilderImpl()
				.assertionConsumerServiceUrl("https://sp-clone.ihe-europe.net/Shibboleth.sso/SAML2/ECP")
				.issueInstant(new GregorianCalendar()).id(String.valueOf(System.currentTimeMillis()))
				.protocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:PAOS").version("2.0")
				.issuer("https://sp-clone.ihe-europe.net/shibboleth").nameIdPolicyAllowCreate(true)
				.providerId("https://idp.ihe-europe.net/idp/shibboleth").create();
		var response = client.send(authnRequest);

		return response.getAssertions().get(0);

	}

}
