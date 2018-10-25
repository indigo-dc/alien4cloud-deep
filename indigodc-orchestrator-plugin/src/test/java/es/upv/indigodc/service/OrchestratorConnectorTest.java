package es.upv.indigodc.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;

import alien4cloud.security.model.User;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.upv.indigodc.IndigoDcOrchestratorFactory;
import es.upv.indigodc.TestBlockingServlet;
import es.upv.indigodc.TestServer;
import es.upv.indigodc.TestUtil;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.BuilderService.Deployment;
import es.upv.indigodc.service.OrchestratorConnector.AccessToken;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;


@Slf4j
public class OrchestratorConnectorTest {


  public static final String TOKEN_AUTH_RESPONSE =
      "{\"expires_in\": 1000000, \"access_token\": \"none\"}";
  public static final String ORCHESTRATOR_COMMON_PATH = "/orchestrator";

  public TestServer getTestServer() throws Exception {
    return new TestServer(
            18080,
            18443,
            OrchestratorConnectorTest.class.getClassLoader().getResource("keystore").getPath(),
            "alien4cloud",
            "alien4cloud");
    //      Map<String, TestBlockingServlet> servlets = new HashMap<>();
    //      servlets.put("/", new TestBlockingServlet());
    // testServer.start();
  }


  @Disabled("Test is ignored because it overstretches the orchestrator")
  @Test
  public void testLoginWithProductionInfo()
      throws JsonParseException, JsonMappingException, IOException, NoSuchFieldException,
          OrchestratorIamException {
    OrchestratorConnector oc = new OrchestratorConnector();
    CloudConfiguration cc = TestUtil.getRealConfiguration(null);

    User user = TestUtil.getTestUser();
    AccessToken at = oc.obtainAuthTokens(cc, user.getUsername(), user.getPassword());
    Assertions.assertEquals(true, at.getAccessToken() != null);
  }

  @Disabled("Test is ignored because it overstretches the orchestrator")
  @Test
  public void deployUndeployProductionAuthSingleToscaCompute()
      throws JsonParseException, JsonMappingException, IOException, NoSuchFieldException,
      OrchestratorIamException {
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
    User user = TestUtil.getTestUser();
    OrchestratorResponse or = oc.callDeploy(cc, user.getUsername(), user.getPassword(), callJson);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
    JsonNode root = objectMapper.readTree(or.getResponse().toString());
    List<JsonNode> vals = root.findValues("uuid");
    Assertions.assertEquals(true, vals.size() >= 1);
    String uuid = vals.get(0).asText();
    // log.info("UUID of the app: " + uuid);
    vals = root.findValues("status");
    Assertions.assertEquals(true, vals.size() >= 1);
    vals = root.findValues("callback");
    Assertions.assertEquals(true, vals.size() >= 1);

    // now let us see the status
    or = oc.callDeploymentStatus(cc, user.getUsername(), user.getPassword(), uuid);
    log.info("Deployment status is: " + or.getResponse().toString());

    // now let us undeploy
    or = oc.callUndeploy(cc, user.getUsername(), user.getPassword(), uuid);
    Assertions.assertEquals(204, or.getCode());

    // now let us see the status
    or = oc.callDeploymentStatus(cc, user.getUsername(), user.getPassword(), uuid);
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
            om.writeValueAsString(
                new OrchestratorResponse(1, HttpMethod.GET, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = new OrchestratorConnector();
    User user = TestUtil.getTestUser();
    OrchestratorResponse or =
        oc.callDeploy(cc, user.getUsername(), user.getPassword(), "{\"response\": 1}");
    testServer.stop();
  }
  
  @Test
  public void testDeployWithTestServerUnsecure() throws Exception {
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
            om.writeValueAsString(
                new OrchestratorResponse(1, HttpMethod.GET, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test_unsecured.json");
    OrchestratorConnector oc = new OrchestratorConnector();
    User user = TestUtil.getTestUser();
    oc.callDeploy(cc, user.getUsername(), user.getPassword(), "{\"response\": 1}");
    testServer.stop();
  }
  
  @Test
  public void testDeploymentStatus() throws Exception {
    Map<String, TestBlockingServlet> servlets = new HashMap<>();
    ObjectMapper om = new ObjectMapper();
    servlets.put(
        "/token",
        new TestBlockingServlet(TestBlockingServlet.CONTENT_TYPE_JSON, 200, TOKEN_AUTH_RESPONSE));
    servlets.put(
        ORCHESTRATOR_COMMON_PATH + OrchestratorConnector.WS_PATH_DEPLOYMENTS + "/id",
        new TestBlockingServlet(
            TestBlockingServlet.CONTENT_TYPE_JSON,
            200,
            om.writeValueAsString(
                new OrchestratorResponse(1, HttpMethod.GET, new StringBuilder("{\"status\": \"ok\"}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = new OrchestratorConnector();
    User user = TestUtil.getTestUser();
    OrchestratorResponse or  = oc.callDeploymentStatus(cc, user.getName(), user.getPassword(), "id");
    testServer.stop();
  }
  
  @Test
  public void testGetDeploymentsWithTestServer404Response() throws Exception {
    Map<String, TestBlockingServlet> servlets = new HashMap<>();
    ObjectMapper om = new ObjectMapper();
    servlets.put(
        "/token",
        new TestBlockingServlet(TestBlockingServlet.CONTENT_TYPE_JSON, 200, TOKEN_AUTH_RESPONSE));
    servlets.put(
        ORCHESTRATOR_COMMON_PATH + OrchestratorConnector.WS_PATH_DEPLOYMENTS,
        new TestBlockingServlet(
            TestBlockingServlet.CONTENT_TYPE_JSON,
            404,
            om.writeValueAsString(
                new OrchestratorResponse(404, HttpMethod.GET, new StringBuilder("{\"status\": \"ok\"}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = new OrchestratorConnector();
    User user = TestUtil.getTestUser();
    Executable callGetDeployments = () -> {oc.callGetDeployments(cc, user.getName(), user.getPassword());};
    Assertions.assertThrows(OrchestratorIamException.class, callGetDeployments);
    testServer.stop();
  }
  
  @Test
  public void testGetDeploymentsWithTestServer() throws Exception {
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
            om.writeValueAsString(
                new OrchestratorResponse(200, HttpMethod.GET, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = new OrchestratorConnector();
    User user = TestUtil.getTestUser();
    OrchestratorResponse or =
        oc.callGetDeployments(cc, user.getName(), user.getPassword());
    testServer.stop();
  }
  
  @Test
  public void testUndeployWithTestServer() throws Exception {
    Map<String, TestBlockingServlet> servlets = new HashMap<>();
    ObjectMapper om = new ObjectMapper();
    servlets.put(
        "/token",
        new TestBlockingServlet(TestBlockingServlet.CONTENT_TYPE_JSON, 200, TOKEN_AUTH_RESPONSE));
    servlets.put(
            ORCHESTRATOR_COMMON_PATH + OrchestratorConnector.WS_PATH_DEPLOYMENTS + "/id",
        new TestBlockingServlet(
            TestBlockingServlet.CONTENT_TYPE_JSON,
            200,
            om.writeValueAsString(
                new OrchestratorResponse(200, HttpMethod.DELETE, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = new OrchestratorConnector();
    User user = TestUtil.getTestUser();
    OrchestratorResponse or =
        oc.callUndeploy(cc, user.getName(), user.getPassword(), "id");
    testServer.stop();
  }
  
  @Test
  public void testUndeployWithTestServerNotSecure() throws Exception {
    Map<String, TestBlockingServlet> servlets = new HashMap<>();
    ObjectMapper om = new ObjectMapper();
    servlets.put(
        "/token",
        new TestBlockingServlet(TestBlockingServlet.CONTENT_TYPE_JSON, 200, TOKEN_AUTH_RESPONSE));
    servlets.put(
            ORCHESTRATOR_COMMON_PATH + OrchestratorConnector.WS_PATH_DEPLOYMENTS + "/id",
        new TestBlockingServlet(
            TestBlockingServlet.CONTENT_TYPE_JSON,
            200,
            om.writeValueAsString(
                new OrchestratorResponse(200, HttpMethod.DELETE, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test_unsecured.json");
    OrchestratorConnector oc = new OrchestratorConnector();
    User user = TestUtil.getTestUser();
    OrchestratorResponse or =
        oc.callUndeploy(cc, user.getName(), user.getPassword(), "id");
    testServer.stop();
  }
  
  @Test
  public void testUndeployWithTestServer404NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(404);
  }
  
  @Test
  public void testUndeployWithTestServer101NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(101);
  }
  
  @Test
  public void testUndeployWithTestServer301NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(301);
  }
  
  @Test
  public void testUndeployWithTestServer501NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(501);
  }
  
  @Test
  public void testUndeployWithTestServer601NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(601);
  }
  
  @Disabled
  protected void testUndeployDifferentHttpCodesUnsecured(int code) throws Exception {
	    Map<String, TestBlockingServlet> servlets = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    servlets.put(
	        "/token",
	        new TestBlockingServlet(TestBlockingServlet.CONTENT_TYPE_JSON, 200, TOKEN_AUTH_RESPONSE));
	    servlets.put(
	            ORCHESTRATOR_COMMON_PATH + OrchestratorConnector.WS_PATH_DEPLOYMENTS + "/id",
	        new TestBlockingServlet(
	            TestBlockingServlet.CONTENT_TYPE_JSON,
	            code,
	            om.writeValueAsString(
	                new OrchestratorResponse(code, HttpMethod.DELETE, new StringBuilder("{}")))));
	    TestServer testServer = getTestServer();
	    testServer.start(servlets);
	    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test_unsecured.json");
	    OrchestratorConnector oc = new OrchestratorConnector();
	    User user = TestUtil.getTestUser();
	    Executable callUndeploy = () -> oc.callUndeploy(cc, user.getName(), user.getPassword(), "id");
	    Assertions.assertThrows(OrchestratorIamException.class, callUndeploy);
	    testServer.stop();
  }
  
}
