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
package org.projecthusky.communication.integration;

import org.apache.commons.io.IOUtils;
import org.projecthusky.common.basetypes.NameBaseType;
import org.projecthusky.common.communication.*;
import org.projecthusky.common.enums.DocumentDescriptor;
import org.projecthusky.common.enums.EhcVersions;
import org.projecthusky.common.enums.LanguageCode;
import org.projecthusky.common.model.Author;
import org.projecthusky.common.model.Code;
import org.projecthusky.common.model.Identificator;
import org.projecthusky.common.model.Name;
import org.projecthusky.common.utils.datatypes.IheCx;
import org.projecthusky.communication.ConvenienceCommunication;
import org.projecthusky.communication.testhelper.TestApplication;
import org.projecthusky.communication.testhelper.XdsTestUtils;
import org.projecthusky.xua.communication.clients.XuaClient;
import org.projecthusky.xua.communication.clients.impl.ClientFactory;
import org.projecthusky.xua.communication.config.XuaClientConfig;
import org.projecthusky.xua.communication.config.impl.XuaClientConfigBuilderImpl;
import org.projecthusky.xua.communication.xua.RequestType;
import org.projecthusky.xua.communication.xua.TokenType;
import org.projecthusky.xua.communication.xua.XUserAssertionResponse;
import org.projecthusky.xua.communication.xua.impl.XUserAssertionRequestBuilderImpl;
import org.projecthusky.xua.deserialization.impl.AssertionDeserializerImpl;
import org.projecthusky.xua.hl7v3.PurposeOfUse;
import org.projecthusky.xua.hl7v3.Role;
import org.projecthusky.xua.hl7v3.impl.CodedWithEquivalentsBuilder;
import org.projecthusky.xua.saml2.Assertion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehealth.ipf.commons.core.OidGenerator;
import org.openehealth.ipf.commons.ihe.xds.core.responses.*;
import org.opensaml.core.config.InitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to test the ProvideAndRegisterDocumentSet [ITI-41] transaction with Swiss metadata requirements. This
 * test performs the following steps:
 * 1. load a test IdP Assertion from the disk
 * 2. Use the IdP Assertion in conjunction with the claims (role, purposeOfUse, EPR-SPID of the patient health record)
 * and request a X-User Assertion.
 * 3. Use the X-User Assertion in conjunction with a specific set of XDS metadata and the JSON file from disk in a
 * ProvideAndRegisterDocumentSet [ITI-41] transaction.
 */
@ExtendWith(value = SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TestApplication.class})
@EnableAutoConfiguration
class CHProvideAndRegisterDocumentSetTest extends XdsTestUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(CHProvideAndRegisterDocumentSetTest.class.getName());

    // Spring dependency injection which sets CamelContext, etc.
    @Autowired
    private ConvenienceCommunication convenienceCommunication;
    
    @Value(value = "${test.xua.uri:https://ehealthsuisse.ihe-europe.net:10443/STS}")
	private String urlToXua;
    
    // set the URL of the community repository you want to store the document to
    @Value(value = "${test.xds.pnr.uri:http://ehealthsuisse.ihe-europe.net:8280/xdstools7/sim/epr-testing__for_init_gw_testing/rep/prb}")
    private String repositoryURL;
    
	@Value(value = "${test.xua.keystore.file:src/test/resources/testKeystore.jks}")
	private String clientKeyStore;
	@Value(value = "${test.xua.keystore.password:changeit}")
	private String clientKeyStorePass;
	@Value(value = "${test.xua.keystore.type:JKS}")
	private String clientKeyStoreType;


    private AffinityDomain affinityDomain = null;

    // the local patient ID as registered in the PIX Feed transaction
    final String localAssigningAuthorityOid = "1.2.3.4.123456.1";
    final String localPatientId = "waldspital-Id-1234";

    // The global patient ID generated by the community as received with the PIX Query response.
    // The global patient ID is required in this test as destinationId parameter of the transaction.
    final String globalAssigningAuthorityOid = "1.3.6.1.4.1.21367.13.20.1000";
    final String globalPatientId = "IHERED-1024";

    // The Swiss unique patient identifier as expected from the PIX Query response.
    // The SPID is required as claim in the Get X-User Assertion transaction.
    final String spidAssigningAuthorityOid = "2.16.756.5.30.1.127.3.10.3";
    final String spid = "761337610411265304";


    /**
     * Creates and start spring test application and set the endpoint of XDS Repository.
     *
     * @throws Exception if something unexpected happens
     */
    @BeforeEach
    public void setUp() throws Exception {

        // initialize the open saml factories
        InitializationService.initialize();

        // create and start spring test application
        var app = new SpringApplication(TestApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run();

        final Destination dest = new Destination();

       
        dest.setUri(new URI(repositoryURL));

        // add the OID of your application as assigned by the community
        final String senderApplicationOid = "1.2.3.4";
        dest.setSenderApplicationOid(senderApplicationOid);

        // add an application name
        final String applicationOid = "2.16.840.1.113883.3.72.6.5.100.1399";
        dest.setReceiverApplicationOid(applicationOid);

        // add the name of your institution
        final String facilityOid = "2.16.840.1.113883.3.72.6.5";
        dest.setReceiverFacilityOid(facilityOid);

        affinityDomain = new AffinityDomain();
        affinityDomain.setRegistryDestination(dest);
        affinityDomain.setRepositoryDestination(dest);

        convenienceCommunication.setAffinityDomain(affinityDomain);

        // remove cached documents in ConvenienceCommunication
        convenienceCommunication.clearDocuments();
    }

    /**
     * This tests the
     * {@link ConvenienceCommunication#submit(SubmissionSetMetadata, org.projecthusky.xua.core.SecurityHeaderElement, String)}
     * with a JSON Document, Swiss compliant metadata and a X-User Assertion
     *
     * @throws Exception if something unexpected happens
     */
    @Test
    void submitJSONDocumentTest() throws Exception {

        // read and add file
        final File file = new File("src/test/resources/docSource/FHIR-Vaccination.json");
        final DocumentMetadata documentMetadata = convenienceCommunication.addDocument(DocumentDescriptor.FHIR_JSON, new FileInputStream(file));

        documentMetadata.setTitle("Impfung");

        // set the author data
        final Name name = new Name(new NameBaseType());
        name.setGiven("Peter");
        name.setFamily("Müller");
        name.setPrefix("Dr. med");

        final Author author = new Author();
        author.addName(name);

        final Code role = new Code("HCP", "2.16.756.5.30.1.127.3.10.6", "Healthcare professional");
        author.setRoleFunction(role);

        documentMetadata.addAuthor(author);

        final Identificator globalId = new Identificator(globalAssigningAuthorityOid, globalPatientId);
        documentMetadata.setDestinationPatientId(globalId);

        final Identificator localId = new Identificator(localAssigningAuthorityOid, localPatientId);
        documentMetadata.setSourcePatientId(localId);

        documentMetadata.setCodedLanguage(LanguageCode.GERMAN_CODE);

        final Code type = new Code("41000179103", "2.16.840.1.113883.6.96", "Immunization Record (record artifact)");
        documentMetadata.setTypeCode(type);

        /*
		    format code for vaccinations shall be ("urn:che:epr:ch-vacd:immunization-administration:2022", "1.3.6.1.4.1.19376.1.2.3", "CH VACD Immunization Administration")
		    but this code is currently not supported by the test system
		    */
        final Code format = new Code("urn:che:epr:EPR_Unstructured_Document", "2.16.756.5.30.1.127.3.10.10",
                "Unstructured EPR document");
        documentMetadata.setFormatCode(format);

        final Code clazz = new Code("184216000", "2.16.840.1.113883.6.96", "Patient record type (record artifact)");
        documentMetadata.setClassCode(clazz);

        final Code facility = new Code("22232009", "2.16.840.1.113883.6.96", "Hospital (environment)");
        documentMetadata.setHealthcareFacilityTypeCode(facility);

        final Code practice = new Code("394802001", "2.16.840.1.113883.6.96", "General medicine (qualifier value)");
        documentMetadata.setPracticeSettingCode(practice);

        final Code confidentiality = new Code("17621005", "2.16.840.1.113883.6.96", "Normal");
        documentMetadata.addConfidentialityCode(confidentiality);


        // submission set metadata settings
        final SubmissionSetMetadata submissionSetMetadata = new SubmissionSetMetadata();
        submissionSetMetadata.setUniqueId(OidGenerator.uniqueOid().toString());

        submissionSetMetadata.setSourceId(EhcVersions.getCurrentVersion().getOid());
        submissionSetMetadata.setEntryUUID(UUID.randomUUID().toString());

        submissionSetMetadata.setDestinationPatientId(globalId);

        submissionSetMetadata.getAuthor().add(authorPerson);

        final Code contentType = new Code("71388002", "2.16.840.1.113883.6.96", "Procedure (procedure)");
        submissionSetMetadata.setContentTypeCode(contentType);

        // Use author data from document metadata
        submissionSetMetadata.addAuthor(author);

        // Get the X-User Assertion to authorize the Document Submission.
        Assertion xUserAssertion = getXUserAssertion();
        assertNotNull(xUserAssertion);

        // set the audit config mode to activate the ATNA logs. Please note that you
        // need to configure additional properties for the ATNA communication
        // in file application.properties
        convenienceCommunication.setAtnaConfig(AtnaConfig.AtnaConfigMode.SECURE);

        // provide and register the document
        final Response response = convenienceCommunication.submit(submissionSetMetadata, xUserAssertion, null);

        // checks whether the document has been successfully submitted
        assertTrue(response.getErrors().isEmpty());
        assertEquals(Status.SUCCESS, response.getStatus());
    }

    /**
     * Retrieve an X-User Assertion for a Healthcare Provider from the test environment. In this test the IdP Assertion
     * required to authenticate the user in the Get X-User Assertion request is loaded from the disk
     *
     * @throws Exception if something unexpected happens
     */
    private Assertion getXUserAssertion() throws Exception {
  
        // initialize XUA client to query XUA assertion
        XuaClientConfig xuaClientConfig = new XuaClientConfigBuilderImpl().clientKeyStore(clientKeyStore)
                .clientKeyStorePassword(clientKeyStorePass).clientKeyStoreType(clientKeyStoreType).url(urlToXua).create();

        XuaClient client = ClientFactory.getXuaClient(xuaClientConfig);

        try (InputStream is = new FileInputStream(new File("src/test/resources/IdPAssertionHCP.xml"))) {

            // for testing load an IdP Assertion from disk.
            var idpAssertion = new AssertionDeserializerImpl().fromXmlByteArray(IOUtils.toByteArray(is));

            // define the attributes for the X-User Assertion request
            var role = new CodedWithEquivalentsBuilder().code("HCP").codeSystem("2.16.756.5.30.1.127.3.10.6")
                    .displayName("Behandelnde(r)")
                    .buildObject(Role.DEFAULT_NS_URI, Role.DEFAULT_ELEMENT_LOCAL_NAME, Role.DEFAULT_PREFIX);

            var purposeOfUse = new CodedWithEquivalentsBuilder().code("NORM").codeSystem("2.16.756.5.30.1.127.3.10.6")
                    .displayName("Normal Access").buildObject(PurposeOfUse.DEFAULT_NS_URI,
                            PurposeOfUse.DEFAULT_ELEMENT_LOCAL_NAME, PurposeOfUse.DEFAULT_PREFIX);

            // set the EPR SPID patient id
            final IheCx cx = new IheCx(spid, spidAssigningAuthorityOid);
            String eprSpid = cx.render();

            // build the  X-User Assertion request
            var assertionRequest = new XUserAssertionRequestBuilderImpl().requestType(RequestType.WST_ISSUE)
                    .tokenType(TokenType.OASIS_WSS_SAML_PROFILE_11_SAMLV20)
                    // .appliesTo(new AppliesToBuilderImpl().address("https://localhost:17001/services/iti18").create())
                    .purposeOfUse(purposeOfUse).subjectRole(role).resourceId(eprSpid).create();

            // query the  X-User Assertion
            List<XUserAssertionResponse> response = client.send(idpAssertion, assertionRequest);
            return response.get(0).getAssertion();
        }
    }

}
