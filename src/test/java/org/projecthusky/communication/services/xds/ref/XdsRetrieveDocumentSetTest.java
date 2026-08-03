package org.projecthusky.communication.services.xds.ref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.xds.core.responses.RetrievedDocument;
import org.openehealth.ipf.commons.ihe.xds.core.responses.RetrievedDocumentSet;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status;
import org.projecthusky.common.communication.Destination;
import org.projecthusky.communication.DocumentRequest;
import org.projecthusky.communication.requests.xds.XdsDocumentSetRequest;
import org.projecthusky.communication.services.HuskyService;
import org.projecthusky.communication.testhelper.IpfApplicationConfig;
import org.projecthusky.communication.testhelper.TestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
		TestApplication.class, IpfApplicationConfig.class })
public class XdsRetrieveDocumentSetTest {

	@Autowired
	private HuskyService service;

	@Value(value = "${test.xds.ret.uri:http://localhost:9988/xdstools7.12.0/sim/project_husky__integration_test/rep/ret}")
	private String repositoryURL;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void retrieveDocuments_Scanned_and_Fhir() throws Exception {
		String applicationOid = "2.16.840.1.113883.3.72.6.5.100.1399";
		String facilityOid = "Waldpsital Bern";
		String senderApplicationOid = "1.2.3.4";

		final String documentId_1 = "2.25.156817188312541163954803632376225384899";
		final String repositoryId_1 = "1.1.4567332.1.1";
		final String homeCommunityId_1 = "urn:oid:1.1.4567334.1.6";

		final String documentId_2 = "2.25.214777870628014861879219079172344039193";
		final String repositoryId_2 = "1.1.4567332.1.1";
		final String homeCommunityId_2 = "urn:oid:1.1.4567334.1.6";

		Destination dest = new Destination();
		dest.setUri(URI.create(repositoryURL));
		dest.setSenderApplicationOid(senderApplicationOid);
		dest.setReceiverApplicationOid(applicationOid);
		dest.setReceiverFacilityOid(facilityOid);

		XdsDocumentSetRequest request = XdsDocumentSetRequest.builder().destination(dest)
				.documentRequest(
						new DocumentRequest(repositoryId_1, null, documentId_1, homeCommunityId_1))
				.documentRequest(
						new DocumentRequest(repositoryId_2, null, documentId_2, homeCommunityId_2))
				.build();

		RetrievedDocumentSet set = this.service.send(request);
		assertEquals(Status.SUCCESS, set.getStatus());
		assertTrue(set.getErrors().isEmpty());

		// check if document is returned
		assertFalse(set.getDocuments().isEmpty());
		assertEquals(2, set.getDocuments().size());

		for (RetrievedDocument retrievedDocument : set.getDocuments()) {

			// check mime type
			assertTrue("application/fhir+json".equals(retrievedDocument.getMimeType())
					|| "application/pdf".equals(retrievedDocument.getMimeType()));

			// check for content
			try (var is = retrievedDocument.getDataHandler().getInputStream()) {
				byte[] bytesOfDocument = is.readAllBytes();
				assertNotNull(bytesOfDocument);
			}
		}
	}

}
