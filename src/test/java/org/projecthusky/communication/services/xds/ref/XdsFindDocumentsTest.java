/*
 * This code is made available under the terms of the Eclipse Public License v1.0
 * in the github project https://github.com/project-husky/husky there you also
 * find a list of the contributors and the license information.
 *
 * This project has been developed further and modified by the joined working group Husky
 * on the basis of the eHealth Connector opensource project from June 28, 2021,
 * whereas medshare GmbH is the initial and main contributor/author of the eHealth Connector.
 */
package org.projecthusky.communication.services.xds.ref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status;
import org.projecthusky.common.communication.Destination;
import org.projecthusky.common.model.Code;
import org.projecthusky.common.model.Identificator;
import org.projecthusky.communication.requests.xds.XdsFindFoldersStoredQuery;
import org.projecthusky.communication.requests.xds.XdsRegistryStoredFindDocumentsQuery;
import org.projecthusky.communication.services.HuskyService;
import org.projecthusky.communication.testhelper.IpfApplicationConfig;
import org.projecthusky.communication.testhelper.MetadataChecker;
import org.projecthusky.communication.testhelper.TestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
		TestApplication.class, IpfApplicationConfig.class })
@ActiveProfiles("atna")
public class XdsFindDocumentsTest {

	@Value(value = "${test.xds.xcq.uri:http://localhost:9988/xdstools7.12.0/sim/project_husky__integration_test/rg/xcq}")
	private String webserviceUri;

	@Autowired
	private HuskyService service;

	private Destination dest;

	@BeforeEach
	public void setUp() {
		this.dest = new Destination();
		dest.setUri(URI.create(webserviceUri));
		dest.setSenderApplicationOid("1.2.3.4");
	}

	/**
	 * Test to find document metadata for a scanned document
	 * 
	 * PatientId = HUSKY-200-SCD^^^&2.16.756.5.30.1.99999.1000&ISO <br>
	 * UniqueId = 2.25.156817188312541163954803632376225384899<br>
	 * EntryUUID = b8b95560-b5c2-4e9d-965c-f4fb56bb1f98
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindDocumentMetadata_ScannedDocument_basicParams_DocumentIsReturned()
			throws Exception {
		Identificator identificator = new Identificator("2.16.756.5.30.1.99999.1000",
				"HUSKY-200-SCD");

		XdsRegistryStoredFindDocumentsQuery query = this.service
				.createRegistryStoredFindDocumentsQuery(dest, identificator)
				.availabilityStatus(AvailabilityStatus.APPROVED).build();
		QueryResponse response = this.service.send(query);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
		assertFalse(response.getDocumentEntries().isEmpty());

		DocumentEntry documentEntry = response.getDocumentEntries().get(0);

		MetadataChecker.checkScannedDocumentMetadata(documentEntry);

	}

	/**
	 * Test to find document metadata for a scanned document
	 * 
	 * PatientId = HUSKY-200-SCD^^^&2.16.756.5.30.1.99999.1000&ISO <br>
	 * UniqueId = 2.25.156817188312541163954803632376225384899<br>
	 * EntryUUID = b8b95560-b5c2-4e9d-965c-f4fb56bb1f98
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindDocumentMetadata_ScannedDocument_withAllParams_DocumentIsReturned()
			throws Exception {
		Identificator identificator = new Identificator("2.16.756.5.30.1.99999.1000",
				"HUSKY-200-SCD");

		XdsRegistryStoredFindDocumentsQuery query = this.service
				.createRegistryStoredFindDocumentsQuery(dest, identificator)
				.availabilityStatus(AvailabilityStatus.APPROVED)
				.classCode(new Code(Code.builder().withCode("422735006")
						.withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Summary clinical document (record artifact)").build()))
				.typeCode(new Code(Code.builder().withCode("419891008")
						.withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Record artifact (record artifact)").build()))
				.confidentialityCode(new Code(
						Code.builder().withCode("17621005").withCodeSystem("2.16.840.1.113883.6.96")
								.withDisplayName("Normal (qualifier value)").build()))
				.formatCode(new Code(Code.builder().withCode("urn:ihe:iti:xds-sd:pdf:2008")
						.withCodeSystem("1.3.6.1.4.1.19376.1.2.3")
						.withDisplayName("1.3.6.1.4.1.19376.1.2.20 (Scanned Document)").build()))
				.healthCareFacilityCode(new Code(Code.builder().withCode("394747008")
						.withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Health Authority").build()))
				.practiceSettingCode(new Code(Code.builder().withCode("394810000")
						.withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Rheumatology (qualifier value)").build()))
				.build();
		QueryResponse response = this.service.send(query);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
		assertFalse(response.getDocumentEntries().isEmpty());

		DocumentEntry documentEntry = response.getDocumentEntries().get(0);

		MetadataChecker.checkScannedDocumentMetadata(documentEntry);
	}

	/**
	 * Test to find document metadata for a scanned document
	 * 
	 * PatientId = HUSKY-200-SCD^^^&2.16.756.5.30.1.99999.1000&ISO <br>
	 * UniqueId = 2.25.156817188312541163954803632376225384899<br>
	 * EntryUUID = b8b95560-b5c2-4e9d-965c-f4fb56bb1f98
	 * 
	 * @throws Exception
	 */
	@Test
	public void whenFoldersAreQueriedByPatientIdentificator_success() throws Exception {
		Identificator identificator = new Identificator("2.16.756.5.30.1.99999.1000",
				"HUSKY-200-SCD");

		XdsFindFoldersStoredQuery query = this.service.createFindFoldersStoredQuery()
				.destination(dest).patientID(identificator)
				.availabilityStatus(AvailabilityStatus.APPROVED).build();

		QueryResponse response = this.service.send(query);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
	}

	/**
	 * Test to find document metadata for a scanned document
	 * 
	 * PatientId = HUSKY-201-CDA^^^&2.16.756.5.30.1.99999.1000&ISO <br>
	 * UniqueId = 2.25.259556660619149366916497015027152147518<br>
	 * EntryUUID = fdd6a0fc-7686-406e-aefb-db61787e0c20
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindDocumentMetadata_CdaDocument_basicParams_DocumentIsReturned()
			throws Exception {
		Identificator identificator = new Identificator("2.16.756.5.30.1.99999.1000",
				"HUSKY-201-CDA");

		XdsRegistryStoredFindDocumentsQuery query = this.service
				.createRegistryStoredFindDocumentsQuery(dest, identificator)
				.availabilityStatus(AvailabilityStatus.APPROVED).build();
		QueryResponse response = this.service.send(query);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
		assertFalse(response.getDocumentEntries().isEmpty());

		DocumentEntry documentEntry = response.getDocumentEntries().get(0);
		MetadataChecker.checkCdaDocumentMetadata(documentEntry);
	}

	/**
	 * Test to find document metadata for a scanned document
	 * 
	 * PatientId = HUSKY-203-FHI^^^&2.16.756.5.30.1.99999.1000&ISO <br>
	 * UniqueId = 2.25.214777870628014861879219079172344039193<br>
	 * EntryUUID = 5ecb7fe0-7565-4acb-ac96-1c48e1ccc641
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindDocumentMetadata_FhirDocument_basicParams_DocumentIsReturned()
			throws Exception {
		Identificator identificator = new Identificator("2.16.756.5.30.1.99999.1000",
				"HUSKY-203-FHI");

		XdsRegistryStoredFindDocumentsQuery query = this.service
				.createRegistryStoredFindDocumentsQuery(dest, identificator)
				.availabilityStatus(AvailabilityStatus.APPROVED).build();
		QueryResponse response = this.service.send(query);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
		assertFalse(response.getDocumentEntries().isEmpty());

		DocumentEntry documentEntry = response.getDocumentEntries().get(0);
		MetadataChecker.checkFhirDocumentMetadata(documentEntry);
	}

	/**
	 * Test to find document metadata for a scanned document
	 * 
	 * PatientId = HUSKY-202-PDF^^^&2.16.756.5.30.1.99999.1000&ISO <br>
	 * UniqueId = 2.25.14253846181530789730333135870476612181<br>
	 * EntryUUID = 687f3d19-d5d1-4c67-8e46-51f6ecae8a8f
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindDocumentMetadata_PdfDocument_basicParams_DocumentIsReturned()
			throws Exception {
		Identificator identificator = new Identificator("2.16.756.5.30.1.99999.1000",
				"HUSKY-202-PDF");

		XdsRegistryStoredFindDocumentsQuery query = this.service
				.createRegistryStoredFindDocumentsQuery(dest, identificator)
				.availabilityStatus(AvailabilityStatus.APPROVED).build();
		QueryResponse response = this.service.send(query);

		// check if query was successful
		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
		assertFalse(response.getDocumentEntries().isEmpty());

		DocumentEntry documentEntry = response.getDocumentEntries().get(0);
		MetadataChecker.checkPdfDocumentMetadata(documentEntry);
	}

	// //////
	// Helper methods

}
