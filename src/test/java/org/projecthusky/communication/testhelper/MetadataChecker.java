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
package org.projecthusky.communication.testhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;

/**
 * 
 */
public class MetadataChecker {

	public static void checkScannedDocumentMetadata(DocumentEntry documentEntry) {
		// check if identifiers (unique ID, repository ID and home community ID)
		// are
		// equal
		assertEquals("2.25.156817188312541163954803632376225384899", documentEntry.getUniqueId());
		assertEquals("1.1.4567332.1.1", documentEntry.getRepositoryUniqueId());
		assertEquals("urn:oid:1.1.4567334.1.6", documentEntry.getHomeCommunityId());

		// Entry UUID is different in HUSKY SIM!
		// assertEquals("urn:uuid:e07d9439-e938-4f12-9404-a70984fe6194",
		// documentEntry.getEntryUuid());

		assertEquals(AvailabilityStatus.APPROVED, documentEntry.getAvailabilityStatus());
		assertEquals("application/pdf", documentEntry.getMimeType());

		assertNull(documentEntry.getComments());
		assertNull(documentEntry.getDocumentAvailability());

		assertEquals("Scanned Document", documentEntry.getTitle().getValue());

		// Creation time is different in HUSKY SIM!
		// assertEquals("20260204060254",
		// documentEntry.getCreationTime().toHL7());

		// check different codes
		assertEquals("de-CH", documentEntry.getLanguageCode());

		assertNotNull(documentEntry.getClassCode());
		assertEquals("422735006", documentEntry.getClassCode().getCode());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getClassCode().getSchemeName());
		assertEquals("Summary clinical document (record artifact)",
				documentEntry.getClassCode().getDisplayName().getValue());

		assertEquals("419891008", documentEntry.getTypeCode().getCode());
		assertEquals("Record artifact (record artifact)",
				documentEntry.getTypeCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getTypeCode().getSchemeName());

		assertNotNull(documentEntry.getConfidentialityCodes().get(0));
		assertEquals("17621005", documentEntry.getConfidentialityCodes().get(0).getCode());
		assertEquals("Normal (qualifier value)",
				documentEntry.getConfidentialityCodes().get(0).getDisplayName().getValue());

		assertTrue(documentEntry.getEventCodeList().isEmpty());

		assertEquals("urn:ihe:iti:xds-sd:pdf:2008", documentEntry.getFormatCode().getCode());
		assertEquals("1.3.6.1.4.1.19376.1.2.3", documentEntry.getFormatCode().getSchemeName());
		assertEquals("1.3.6.1.4.1.19376.1.2.20 (Scanned Document)",
				documentEntry.getFormatCode().getDisplayName().getValue());

		assertEquals("394747008", documentEntry.getHealthcareFacilityTypeCode().getCode());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getHealthcareFacilityTypeCode().getSchemeName());
		assertEquals("Health Authority",
				documentEntry.getHealthcareFacilityTypeCode().getDisplayName().getValue());

		assertEquals("394810000", documentEntry.getPracticeSettingCode().getCode());
		assertEquals("Rheumatology (qualifier value)",
				documentEntry.getPracticeSettingCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getPracticeSettingCode().getSchemeName());

		// check patient details
		assertEquals("HUSKY-200-SCD", documentEntry.getPatientId().getId());
		assertEquals("2.16.756.5.30.1.99999.1000",
				documentEntry.getPatientId().getAssigningAuthority().getUniversalId());

		assertEquals("HUSKY-200-SCD", documentEntry.getSourcePatientId().getId());
		assertEquals("1.2.3.4",
				documentEntry.getSourcePatientId().getAssigningAuthority().getUniversalId());

		// check author details
		assertFalse(documentEntry.getAuthors().isEmpty());
		assertNotNull(documentEntry.getAuthors().get(0));
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson().getName());
		assertEquals("Bereit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getFamilyName());
		assertEquals("Allzeit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getGivenName());
		assertEquals("Dr.",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getPrefix());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole().get(0));
		assertEquals("HCP", documentEntry.getAuthors().get(0).getAuthorRole().get(0).getId());
		assertEquals("2.16.756.5.30.1.127.3.10.1.1.3", documentEntry.getAuthors().get(0)
				.getAuthorRole().get(0).getAssigningAuthority().getUniversalId());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty());
		assertTrue(documentEntry.getAuthors().get(0).getAuthorSpecialty().isEmpty());
		// assertNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0).getId());
		// assertNull(documentEntry.getAuthors().get(0).getAuthorSpecialty().get(0)
		// .getAssigningAuthority().getUniversalId());

	}

	public static void checkFhirDocumentMetadata(DocumentEntry documentEntry) {
		assertEquals("2.25.214777870628014861879219079172344039193", documentEntry.getUniqueId());
		assertEquals("1.1.4567332.1.1", documentEntry.getRepositoryUniqueId());
		assertEquals("urn:oid:1.1.4567334.1.6", documentEntry.getHomeCommunityId());

		// Entry UUID is different in HUSKY SIM!
		// assertEquals("urn:uuid:71128e64-a87f-458b-ab85-bb4577628f3d",
		// documentEntry.getEntryUuid());

		assertEquals(AvailabilityStatus.APPROVED, documentEntry.getAvailabilityStatus());
		assertEquals("application/fhir+json", documentEntry.getMimeType());

		assertNull(documentEntry.getComments());
		assertNull(documentEntry.getDocumentAvailability());

		assertEquals("CH-VACD Vaccination Record", documentEntry.getTitle().getValue());

		// Creation time is different in HUSKY SIM!
		// assertEquals("20260204060252",
		// documentEntry.getCreationTime().toHL7());

		// check different codes
		assertEquals("de-CH", documentEntry.getLanguageCode());

		assertNotNull(documentEntry.getClassCode());
		assertEquals("184216000", documentEntry.getClassCode().getCode());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getClassCode().getSchemeName());
		assertEquals("Patient record type (record artifact)",
				documentEntry.getClassCode().getDisplayName().getValue());

		assertEquals("41000179103", documentEntry.getTypeCode().getCode());
		assertEquals("Immunization record (record artifact)",
				documentEntry.getTypeCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getTypeCode().getSchemeName());

		assertNotNull(documentEntry.getConfidentialityCodes().get(0));
		assertEquals("17621005", documentEntry.getConfidentialityCodes().get(0).getCode());
		assertEquals("Normal (qualifier value)",
				documentEntry.getConfidentialityCodes().get(0).getDisplayName().getValue());

		assertTrue(documentEntry.getEventCodeList().isEmpty());

		assertEquals("urn:che:epr:ch-vacd:vaccination-record:2022",
				documentEntry.getFormatCode().getCode());
		assertEquals("2.16.756.5.30.1.127.3.10.10", documentEntry.getFormatCode().getSchemeName());
		assertEquals("CH VACD Vaccination Record",
				documentEntry.getFormatCode().getDisplayName().getValue());

		assertEquals("264358009", documentEntry.getHealthcareFacilityTypeCode().getCode());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getHealthcareFacilityTypeCode().getSchemeName());
		assertEquals("General practice premises (environment)",
				documentEntry.getHealthcareFacilityTypeCode().getDisplayName().getValue());

		assertEquals("394802001", documentEntry.getPracticeSettingCode().getCode());
		assertEquals("General medicine (qualifier value)",
				documentEntry.getPracticeSettingCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getPracticeSettingCode().getSchemeName());

		// check patient details
		assertEquals("HUSKY-203-FHI", documentEntry.getPatientId().getId());
		assertEquals("2.16.756.5.30.1.99999.1000",
				documentEntry.getPatientId().getAssigningAuthority().getUniversalId());

		assertEquals("HUSKY-203-FHI", documentEntry.getSourcePatientId().getId());
		assertEquals("1.2.3.4",
				documentEntry.getSourcePatientId().getAssigningAuthority().getUniversalId());

		// check author details
		assertFalse(documentEntry.getAuthors().isEmpty());
		assertNotNull(documentEntry.getAuthors().get(0));
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson().getName());
		assertEquals("Bereit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getFamilyName());
		assertEquals("Allzeit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getGivenName());
		assertEquals("Dr.",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getPrefix());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole().get(0));
		assertEquals("HCP", documentEntry.getAuthors().get(0).getAuthorRole().get(0).getId());
		assertEquals("2.16.756.5.30.1.127.3.10.1.1.3", documentEntry.getAuthors().get(0)
				.getAuthorRole().get(0).getAssigningAuthority().getUniversalId());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty());
		assertTrue(documentEntry.getAuthors().get(0).getAuthorSpecialty().isEmpty());
	}

	public static void checkCdaDocumentMetadata(DocumentEntry documentEntry) {
		assertEquals("2.25.259556660619149366916497015027152147518", documentEntry.getUniqueId());
		assertEquals("1.1.4567332.1.1", documentEntry.getRepositoryUniqueId());
		assertEquals("urn:oid:1.1.4567334.1.6", documentEntry.getHomeCommunityId());

		// Entry UUID is different in HUSKY SIM!
		// assertEquals("urn:uuid:79981799-f378-437f-9e64-c5fd07cb59c1",
		// documentEntry.getEntryUuid());

		assertEquals(AvailabilityStatus.APPROVED, documentEntry.getAvailabilityStatus());
		assertEquals("text/xml", documentEntry.getMimeType());

		assertNull(documentEntry.getComments());
		assertNull(documentEntry.getDocumentAvailability());

		assertEquals("CDA Vaccination Record", documentEntry.getTitle().getValue());

		// Creation time is different in HUSKY SIM!
		// assertEquals("20260204060253",
		// documentEntry.getCreationTime().toHL7());

		// check different codes
		assertEquals("de-CH", documentEntry.getLanguageCode());

		assertNotNull(documentEntry.getClassCode());
		assertEquals("184216000", documentEntry.getClassCode().getCode());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getClassCode().getSchemeName());
		assertEquals("Patient record type (record artifact)",
				documentEntry.getClassCode().getDisplayName().getValue());

		assertEquals("41000179103", documentEntry.getTypeCode().getCode());
		assertEquals("Immunization record (record artifact)",
				documentEntry.getTypeCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getTypeCode().getSchemeName());

		assertNotNull(documentEntry.getConfidentialityCodes().get(0));
		assertEquals("17621005", documentEntry.getConfidentialityCodes().get(0).getCode());
		assertEquals("Normal (qualifier value)",
				documentEntry.getConfidentialityCodes().get(0).getDisplayName().getValue());

		assertTrue(documentEntry.getEventCodeList().isEmpty());

		assertEquals("urn:ihe:pcc:ic:2009", documentEntry.getFormatCode().getCode());
		assertEquals("1.3.6.1.4.1.19376.1.2.3", documentEntry.getFormatCode().getSchemeName());
		assertEquals("Immunization Content (IC)",
				documentEntry.getFormatCode().getDisplayName().getValue());

		assertEquals("264358009", documentEntry.getHealthcareFacilityTypeCode().getCode());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getHealthcareFacilityTypeCode().getSchemeName());
		assertEquals("General practice premises (environment)",
				documentEntry.getHealthcareFacilityTypeCode().getDisplayName().getValue());

		assertEquals("394802001", documentEntry.getPracticeSettingCode().getCode());
		assertEquals("General medicine (qualifier value)",
				documentEntry.getPracticeSettingCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getPracticeSettingCode().getSchemeName());

		// check patient details
		assertEquals("HUSKY-201-CDA", documentEntry.getPatientId().getId());
		assertEquals("2.16.756.5.30.1.99999.1000",
				documentEntry.getPatientId().getAssigningAuthority().getUniversalId());

		assertEquals("HUSKY-201-CDA", documentEntry.getSourcePatientId().getId());
		assertEquals("1.2.3.4",
				documentEntry.getSourcePatientId().getAssigningAuthority().getUniversalId());

		// check author details
		assertFalse(documentEntry.getAuthors().isEmpty());
		assertNotNull(documentEntry.getAuthors().get(0));
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson().getName());
		assertEquals("Bereit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getFamilyName());
		assertEquals("Allzeit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getGivenName());
		assertEquals("Dr.",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getPrefix());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole().get(0));
		assertEquals("HCP", documentEntry.getAuthors().get(0).getAuthorRole().get(0).getId());
		assertEquals("2.16.756.5.30.1.127.3.10.1.1.3", documentEntry.getAuthors().get(0)
				.getAuthorRole().get(0).getAssigningAuthority().getUniversalId());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty());
		assertTrue(documentEntry.getAuthors().get(0).getAuthorSpecialty().isEmpty());
	}

	public static void checkPdfDocumentMetadata(DocumentEntry documentEntry) {
		assertEquals("2.25.14253846181530789730333135870476612181", documentEntry.getUniqueId());
		assertEquals("1.1.4567332.1.1", documentEntry.getRepositoryUniqueId());
		assertEquals("urn:oid:1.1.4567334.1.6", documentEntry.getHomeCommunityId());

		// Entry UUID is different in HUSKY SIM!
		// assertEquals("urn:uuid:7f4c8ac6-9954-4cfa-af7c-eb3d8f3f732d",
		// documentEntry.getEntryUuid());

		assertEquals(AvailabilityStatus.APPROVED, documentEntry.getAvailabilityStatus());
		assertEquals("application/fhir+json", documentEntry.getMimeType());

		assertNull(documentEntry.getComments());
		assertNull(documentEntry.getDocumentAvailability());

		assertEquals("Normal PDF Document", documentEntry.getTitle().getValue());

		// Creation time is different in HUSKY SIM!
		// assertEquals("20260204060253",
		// documentEntry.getCreationTime().toHL7());

		// check different codes
		assertEquals("de-CH", documentEntry.getLanguageCode());

		assertNotNull(documentEntry.getClassCode());
		assertEquals("405624007", documentEntry.getClassCode().getCode());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getClassCode().getSchemeName());
		assertEquals("Administrative documentation (record artifact)",
				documentEntry.getClassCode().getDisplayName().getValue());

		assertEquals("772786005", documentEntry.getTypeCode().getCode());
		assertEquals("Medical certificate (record artifact)",
				documentEntry.getTypeCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96", documentEntry.getTypeCode().getSchemeName());

		assertNotNull(documentEntry.getConfidentialityCodes().get(0));
		assertEquals("17621005", documentEntry.getConfidentialityCodes().get(0).getCode());
		assertEquals("Normal (qualifier value)",
				documentEntry.getConfidentialityCodes().get(0).getDisplayName().getValue());

		assertTrue(documentEntry.getEventCodeList().isEmpty());

		assertEquals("urn:ihe:iti:xds:2017:mimeTypeSufficient",
				documentEntry.getFormatCode().getCode());
		assertEquals("1.3.6.1.4.1.19376.1.2.3", documentEntry.getFormatCode().getSchemeName());
		assertEquals("MimeType sufficient",
				documentEntry.getFormatCode().getDisplayName().getValue());

		assertEquals("264358009", documentEntry.getHealthcareFacilityTypeCode().getCode());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getHealthcareFacilityTypeCode().getSchemeName());
		assertEquals("General practice premises (environment)",
				documentEntry.getHealthcareFacilityTypeCode().getDisplayName().getValue());

		assertEquals("394802001", documentEntry.getPracticeSettingCode().getCode());
		assertEquals("General medicine (qualifier value)",
				documentEntry.getPracticeSettingCode().getDisplayName().getValue());
		assertEquals("2.16.840.1.113883.6.96",
				documentEntry.getPracticeSettingCode().getSchemeName());

		// check patient details
		assertEquals("HUSKY-202-PDF", documentEntry.getPatientId().getId());
		assertEquals("2.16.756.5.30.1.99999.1000",
				documentEntry.getPatientId().getAssigningAuthority().getUniversalId());

		assertEquals("HUSKY-202-PDF", documentEntry.getSourcePatientId().getId());
		assertEquals("1.2.3.4",
				documentEntry.getSourcePatientId().getAssigningAuthority().getUniversalId());

		// check author details
		assertFalse(documentEntry.getAuthors().isEmpty());
		assertNotNull(documentEntry.getAuthors().get(0));
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorPerson().getName());
		assertEquals("Bereit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getFamilyName());
		assertEquals("Allzeit",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getGivenName());
		assertEquals("Dr.",
				documentEntry.getAuthors().get(0).getAuthorPerson().getName().getPrefix());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole());
		assertNotNull(documentEntry.getAuthors().get(0).getAuthorRole().get(0));
		assertEquals("HCP", documentEntry.getAuthors().get(0).getAuthorRole().get(0).getId());
		assertEquals("2.16.756.5.30.1.127.3.10.1.1.3", documentEntry.getAuthors().get(0)
				.getAuthorRole().get(0).getAssigningAuthority().getUniversalId());

		assertNotNull(documentEntry.getAuthors().get(0).getAuthorSpecialty());
		assertTrue(documentEntry.getAuthors().get(0).getAuthorSpecialty().isEmpty());
	}
	
}
