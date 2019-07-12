package es.upv.indigodc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upv.indigodc.TestBlockingServlet;
import es.upv.indigodc.TestServer;
import es.upv.indigodc.TestUtil;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.social.oidc.deep.api.DeepOrchestrator;
import org.springframework.social.oidc.deep.api.OidcConfiguration;
import org.springframework.social.oidc.deep.api.impl.DeepOrchestratorTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrchestratorConnectorTest {


  public static final String TOKEN_AUTH_RESPONSE =
      "{\"expires_in\": 1000000, \"access_token\": \"none\"}";
  public static final String ORCHESTRATOR_COMMON_PATH = "/orchestrator";
  public static final String OIDC_PROVIDER_ID = "providerId";
  public static final String OIDC_PROVIDER_USER_ID = "providerUserId";
  public static final String OIDC_DISPLAY_NAME ="displayName";
  public static final String OIDC_PROFILE_URL = "profileUrl";
  public static final String OIDC_IMAGE_URL = "imageUrl";
  public static final String OIDC_ACCESS_TOKEN = "accessToken";
  public static final String OIDC_SECRET = "secret";
  public static final String OIDC_REFRESH_TOKEN = "refreshToken";
  public static final Long OIDC_EXPIRE_TIME = 360L;

  public TestServer getTestServer() throws Exception {
    return new TestServer(
            28080,
            28443,
            OrchestratorConnectorTest.class.getClassLoader().getResource("keystore").getPath(),
            "alien4cloud",
            "alien4cloud");
    //      Map<String, TestBlockingServlet> servlets = new HashMap<>();
    //      servlets.put("/", new TestBlockingServlet());
    // testServer.start();
  }

//  @Disabled("Test is ignored because it overstretches the orchestrator")
//  @Test
//  public void deployUndeployProductionAuthSingleToscaCompute()
//      throws JsonParseException, JsonMappingException, IOException, NoSuchFieldException,
//      OrchestratorIamException {
//    OrchestratorConnector oc = new OrchestratorConnector();
//
//    URL url =
//        OrchestratorConnectorTest.class.getClassLoader().getResource("test_compute_indigodc.yaml");
//    String yamlIndigoDC =
//        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
//    ObjectMapper mapper = new ObjectMapper();
//    Deployment d = new Deployment();
//    d.setParameters(new HashMap<String, Object>());
//    d.setCallback("http://localhost:8080/callback");
//    d.setTemplate(yamlIndigoDC);
//    String callJson =
//        mapper.writeValueAsString(d).replaceAll("\\\\n", "\\n").replace("\\\\\\", "\\");
//
//    // String callJson =
//    // String.format("{\"template\":\"%s\",\"parameters\":{\"cpus\":1},\"callback\":\"http://localhost:8080/callback\"}", yamlIndigoDC);
//    // log.info("call to be sent to the orchestrator: \n" + callJson);
//    CloudConfiguration cc = TestUtil.getRealConfiguration(null);
//    String token = TestUtil.getTestToken();
//    OrchestratorResponse or = oc.callDeploy(cc, callJson);
//    ObjectMapper objectMapper = new ObjectMapper();
//    objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
//    JsonNode root = objectMapper.readTree(or.getResponse().toString());
//    List<JsonNode> vals = root.findValues("uuid");
//    Assertions.assertEquals(true, vals.size() >= 1);
//    String uuid = vals.get(0).asText();
//    // log.info("UUID of the app: " + uuid);
//    vals = root.findValues("status");
//    Assertions.assertEquals(true, vals.size() >= 1);
//    vals = root.findValues("callback");
//    Assertions.assertEquals(true, vals.size() >= 1);
//
//    // now let us see the status
//    or = oc.callDeploymentStatus(cc, uuid);
//    log.info("Deployment status is: " + or.getResponse().toString());
//
//    // now let us undeploy
//    or = oc.callUndeploy(cc, uuid);
//    Assertions.assertEquals(204, or.getCode());
//
//    // now let us see the status
//    or = oc.callDeploymentStatus(cc, uuid);
//    log.info("Deployment status is: " + or.getResponse().toString());
//  }


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
                new OrchestratorResponse(1, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or =
        oc.callDeploy(cc, "{\"response\": 1}");
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
                new OrchestratorResponse(1, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test_unsecured.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    oc.callDeploy(cc, "{\"response\": 1}");
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
                new OrchestratorResponse(1, new StringBuilder("{\"status\": \"ok\"}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or  = oc.callDeploymentStatus("", cc, "id");
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
                new OrchestratorResponse(404, new StringBuilder("{\"status\": \"ok\"}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    Executable callGetDeployments = () -> {oc.callGetDeployments(cc);};
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
                new OrchestratorResponse(200, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or =
        oc.callGetDeployments(cc);
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
                new OrchestratorResponse(200, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or =
        oc.callUndeploy(cc, "id");
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
                new OrchestratorResponse(200, new StringBuilder("{}")))));
    TestServer testServer = getTestServer();
    testServer.start(servlets);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test_unsecured.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or =
        oc.callUndeploy(cc, "id");
    testServer.stop();
  }
  
  @Test
  public void testUndeployWithTestServer404NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(404);
  }
  
  @Test
  public void testUndeployWithTestServer101NotSecure() throws Exception {
	  //testUndeployDifferentHttpCodesUnsecured(101);
  }
  
  @Test
  public void testUndeployWithTestServer301NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(301);
  }
  
  @Test
  public void testUndeployWithTestServer501NotSecure() throws Exception {
	  testUndeployDifferentHttpCodesUnsecured(501);
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
	                new OrchestratorResponse(code, new StringBuilder("{}")))));
	    TestServer testServer = getTestServer();
	    testServer.start(servlets);
	    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test_unsecured.json");
	    OrchestratorConnector oc = getTestOrchestratorConnector();
	    Executable callUndeploy = () -> oc.callUndeploy(cc, "id");
	    Assertions.assertThrows(OrchestratorIamException.class, callUndeploy);
	    testServer.stop();
  }
  
  @Disabled
  protected static OrchestratorConnector getTestOrchestratorConnector() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
    OrchestratorConnector oc = new OrchestratorConnector();
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OidcConfiguration oidcConf = new OidcConfiguration();
    oidcConf.setAuthorizationEndpoint("http://localhost");
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(OrchestratorConnectorTest.class.getClassLoader().getResourceAsStream("test_keystore"), null);
    DeepOrchestrator client = new DeepOrchestratorTemplate(cc.getOrchestratorEndpoint(), ks, oidcConf, OIDC_ACCESS_TOKEN);
    /*ConnectionRepository conn = Mockito.mock(ConnectionRepository.class);
    Connection c = Mockito.mock(Connection.class);
    Mockito.when(conn.getPrimaryConnection(Oidc.class)).thenReturn(c);
    ConnectionData cd = new ConnectionData(OIDC_PROVIDER_ID, OIDC_PROVIDER_USER_ID,
        OIDC_DISPLAY_NAME, OIDC_PROFILE_URL, OIDC_IMAGE_URL,
        OIDC_ACCESS_TOKEN, OIDC_SECRET, OIDC_REFRESH_TOKEN, OIDC_EXPIRE_TIME);
    Mockito.when(c.createData()).thenReturn(cd);*/
    TestUtil.setPrivateField(oc, "client", client);
    return oc;
  }
  
}
