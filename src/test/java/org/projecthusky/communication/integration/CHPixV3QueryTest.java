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

import org.hl7.fhir.r4.model.Identifier;
import org.projecthusky.common.communication.AffinityDomain;
import org.projecthusky.common.communication.Destination;
import org.projecthusky.communication.ConvenienceMasterPatientIndexV3;
import org.projecthusky.communication.mpi.impl.PixV3Query;
import org.projecthusky.communication.testhelper.TestApplication;
import org.projecthusky.fhir.structures.gen.FhirCommon;
import org.projecthusky.fhir.structures.gen.FhirPatient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
@ExtendWith(value = SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TestApplication.class})
@EnableAutoConfiguration
class CHPixV3QueryTest {

    protected static Logger LOGGER = LoggerFactory.getLogger(CHPixV3QueryTest.class);

    @Autowired
    private ConvenienceMasterPatientIndexV3 convenienceMasterPatientIndexV3Client;

    @Value(value = "${test.pixq.uri:https://ehealthsuisse.ihe-europe.net/PAMSimulator-ejb/PIXManager_Service/PIXManager_PortType}")
    private String pixUri;

    final String facilityOid =  "2.16.840.1.113883.3.72.6.1";
    final String receiverApplicationOid = "1.3.6.1.4.1.12559.11.20.1.10";
    final String senderApplicationOid = "1.2.3.4.123456";

    // The local patient ID as registered in the PIX Feed transaction. The local id is the
    // id used in the client system (portal, primary system, etc.) to identify the patient locally.
    final String localAssigningAuthorityOid = "1.2.3.4.123456.1";
    final String localIdNamespace = "WALDSPITAL";
    final String localPatientId = "waldspital-Id-1234";

    // The Swiss unique patient identifier as expected from the PIX Query response.
    // The SPID is required as claim in the Get X-User Assertion transaction.
    final String spidAssigningAuthorityOid = "2.16.756.5.30.1.127.3.10.3";
    final String spidNamespace = "SPID";
    final String eprSpid = "761337713436974989"; // expected to be returned in test

    // the global patient ID generated by the community (required as destinationId in transactions)
    final String globalAssigningAuthorityOid = "1.3.6.1.4.1.12559.11.20.1";
    final String globalIdNamespace = "CHPAM2"; // expected to be returned in test
    final String globalPatientId = "IHE-12361761818786818818"; // expected to be returned in test. Needs to be adjusted to the value from community PIX Manager

    /**
     * @throws Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        var app = new SpringApplication(TestApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run();
    }

    /**
     *
     */
    @Test
    void queryTest() {

        final AffinityDomain affinityDomain = new AffinityDomain();

        final Destination dest = new Destination();
        dest.setUri(URI.create(pixUri));
        dest.setSenderApplicationOid(senderApplicationOid);
        dest.setReceiverApplicationOid(receiverApplicationOid);
        dest.setReceiverFacilityOid(facilityOid);
        affinityDomain.setPdqDestination(dest);
        affinityDomain.setPixDestination(dest);

        PixV3Query pixV3Query = new PixV3Query(affinityDomain, localAssigningAuthorityOid, localIdNamespace,
                spidAssigningAuthorityOid, spidNamespace,
                convenienceMasterPatientIndexV3Client.getContext(),
                convenienceMasterPatientIndexV3Client.getAuditContext());

        // set the local patient id as input for the pix query
        final Identifier localIdentifier = new Identifier();
        localIdentifier.setValue(localPatientId);
        localIdentifier.setSystem(FhirCommon.addUrnOid(localAssigningAuthorityOid));

        final FhirPatient patient = new FhirPatient();
        patient.getIdentifier().add(localIdentifier);

        // data source settings. By setting the assigning authority oid we tell the PIX Manager to return
        // the patient Id assigned by the authority. The query should return the patient identifiers
        // in the order the assigning authority oids are added.
        List<String> queryDomainOids = new ArrayList();
        queryDomainOids.add(spidAssigningAuthorityOid);
        queryDomainOids.add(globalAssigningAuthorityOid);

        List<String> returnedIds = pixV3Query.queryPatientId(patient, queryDomainOids, null, null, null);

        assertTrue(returnedIds.size() > 0);

        assertEquals(returnedIds.get(0), eprSpid);

        // In the Swiss EPR the PIX Manager should return the ids in the order set in the queryDomainIds, but does not so in
        // gazelle test system.
        // assertEquals(returnedIds.get(1), communityId);
    }

}
