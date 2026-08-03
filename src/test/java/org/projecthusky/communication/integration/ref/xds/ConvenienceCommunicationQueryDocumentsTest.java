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
package org.projecthusky.communication.integration.ref.xds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.projecthusky.common.communication.AffinityDomain;
import org.projecthusky.common.communication.Destination;
import org.projecthusky.common.model.Code;
import org.projecthusky.common.model.Identificator;
import org.projecthusky.common.model.Name;
import org.projecthusky.common.model.Person;
import org.projecthusky.communication.ConvenienceCommunication;
import org.projecthusky.communication.testhelper.MetadataChecker;
import org.projecthusky.communication.testhelper.TestApplication;
import org.projecthusky.communication.testhelper.XdsTestUtils;
import org.projecthusky.communication.xd.storedquery.FindDocumentsQuery;
import org.projecthusky.communication.xd.storedquery.GetDocumentsQuery;
import org.apache.camel.spring.boot.vault.CyberArkVaultAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehealth.ipf.commons.audit.AuditContext;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.ObjectReference;
import org.openehealth.ipf.commons.ihe.xds.core.responses.ErrorCode;
import org.openehealth.ipf.commons.ihe.xds.core.responses.ErrorInfo;
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Severity;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The purpose of this test class is to check whether document metadata
 * retrieval (XDS ITI-18) works with a wide variety of parameters.
 */
@ExtendWith(value = SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
		TestApplication.class })
@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class, CyberArkVaultAutoConfiguration.class })
class ConvenienceCommunicationQueryDocumentsTest extends XdsTestUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConvenienceCommunicationQueryDocumentsTest.class.getName());

	@Autowired
	private ConvenienceCommunication convenienceCommunication;

	@Autowired
	protected AuditContext auditContext;

	@Value(value = "${test.xds.xcq.uri:http://localhost:9988/xdstools7.12.0/sim/project_husky__integration_test/rg/xcq}")
	private String xcqUri;

	final private String applicationName = "2.16.840.1.113883.3.72.6.5.100.1399";
	final private String facilityName = null;

	final private String senderApplicationOid = "1.2.3.4";

	private AffinityDomain affinityDomain = null;

	/**
	 * This method creates and start spring test application. Moreover, it sets
	 * the endpoint of XDS service for querying metadata.
	 *
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		// create and start spring test application
		var app = new SpringApplication(TestApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.run();

		// sets XDS service endpoint
		affinityDomain = new AffinityDomain();
		final Destination dest = new Destination();

		try {
			dest.setUri(new URI(xcqUri));
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		dest.setSenderApplicationOid(senderApplicationOid);
		dest.setReceiverApplicationOid(applicationName);
		dest.setReceiverFacilityOid(facilityName);
		affinityDomain.setRegistryDestination(dest);
		affinityDomain.setRepositoryDestination(dest);
	}

	/**
	 * This method checks if initialization of {@link ConvenienceCommunication}
	 * was correct.
	 */
	@Test
	void contextLoads() {
		assertNotNull(convenienceCommunication);
		assertNotNull(convenienceCommunication.getCamelContext());
	}

	/**
	 * This test checks the behavior of the
	 * {@link ConvenienceCommunication#queryDocuments(org.projecthusky.communication.xd.storedquery.AbstractStoredQuery, org.projecthusky.xua.core.SecurityHeaderElement)}
	 * when no documents are found.
	 * 
	 * @throws Exception
	 */
	@Test
	void queryFindDocumentsEmptyResponseTest() throws Exception {

		// ID of the patient for whom the metadata is to be searched for
		Identificator patientId = new Identificator("1.3.6.1.4.1.21367.13.20.1000", "IHEBLUE-2737");

		FindDocumentsQuery findDocumentsQuery = new FindDocumentsQuery(patientId,
				AvailabilityStatus.APPROVED);

		convenienceCommunication.setAffinityDomain(affinityDomain);

		// query metadata of documents
		final QueryResponse response = convenienceCommunication.queryDocuments(findDocumentsQuery,
				null, null);

		// check if errors are returned
		assertTrue(response.getErrors().isEmpty());

		// check if no document metadata are returned
		assertTrue(response.getDocuments().isEmpty());
		assertTrue(response.getDocumentEntries().isEmpty());

		// check if query was successful
		assertEquals(Status.SUCCESS, response.getStatus());
	}

	/**
	 * This test checks the behavior of the
	 * {@link ConvenienceCommunication#queryDocuments(org.projecthusky.communication.xd.storedquery.AbstractStoredQuery, org.projecthusky.xua.core.SecurityHeaderElement)}
	 * if no patient ID is passed.
	 * 
	 * @throws Exception
	 */
	@Test
	void queryFindDocumentsNoPatientIdExpectedErrorTest() throws Exception {

		FindDocumentsQuery findDocumentsQuery = new FindDocumentsQuery(null,
				AvailabilityStatus.APPROVED);

		convenienceCommunication.setAffinityDomain(affinityDomain);

		// query metadata of documents
		final QueryResponse response = convenienceCommunication.queryDocuments(findDocumentsQuery,
				null, null);

		// check if query failed
		assertEquals(Status.FAILURE, response.getStatus());
		assertFalse(response.getErrors().isEmpty());

		// check details of returned errors
		assertEquals(2, response.getErrors().size());

		ErrorInfo error = response.getErrors().get(0);
		assertEquals(ErrorCode.REGISTRY_ERROR, error.getErrorCode());
		assertEquals("StoredQuery.java", error.getLocation());
		assertEquals(Severity.ERROR, error.getSeverity());

		error = response.getErrors().get(1);
		assertEquals(ErrorCode.REGISTRY_ERROR, error.getErrorCode());
		assertEquals("QueryRequestMessageValidator", error.getLocation());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * This test checks the behavior of the
	 * {@link ConvenienceCommunication#queryDocuments(org.projecthusky.communication.xd.storedquery.AbstractStoredQuery, org.projecthusky.xua.core.SecurityHeaderElement)}
	 * when at least metadata is found for one PDF document
	 * 
	 * @throws Exception
	 */
	/*
	 * test function to checks attributes - no need to reduce number of
	 * assertions
	 */
	@Test
	@SuppressWarnings("java:S5961")
	void queryFindDocumentsMetadataOfPdf() throws Exception {

		Identificator patientId = new Identificator("2.16.756.5.30.1.99999.1000", "HUSKY-202-PDF");

		FindDocumentsQuery findDocumentsQuery = new FindDocumentsQuery(patientId,
				AvailabilityStatus.APPROVED);

		convenienceCommunication.setAffinityDomain(affinityDomain);

		// query metadata of documents with patient ID and approved as
		// availability
		// status
		final QueryResponse response = convenienceCommunication.queryDocuments(findDocumentsQuery,
				null, null);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
		assertTrue(response.getDocumentEntries().size() > 0);

		DocumentEntry documentEntry = response.getDocumentEntries().get(0);
		
		MetadataChecker.checkPdfDocumentMetadata(documentEntry);

//		// check if identifiers (unique ID, repository ID and home community ID)
//		// are
//		// equal
//		assertEquals("2.25.272325930096337302465411782668491329407", documentEntry.getUniqueId());
//		assertEquals("1.1.4567332.1.75", documentEntry.getRepositoryUniqueId());
//		assertEquals("urn:oid:1.1.4567334.1.6", documentEntry.getHomeCommunityId());
//		assertEquals("urn:uuid:63e22bfe-5d6a-4360-8045-44b938ced995", documentEntry.getEntryUuid());
//
//		assertEquals(AvailabilityStatus.APPROVED, documentEntry.getAvailabilityStatus());
//		assertEquals("application/fhir+json", documentEntry.getMimeType());
//
//		assertNull(documentEntry.getComments());
//		assertNull(documentEntry.getDocumentAvailability());
//
//		assertEquals("Impfung", documentEntry.getTitle().getValue());
//		assertEquals("20241212154748", documentEntry.getCreationTime().toHL7());
//
//		// check different codes
//		assertEquals("de-CH", documentEntry.getLanguageCode());
//
//		assertNotNull(documentEntry.getClassCode());
//		assertEquals("184216000", documentEntry.getClassCode().getCode());
//		assertEquals("2.16.840.1.113883.6.96", documentEntry.getClassCode().getSchemeName());
//		assertEquals("Patient record type (record artifact)",
//				documentEntry.getClassCode().getDisplayName().getValue());
//
//		assertNotNull(documentEntry.getConfidentialityCodes().get(0));
//		assertEquals("17621005", documentEntry.getConfidentialityCodes().get(0).getCode());
//		assertEquals("Normal",
//				documentEntry.getConfidentialityCodes().get(0).getDisplayName().getValue());
//
//		assertTrue(documentEntry.getEventCodeList().isEmpty());
//
//		assertEquals("urn:che:epr:EPR_Unstructured_Document",
//				documentEntry.getFormatCode().getCode());
//		assertEquals("2.16.756.5.30.1.127.3.10.10", documentEntry.getFormatCode().getSchemeName());
//		assertEquals("Unstructured EPR document",
//				documentEntry.getFormatCode().getDisplayName().getValue());
//
//		assertEquals("22232009", documentEntry.getHealthcareFacilityTypeCode().getCode());
//		assertEquals("2.16.840.1.113883.6.96",
//				documentEntry.getHealthcareFacilityTypeCode().getSchemeName());
//		assertEquals("Hospital (environment)",
//				documentEntry.getHealthcareFacilityTypeCode().getDisplayName().getValue());
//
//		assertEquals("394802001", documentEntry.getPracticeSettingCode().getCode());
//		assertEquals("General medicine (qualifier value)",
//				documentEntry.getPracticeSettingCode().getDisplayName().getValue());
//		assertEquals("2.16.840.1.113883.6.96",
//				documentEntry.getPracticeSettingCode().getSchemeName());
//
//		assertEquals("41000179103", documentEntry.getTypeCode().getCode());
//		assertEquals("Immunization Record (record artifact)",
//				documentEntry.getTypeCode().getDisplayName().getValue());
//		assertEquals("2.16.840.1.113883.6.96", documentEntry.getTypeCode().getSchemeName());
//
//		// check patient details
//		assertEquals("IHERED-1024", documentEntry.getPatientId().getId());
//		assertEquals("1.3.6.1.4.1.21367.13.20.1000",
//				documentEntry.getPatientId().getAssigningAuthority().getUniversalId());
//
//		assertEquals("waldspital-Id-1234", documentEntry.getSourcePatientId().getId());
//		assertEquals("1.2.3.4.123456.1",
//				documentEntry.getSourcePatientId().getAssigningAuthority().getUniversalId());
//
//		// check author details
//		assertFalse(documentEntry.getAuthors().isEmpty());
//		assertNotNull(documentEntry.getAuthors().get(0));
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson());
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson().getName());
//		assertEquals("MÃ¼ller",
//				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getFamilyName());
//		assertEquals("Peter",
//				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getGivenName());
//		assertEquals("Dr. med",
//				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getPrefix());
//
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole());
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole().get(0));
//		assertEquals("HCP", documentEntry.getAuthors().get(0).getAuthorRole().get(0).getId());
//		assertEquals("2.16.756.5.30.1.127.3.10.6", documentEntry.getAuthors().get(0).getAuthorRole()
//				.get(0).getAssigningAuthority().getUniversalId());
//
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty());
//		// assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0));
//		// assertNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0).getId());
//		// assertNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0)
//		// .getAssigningAuthority().getUniversalId());
	}

	/**
	 * This test checks the behavior of the
	 * {@link ConvenienceCommunication#queryDocuments(org.projecthusky.communication.xd.storedquery.AbstractStoredQuery, org.projecthusky.xua.core.SecurityHeaderElement)}
	 * when at least metadata is found for one CDA document with following
	 * metadata:
	 * 
	 * <ul>
	 * <li>patient ID</li>
	 * <li>class code</li>
	 * <li>practice setting</li>
	 * <li>health care facility</li>
	 * <li>confidentiality</li>
	 * <li>format</li>
	 * <li>given and last name of author</li>
	 * <li>approved as availability status</li>
	 * </ul>
	 * 
	 * @throws Exception
	 */
	/*
	 * test function to checks attributes - no need to reduce number of
	 * assertions
	 */
	@Test
	@SuppressWarnings("java:S5961")
	void queryFindDocumentsMetadataOfCda() throws Exception {

		Identificator patientId = new Identificator("2.16.756.5.30.1.99999.1000", "HUSKY-201-CDA");

		List<Code> classCodes = List.of(new Code("184216000", "2.16.840.1.113883.6.96",
				"Patient record type (record artifact)"));

		List<Code> formatCodes = List.of(//
				new Code("urn:ihe:pcc:ic:2009", "1.3.6.1.4.1.19376.1.2.3",
						"Immunization Content (IC)"));

		List<Code> confidentialityCodes = List
				.of(new Code("17621005", "2.16.840.1.113883.6.96", "Normal (qualifier value)"));

		List<Code> healthcareFacilityCodes = List.of(new Code("264358009", "2.16.840.1.113883.6.96",
				"General practice premises (environment)"));

		List<Code> practiceSettingCodes = List.of(new Code("394802001", "2.16.840.1.113883.6.96",
				"General medicine (qualifier value)"));

		Person person = new Person();
		var name = new Name();
		name.setFamily("Allzeit");
		name.setGiven("Bereit");
		name.setPrefix("Dr.");
		person.addName(name);

		FindDocumentsQuery findDocumentsQuery = new FindDocumentsQuery(patientId, classCodes, null,
				practiceSettingCodes, healthcareFacilityCodes, confidentialityCodes, formatCodes,
				null/* person */, AvailabilityStatus.APPROVED);

		// FindDocumentsQuery findDocumentsQuery = new
		// FindDocumentsQuery(patientId,
		// availabilityStatus);

		convenienceCommunication.setAffinityDomain(affinityDomain);

		// query metadata of documents
		final QueryResponse response = convenienceCommunication.queryDocuments(findDocumentsQuery,
				null, null);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
		assertTrue(response.getDocumentEntries().size() > 0);

		response.getDocumentEntries().forEach(documentEntry -> {
			LOGGER.info("DocumentEntry:\n\t{},\n\t{},\n\t{},\n\t{},\n\t{},\n\t{}\n\t{}",
					documentEntry.getUniqueId(), documentEntry.getRepositoryUniqueId(),
					documentEntry.getHomeCommunityId(), documentEntry.getEntryUuid(),
					documentEntry.getMimeType(),
					documentEntry.getClassCode().getCode() + "|"
							+ documentEntry.getClassCode().getSchemeName() + "|"
							+ documentEntry.getClassCode().getDisplayName(), //
					documentEntry.getFormatCode().getCode() + "|"
							+ documentEntry.getFormatCode().getSchemeName() + "|"
							+ documentEntry.getFormatCode().getDisplayName());
		});

		DocumentEntry documentEntry = response.getDocumentEntries().get(0);
		
		MetadataChecker.checkCdaDocumentMetadata(documentEntry);

//		// check if identifiers (unique ID, repository ID and home community ID)
//		// are
//		// equal
//		// 2.25.190208551364738359849377333875388436319,
//		// 1.1.4567332.1.75,
//		// urn:oid:1.1.4567334.1.6,
//		// urn:uuid:49466513-5a4c-40bc-93cb-3f65344f04cf,
//		// text/xml,
//		// 419891008|2.16.840.1.113883.6.96|LocalizedString(lang=en-US,
//		// charset=UTF-8, value=Record artifact (record artifact))
//		// urn:ihe:iti:xds-sd:pdf:2008|1.3.6.1.4.1.19376.1.2.3|LocalizedString(lang=en-US,
//		// charset=UTF-8, value=1.3.6.1.4.1.19376.1.2.20 (Scanned Document))
//		assertEquals("2.25.259556660619149366916497015027152147518", documentEntry.getUniqueId());
//		assertEquals("1.1.4567332.1.1", documentEntry.getRepositoryUniqueId());
//		assertEquals("urn:oid:1.1.4567334.1.6", documentEntry.getHomeCommunityId());
//		// Entry UUID is different in HUSKY SIM!
//		// assertEquals("urn:uuid:762b3855-55de-4baa-b71e-df6a4dd79faf",
//		// documentEntry.getEntryUuid());
//
//		assertEquals(AvailabilityStatus.APPROVED, documentEntry.getAvailabilityStatus());
//		assertEquals("text/xml", documentEntry.getMimeType());
//
//		assertNull(documentEntry.getComments());
//		assertNull(documentEntry.getDocumentAvailability());
//
//		assertNotNull(documentEntry.getTitle());
//		// Creation time is different in HUSKY SIM!
//		// assertEquals("20250328065355",
//		// documentEntry.getCreationTime().toHL7());
//
//		// check different codes
//		assertEquals("de-CH", documentEntry.getLanguageCode());
//
//		assertNotNull(documentEntry.getClassCode());
//		assertEquals("184216000", documentEntry.getClassCode().getCode());
//		assertEquals("2.16.840.1.113883.6.96", documentEntry.getClassCode().getSchemeName());
//		assertEquals("Patient record type (record artifact)",
//				documentEntry.getClassCode().getDisplayName().getValue());
//
//		assertNotNull(documentEntry.getConfidentialityCodes().get(0));
//		assertEquals("17621005", documentEntry.getConfidentialityCodes().get(0).getCode());
//		assertEquals("Normal (qualifier value)",
//				documentEntry.getConfidentialityCodes().get(0).getDisplayName().getValue());
//
//		assertTrue(documentEntry.getEventCodeList().isEmpty());
//
//		assertEquals("urn:ihe:pcc:ic:2009", documentEntry.getFormatCode().getCode());
//		assertEquals("1.3.6.1.4.1.19376.1.2.3", documentEntry.getFormatCode().getSchemeName());
//		assertEquals("Immunization Content (IC)",
//				documentEntry.getFormatCode().getDisplayName().getValue());
//
//		assertEquals("264358009", documentEntry.getHealthcareFacilityTypeCode().getCode());
//		assertEquals("2.16.840.1.113883.6.96",
//				documentEntry.getHealthcareFacilityTypeCode().getSchemeName());
//		assertEquals("General practice premises (environment)",
//				documentEntry.getHealthcareFacilityTypeCode().getDisplayName().getValue());
//
//		assertEquals("394802001", documentEntry.getPracticeSettingCode().getCode());
//		assertEquals("General medicine (qualifier value)",
//				documentEntry.getPracticeSettingCode().getDisplayName().getValue());
//		assertEquals("2.16.840.1.113883.6.96",
//				documentEntry.getPracticeSettingCode().getSchemeName());
//
//		assertEquals("41000179103", documentEntry.getTypeCode().getCode());
//		assertEquals("Immunization record (record artifact)",
//				documentEntry.getTypeCode().getDisplayName().getValue());
//		assertEquals("2.16.840.1.113883.6.96", documentEntry.getTypeCode().getSchemeName());
//
//		// check patient details
//		assertEquals("HUSKY-201-CDA", documentEntry.getPatientId().getId());
//		assertEquals("2.16.756.5.30.1.99999.1000",
//				documentEntry.getPatientId().getAssigningAuthority().getUniversalId());
//
//		assertEquals("HUSKY-201-CDA", documentEntry.getSourcePatientId().getId());
//		assertEquals("1.2.3.4",
//				documentEntry.getSourcePatientId().getAssigningAuthority().getUniversalId());
//
//		// check author details
//		assertFalse(documentEntry.getAuthors().isEmpty());
//		assertNotNull(documentEntry.getAuthors().get(0));
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson());
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson().getName());
//		assertEquals("Bereit",
//				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getFamilyName());
//		assertEquals("Allzeit",
//				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getGivenName());
//		assertEquals("Dr.",
//				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getPrefix());
//
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole());
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole().get(0));
//		assertEquals("HCP", documentEntry.getAuthors().get(0).getAuthorRole().get(0).getId());
//		assertEquals("2.16.756.5.30.1.127.3.10.1.1.3", documentEntry.getAuthors().get(0)
//				.getAuthorRole().get(0).getAssigningAuthority().getUniversalId());
//
//		assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty());
//		// assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0));
//		// assertNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0).getId());
//		// assertNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0)
//		// .getAssigningAuthority().getUniversalId());
	}

	/**
	 * This test checks the behavior of the
	 * {@link ConvenienceCommunication#queryDocumentReferencesOnly(org.projecthusky.communication.xd.storedquery.AbstractStoredQuery, org.projecthusky.xua.core.SecurityHeaderElement)}
	 * when only the reference to a document is to be returned in the query.
	 * 
	 * @throws Exception
	 */
	@Test
	void queryGetDocumentsMetadataOfCda() throws Exception {

		// unique IDs of documents for which the document references are
		// searched for
		List<String> uniqueIds = new LinkedList<>();
		uniqueIds.add("2.25.259556660619149366916497015027152147518");

		GetDocumentsQuery getDocumentsQuery = new GetDocumentsQuery(uniqueIds, false,
				"urn:oid:1.1.4567334.1.6");

		convenienceCommunication.setAffinityDomain(affinityDomain);

		final QueryResponse response = convenienceCommunication.queryDocumentReferencesOnly(
				getDocumentsQuery, null,
				String.format("urn:uuid:testMessage-%s", UUID.randomUUID().toString()));

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());

		// check if references are returned.
		assertTrue(response.getReferences().size() > 0);

		// check if retrieved reference is correct
		ObjectReference objectRef = response.getReferences().iterator().next();
		assertEquals("urn:uuid:2c41c4b0-5c63-4dc3-9486-8394990001ca", objectRef.getId());
		assertEquals("urn:oid:1.1.4567334.1.6", objectRef.getHome());
	}
}
