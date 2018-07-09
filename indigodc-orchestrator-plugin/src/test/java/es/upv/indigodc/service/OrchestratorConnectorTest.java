package es.upv.indigodc.service;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.upv.indigodc.IndigoDCOrchestratorFactory;
import es.upv.indigodc.TestBlockingServlet;
import es.upv.indigodc.TestServer;
import es.upv.indigodc.TestUtil;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.BuilderService.Deployment;
import es.upv.indigodc.service.OrchestratorConnector.AccessToken;
import es.upv.indigodc.service.model.OrchestratorIAMException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrchestratorConnectorTest {

  protected TestServer testServer;

  public static final String TOKEN_AUTH_RESPONSE =
      "{\"expires_in\": 1000000, \"access_token\": \"none\"}";
  public static final String ORCHESTRATOR_COMMON_PATH = "/orchestrator";

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() throws Exception {
    testServer =
        new TestServer(
            18080,
            18443,
            OrchestratorConnectorTest.class.getClassLoader().getResource("keystore").getPath(),
            "alien4cloud",
            "alien4cloud");
    //      Map<String, TestBlockingServlet> servlets = new HashMap<>();
    //      servlets.put("/", new TestBlockingServlet());
    // testServer.start();
  }

  @After
  public void end() throws Exception {
    if (testServer.isStarted()) testServer.stop();
  }

  @Ignore("Test is ignored because it overstretches the orchestrator")
  @Test
  public void testLoginWithProductionInfo()
      throws JsonParseException, JsonMappingException, IOException, NoSuchFieldException,
          OrchestratorIAMException {
    OrchestratorConnector oc = new OrchestratorConnector();
    CloudConfiguration cc = TestUtil.getRealConfiguration(null);
    AccessToken at = oc.obtainAuthTokens(cc);
    assertEquals(true, at.getAccessToken() != null);
  }

  @Ignore("Test is ignored because it overstretches the orchestrator")
  @Test
  public void deployUndeployProductionAuthSingleToscaCompute()
      throws JsonParseException, JsonMappingException, IOException, NoSuchFieldException,
          OrchestratorIAMException {
    OrchestratorConnector oc = new OrchestratorConnector();

    URL url =
        OrchestratorConnectorTest.class.getClassLoader().getResource("test_compute_indigodc.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    ObjectMapper mapper = new ObjectMapper();
    Deployment d = new Deployment();
    d.setParameters(new HashMap<String, Object>());
    d.setCallback("http://localhost:8080/callback");
    d.setTemplate(yamlIndigoDC);
    String callJson =
        mapper.writeValueAsString(d).replaceAll("\\\\n", "\\n").replace("\\\\\\", "\\");

    // String callJson =
    // String.format("{\"template\":\"%s\",\"parameters\":{\"cpus\":1},\"callback\":\"http://localhost:8080/callback\"}", yamlIndigoDC);
    // log.info("call to be sent to the orchestrator: \n" + callJson);
    CloudConfiguration cc = TestUtil.getRealConfiguration(null);
    OrchestratorResponse or = oc.callDeploy(cc, callJson);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
    JsonNode root = objectMapper.readTree(or.getResponse().toString());
    List<JsonNode> vals = root.findValues("uuid");
    assertEquals(true, vals.size() >= 1);
    String uuid = vals.get(0).asText();
    // log.info("UUID of the app: " + uuid);
    vals = root.findValues("status");
    assertEquals(true, vals.size() >= 1);
    vals = root.findValues("callback");
    assertEquals(true, vals.size() >= 1);

    // now let us see the status
    or = oc.callDeploymentStatus(cc, uuid);
    log.info("Deployment status is: " + or.getResponse().toString());

    // now let us undeploy
    or = oc.callUndeploy(cc, uuid);
    assertEquals(204, or.getCode());

    // now let us see the status
    or = oc.callDeploymentStatus(cc, uuid);
    log.info("Deployment status is: " + or.getResponse().toString());
  }

  @Test
  public void testLoginWithTestServer() throws Exception {
    Map<String, TestBlockingServlet> servlets = new HashMap<>();
    ObjectMapper om = new ObjectMapper();
    servlets.put(
        "/token",
        new TestBlockingServlet(TestBlockingServlet.CONTENT_TYPE_JSON, 200, TOKEN_AUTH_RESPONSE));
    servlets.put(
        ORCHESTRATOR_COMMON_PATH + OrchestratorConnector.WS_PATH_DEPLOYMENTS,
        new TestBlockingServlet(
            TestBlockingServlet.CONTENT_TYPE_JSON,
            200,
            om.writeValueAsString(new OrchestratorResponse(1, new StringBuilder("none")))));
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    log.info(cc.getTokenEndpoint());
    OrchestratorConnector oc = new OrchestratorConnector();
    OrchestratorResponse or = oc.callDeploy(cc, "{\"response\": 1}");
    testServer.stop();
  }
}
