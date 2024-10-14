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
package org.projecthusky.communication.services.pix;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.hl7v3.core.metadata.Device;
import org.openehealth.ipf.commons.ihe.hl7v3.core.requests.PixV3QueryRequest;
import org.projecthusky.common.communication.Destination;
import org.projecthusky.communication.requests.pix.PixPatientIDQuery;
import org.projecthusky.communication.responses.pix.PixPatientIDResult;
import org.projecthusky.communication.testhelper.TestApplication;
import org.projecthusky.communication.services.HuskyService;
import org.projecthusky.communication.testhelper.IpfApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import net.ihe.gazelle.hl7v3.datatypes.II;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TestApplication.class,
    IpfApplicationConfig.class})
@ActiveProfiles("atna")
public class PixQueryPatientIDTest {

  @Autowired
  private HuskyService service;

  @Value(value = "${test.pixq.uri:https://ehealthsuisse.ihe-europe.net/PAMSimulator-ejb/PIXManager_Service/PIXManager_PortType}")
  private String pixUri;

  @Autowired
  private CamelContext camelContext;

  @Test
  void test1() throws Exception {
    Destination destination = new Destination();
    destination.setUri(URI.create(pixUri));
    destination.setSenderApplicationOid("1.2.3.4");
    destination.setReceiverApplicationOid("1.3.6.1.4.1.12559.11.20.1.10");

    PixPatientIDQuery query = this.service.createPixPatientIDQuery(destination)
        .homeCommunityPatientOid("waldspital-Id-1234").homeCommunityOid("2.16.756.5.30.1.127.3.10.3")
        .homeCommunityNamespace("WALDSPITAL")
        .build();
    PixPatientIDResult result = this.service.send(query);
    assertNotNull(result);
  }
  
  @Test
  public void testPixV3QueryRequestTranslation() throws Exception {	 
	  
	  
	  camelContext.setDebugging(true);
	  camelContext.setTracing(true);
	  
      PixV3QueryRequest request = new PixV3QueryRequest();
      request.setReceiver(new Device());
      request.setSender(new Device());
      request.setQueryPatientId(new II("patient1", "3.14.15.926"));

      Exchange exchange = new DefaultExchange(camelContext);
      exchange.getIn( ).setBody(request);

      String s = exchange.getMessage().getMandatoryBody(String.class);
      assertTrue(s.startsWith("<"));
  }

}
