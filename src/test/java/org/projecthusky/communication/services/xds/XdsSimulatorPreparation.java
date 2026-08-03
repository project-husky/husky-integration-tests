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
package org.projecthusky.communication.services.xds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.apache.camel.spring.boot.vault.CyberArkVaultAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.core.OidGenerator;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Response;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status;
import org.projecthusky.common.communication.Destination;
import org.projecthusky.common.communication.DocumentMetadata;
import org.projecthusky.common.communication.SubmissionSetMetadata;
import org.projecthusky.common.enums.DocumentDescriptor;
import org.projecthusky.common.enums.EhcVersions;
import org.projecthusky.common.model.Code;
import org.projecthusky.common.model.Identificator;
import org.projecthusky.communication.requests.xds.XdsDocumentWithMetadata;
import org.projecthusky.communication.requests.xds.XdsProvideAndRetrieveDocumentSetQuery;
import org.projecthusky.communication.services.HuskyService;
import org.projecthusky.communication.testhelper.TestApplication;
import org.projecthusky.communication.utils.XdsTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
		TestApplication.class })
@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class, CyberArkVaultAutoConfiguration.class })
@Disabled("These tests are only for manual execution against the XDS simulator.")
public class XdsSimulatorPreparation extends XdsTestUtils {

	// HUSKY-200-SCD^^^&2.16.756.5.30.1.99999.1000&ISO
	// HUSKY-201-CDA^^^&2.16.756.5.30.1.99999.1000&ISO
	// HUSKY-202-PDF^^^&2.16.756.5.30.1.99999.1000&ISO
	// HUSKY-203-FHI^^^&2.16.756.5.30.1.99999.1000&ISO
	// IHERED-1030^^^&1.3.6.1.4.1.21367.13.20.1000&ISO

	@Autowired
	private HuskyService huskyService;

	@Value(value = "${test.xds.pnr.uri:http://localhost:9988/xdstools7.12.0/sim/project_husky__integration_test/rep/prb}")
	private String pnrUri;

	final private String applicationName = "2.16.840.1.113883.3.72.6.5.100.1399";
	final private String facilityName = null;

	final private String senderApplicationOid = "1.2.3.4";

	/**
	 * This method creates and start spring test application. Moreover, it sets
	 * the endpoint of XDS service for submitting documents.
	 *
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		// create and start spring test application
		var app = new SpringApplication(TestApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.run();
	}

	@Test
	void submitSimulatorTestScannedDocument() throws Exception {
		DocumentMetadata documentMetadata = new DocumentMetadata();
		setMetadataInGeneral(documentMetadata);

		documentMetadata.setUniqueId("2.25.156817188312541163954803632376225384899");
		documentMetadata.setEntryUUID("b8b95560-b5c2-4e9d-965c-f4fb56bb1f98");

		documentMetadata.setTitle("Scanned Document");

		documentMetadata.setTypeCode(new Code(
				Code.builder().withCode("419891008").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Record artifact (record artifact)").build()));
		documentMetadata.setClassCode(new Code(
				Code.builder().withCode("422735006").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Summary clinical document (record artifact)").build()));

		documentMetadata.addConfidentialityCode(new Code(
				Code.builder().withCode("17621005").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Normal (qualifier value)").build()));

		documentMetadata.setFormatCode(new Code(Code.builder()
				.withCode("urn:ihe:iti:xds-sd:pdf:2008").withCodeSystem("1.3.6.1.4.1.19376.1.2.3")
				.withDisplayName("1.3.6.1.4.1.19376.1.2.20 (Scanned Document)").build()));

		documentMetadata.setHealthcareFacilityTypeCode(new Code(
				Code.builder().withCode("394747008").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Health Authority").build()));
		documentMetadata.setPracticeSettingCode(new Code(
				Code.builder().withCode("394810000").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Rheumatology (qualifier value)").build()));

		Identificator patientId = new Identificator("2.16.756.5.30.1.99999.1000", "HUSKY-200-SCD");
		documentMetadata.setDestinationPatientId(patientId);
		documentMetadata.setSourcePatientId(new Identificator("1.2.3.4", "HUSKY-200-SCD"));
		// setMetadataForPdf(documentMetadata, patientId);

		XdsDocumentWithMetadata.XdsDocumentWithMetadataBuilder builder = this.huskyService
				.createDocumentWithMetadata();
		XdsDocumentWithMetadata documentWithMetadata = builder.documentMetadata(documentMetadata)
				.dataStream(getResourceAsStream("/docSource/ScannedDocument.pdf"))
				.documentDescriptor(DocumentDescriptor.PDF).build();

		SubmissionSetMetadata subSet = new SubmissionSetMetadata();
		setSubmissionMetadata(subSet, patientId, "Scanned Document");

		XdsProvideAndRetrieveDocumentSetQuery.XdsProvideAndRetrieveDocumentSetQueryBuilder documentSetQueryBuilder = this.huskyService
				.createProvideAndRetrieveDocumentSetQuery();
		XdsProvideAndRetrieveDocumentSetQuery provideAndRetrieveDocumentSetQuery = documentSetQueryBuilder
				.documentWithMetadata(List.of(documentWithMetadata))
				.destination(createDestination()).submissionSetMetadata(subSet).build();

		// submit added documents
		Response response = huskyService.send(provideAndRetrieveDocumentSetQuery);

		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());

	}

	@Test
	void submitSimulatorTestCda() throws Exception {
		DocumentMetadata documentMetadata = new DocumentMetadata();
		setMetadataInGeneral(documentMetadata);

		documentMetadata.setUniqueId("2.25.259556660619149366916497015027152147518");
		documentMetadata.setEntryUUID("fdd6a0fc-7686-406e-aefb-db61787e0c20");

		documentMetadata.setTitle("CDA Vaccination Record");
		documentMetadata.setTypeCode(new Code(
				Code.builder().withCode("41000179103").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Immunization record (record artifact)").build()));
		documentMetadata.setClassCode(new Code(
				Code.builder().withCode("184216000").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Patient record type (record artifact)").build()));

		documentMetadata.addConfidentialityCode(new Code(
				Code.builder().withCode("17621005").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Normal (qualifier value)").build()));

		documentMetadata.setFormatCode(new Code(Code.builder().withCode("urn:ihe:pcc:ic:2009")
				.withCodeSystem("1.3.6.1.4.1.19376.1.2.3")
				.withDisplayName("Immunization Content (IC)").build()));

		documentMetadata.setHealthcareFacilityTypeCode(new Code(
				Code.builder().withCode("264358009").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("General practice premises (environment)").build()));
		documentMetadata.setPracticeSettingCode(new Code(
				Code.builder().withCode("394802001").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("General medicine (qualifier value)").build()));

		Identificator patientId = new Identificator("2.16.756.5.30.1.99999.1000", "HUSKY-201-CDA");
		documentMetadata.setDestinationPatientId(patientId);
		documentMetadata.setSourcePatientId(new Identificator("1.2.3.4", "HUSKY-201-CDA"));

		XdsDocumentWithMetadata.XdsDocumentWithMetadataBuilder builder = this.huskyService
				.createDocumentWithMetadata();
		XdsDocumentWithMetadata documentWithMetadata = builder.documentMetadata(documentMetadata)
				.dataStream(getResourceAsStream("/docConsumer/CDA-CH-VACD_Impfausweis_V2.xml"))
				.documentDescriptor(DocumentDescriptor.CDA_R2).build();

		SubmissionSetMetadata subSet = new SubmissionSetMetadata();
		setSubmissionMetadata(subSet, patientId, "CDA Vaccination Record");

		XdsProvideAndRetrieveDocumentSetQuery.XdsProvideAndRetrieveDocumentSetQueryBuilder documentSetQueryBuilder = this.huskyService
				.createProvideAndRetrieveDocumentSetQuery();
		XdsProvideAndRetrieveDocumentSetQuery provideAndRetrieveDocumentSetQuery = documentSetQueryBuilder
				.documentWithMetadata(List.of(documentWithMetadata))
				.destination(createDestination()).submissionSetMetadata(subSet).build();

		// submit added documents
		Response response = huskyService.send(provideAndRetrieveDocumentSetQuery);

		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
	}

	@Test
	void submitSimulatorTestFhir() throws Exception {
		DocumentMetadata documentMetadata = new DocumentMetadata();
		setMetadataInGeneral(documentMetadata);

		documentMetadata.setUniqueId("2.25.214777870628014861879219079172344039193");
		documentMetadata.setEntryUUID("5ecb7fe0-7565-4acb-ac96-1c48e1ccc641");

		documentMetadata.setTitle("CH-VACD Vaccination Record");
		documentMetadata.setTypeCode(new Code(
				Code.builder().withCode("41000179103").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Immunization record (record artifact)").build()));
		documentMetadata.setClassCode(new Code(
				Code.builder().withCode("184216000").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Patient record type (record artifact)").build()));

		documentMetadata.addConfidentialityCode(new Code(
				Code.builder().withCode("17621005").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Normal (qualifier value)").build()));

		documentMetadata.setFormatCode(
				new Code(Code.builder().withCode("urn:che:epr:ch-vacd:vaccination-record:2022")
						.withCodeSystem("2.16.756.5.30.1.127.3.10.10")
						.withDisplayName("CH VACD Vaccination Record").build()));

		documentMetadata.setHealthcareFacilityTypeCode(new Code(
				Code.builder().withCode("264358009").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("General practice premises (environment)").build()));
		documentMetadata.setPracticeSettingCode(new Code(
				Code.builder().withCode("394802001").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("General medicine (qualifier value)").build()));

		Identificator patientId = new Identificator("2.16.756.5.30.1.99999.1000", "HUSKY-203-FHI");
		documentMetadata.setDestinationPatientId(patientId);
		documentMetadata.setSourcePatientId(new Identificator("1.2.3.4", "HUSKY-203-FHI"));

		XdsDocumentWithMetadata.XdsDocumentWithMetadataBuilder builder = this.huskyService
				.createDocumentWithMetadata();
		XdsDocumentWithMetadata documentWithMetadata = builder.documentMetadata(documentMetadata)
				.dataStream(getResourceAsStream("/docSource/Bundle-1-3-VaccinationRecord.json"))
				.documentDescriptor(DocumentDescriptor.FHIR_JSON).build();

		SubmissionSetMetadata subSet = new SubmissionSetMetadata();
		setSubmissionMetadata(subSet, patientId, "CH-VACD Vaccination Record");

		XdsProvideAndRetrieveDocumentSetQuery.XdsProvideAndRetrieveDocumentSetQueryBuilder documentSetQueryBuilder = this.huskyService
				.createProvideAndRetrieveDocumentSetQuery();
		XdsProvideAndRetrieveDocumentSetQuery provideAndRetrieveDocumentSetQuery = documentSetQueryBuilder
				.documentWithMetadata(List.of(documentWithMetadata))
				.destination(createDestination()).submissionSetMetadata(subSet).build();

		// submit added documents
		Response response = huskyService.send(provideAndRetrieveDocumentSetQuery);

		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
	}

	@Test
	void submitSimulatorTestPDF() throws Exception {
		DocumentMetadata documentMetadata = new DocumentMetadata();
		setMetadataInGeneral(documentMetadata);

		documentMetadata.setUniqueId("2.25.14253846181530789730333135870476612181");
		documentMetadata.setEntryUUID("687f3d19-d5d1-4c67-8e46-51f6ecae8a8f");

		documentMetadata.setTitle("Normal PDF Document");
		documentMetadata.setTypeCode(new Code(
				Code.builder().withCode("772786005").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Medical certificate (record artifact)").build()));
		documentMetadata.setClassCode(new Code(Code.builder().withCode("405624007")
				.withCodeSystem("2.16.840.1.113883.6.96")
				.withDisplayName("Administrative documentation (record artifact)").build()));

		documentMetadata.addConfidentialityCode(new Code(
				Code.builder().withCode("17621005").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("Normal (qualifier value)").build()));

		documentMetadata.setFormatCode(
				new Code(Code.builder().withCode("urn:ihe:iti:xds:2017:mimeTypeSufficient")
						.withCodeSystem("1.3.6.1.4.1.19376.1.2.3")
						.withDisplayName("MimeType sufficient").build()));

		documentMetadata.setHealthcareFacilityTypeCode(new Code(
				Code.builder().withCode("264358009").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("General practice premises (environment)").build()));
		documentMetadata.setPracticeSettingCode(new Code(
				Code.builder().withCode("394802001").withCodeSystem("2.16.840.1.113883.6.96")
						.withDisplayName("General medicine (qualifier value)").build()));

		Identificator patientId = new Identificator("2.16.756.5.30.1.99999.1000", "HUSKY-202-PDF");
		documentMetadata.setDestinationPatientId(patientId);
		documentMetadata.setSourcePatientId(new Identificator("1.2.3.4", "HUSKY-202-PDF"));

		XdsDocumentWithMetadata.XdsDocumentWithMetadataBuilder builder = this.huskyService
				.createDocumentWithMetadata();
		XdsDocumentWithMetadata documentWithMetadata = builder.documentMetadata(documentMetadata)
				.dataStream(getResourceAsStream("/docSource/PDFDocument.pdf"))
				.documentDescriptor(DocumentDescriptor.FHIR_JSON).build();

		SubmissionSetMetadata subSet = new SubmissionSetMetadata();
		setSubmissionMetadata(subSet, patientId, "Normal PDF Document");

		XdsProvideAndRetrieveDocumentSetQuery.XdsProvideAndRetrieveDocumentSetQueryBuilder documentSetQueryBuilder = this.huskyService
				.createProvideAndRetrieveDocumentSetQuery();
		XdsProvideAndRetrieveDocumentSetQuery provideAndRetrieveDocumentSetQuery = documentSetQueryBuilder
				.documentWithMetadata(List.of(documentWithMetadata))
				.destination(createDestination()).submissionSetMetadata(subSet).build();

		// submit added documents
		Response response = huskyService.send(provideAndRetrieveDocumentSetQuery);

		assertTrue(response.getErrors().isEmpty());
		assertEquals(Status.SUCCESS, response.getStatus());
	}

	// @Test
	// public void test() {
	// System.out.println(OidGenerator.uniqueOid().toString());
	// System.out.println(UUID.randomUUID().toString());
	// System.out.println("--");
	// System.out.println(OidGenerator.uniqueOid().toString());
	// System.out.println(UUID.randomUUID().toString());
	// System.out.println("--");
	// System.out.println(OidGenerator.uniqueOid().toString());
	// System.out.println(UUID.randomUUID().toString());
	// System.out.println("--");
	// System.out.println(OidGenerator.uniqueOid().toString());
	// System.out.println(UUID.randomUUID().toString());
	// System.out.println("--");
	//
	// }

	private InputStream getResourceAsStream(String resourcePath) {
		return this.getClass().getResourceAsStream(resourcePath);
	}

	protected void setSubmissionMetadata(SubmissionSetMetadata metadata, Identificator patientId,
			String commentText) {
		metadata.getAuthor().add(authorPerson);
		metadata.setUniqueId(OidGenerator.uniqueOid().toString());
		metadata.setSourceId(EhcVersions.getCurrentVersion().getOid());
		metadata.setEntryUUID(UUID.randomUUID().toString());
		metadata.setDestinationPatientId(patientId);
		metadata.setComments(commentText);
		metadata.setContentTypeCode(
				new Code("71388002", "2.16.840.1.113883.6.96", "Procedure (procedure)"));
	}

	private Destination createDestination() {
		final Destination dest = new Destination();

		try {
			dest.setUri(new URI(pnrUri));
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		dest.setSenderApplicationOid(senderApplicationOid);
		dest.setReceiverApplicationOid(applicationName);
		dest.setReceiverFacilityOid(facilityName);
		return dest;
	}

}
