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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
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

  public static final String ORCHESTRATOR_URL = "";
  public static final String DEPLOYMENT_ID = "deployment_id";
  public static final String ORCHESTRATOR_RESPONSE_TEMPLATE = "{\"response\": \"%s\"}";


  @Test
  public void callDeploy_response_fname() throws Exception {
    ObjectMapper om = new ObjectMapper();

    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or =
        oc.callDeploy(ORCHESTRATOR_URL, "{\"response\": 1}");
    Assertions.assertEquals(or.getResponse().toString(), String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callDeploy"));
  }

  @Test
  public void callDeploymentStatus_response_fname() throws Exception {

    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or  = oc.callDeploymentStatus(ORCHESTRATOR_URL, DEPLOYMENT_ID);
    Assertions.assertEquals(or.getResponse().toString(), String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callDeploymentStatus"));
  }

  @Test
  public void callGetDeployments_response_fname() throws Exception {
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or = oc.callGetDeployments(ORCHESTRATOR_URL);
    Assertions.assertEquals(or.getResponse().toString(), String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callGetDeployments"));
  }

  @Test
  public void callUndeploy_response_fname() throws Exception {
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or = oc.callUndeploy(ORCHESTRATOR_URL, DEPLOYMENT_ID);
    Assertions.assertEquals(or.getResponse().toString(), String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callUndeploy"));
  }

  @Test
  public void callGetTemplate_response_fname() throws Exception {
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or = oc.callGetTemplate(ORCHESTRATOR_URL, DEPLOYMENT_ID);
    Assertions.assertEquals(or.getResponse().toString(), String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callGetTemplate"));
  }

  @Test
  public void callGetTemplate_server_internal_error() throws Exception {
    OrchestratorConnector oc = getTestOrchestratorConnector();
    OrchestratorResponse or = oc.callGetTemplate(ORCHESTRATOR_URL, DEPLOYMENT_ID);
    Assertions.assertEquals(or.getResponse().toString(), String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callGetTemplate"));
  }

  @Disabled
  protected static OrchestratorConnector getTestOrchestratorConnector() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
    OrchestratorConnector oc = new OrchestratorConnector();
    DeepOrchestrator client = Mockito.mock(DeepOrchestrator.class);
    Connection<DeepOrchestrator> connection = Mockito.mock(Connection.class);
    ConnectionRepository repository = Mockito.mock(ConnectionRepository.class);
    Mockito.when(repository.<DeepOrchestrator>findPrimaryConnection(DeepOrchestrator.class))
            .thenReturn(connection);
    Mockito.when(connection.getApi()).thenReturn(client);

    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
    OidcConfiguration oidcConf = new OidcConfiguration();
    oidcConf.setAuthorizationEndpoint("http://localhost");
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(OrchestratorConnectorTest.class.getClassLoader().getResourceAsStream("test_keystore"), null);
    Mockito.when(client.callDeploy(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new ResponseEntity<String>(String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callDeploy"), HttpStatus.OK));
    Mockito.when(client.callDeploymentStatus(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new ResponseEntity<String>(String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callDeploymentStatus"), HttpStatus.OK));
    Mockito.when(client.callGetDeployments(Mockito.anyString()))
            .thenReturn(new ResponseEntity<String>(String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callGetDeployments"), HttpStatus.OK));
    Mockito.when(client.callGetTemplate(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new ResponseEntity<String>(String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callGetTemplate"), HttpStatus.OK));
    Mockito.when(client.callUndeploy(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new ResponseEntity<String>(String.format(ORCHESTRATOR_RESPONSE_TEMPLATE, "callUndeploy"), HttpStatus.OK));
    /*ConnectionRepository conn = Mockito.mock(ConnectionRepository.class);
    Connection c = Mockito.mock(Connection.class);
    Mockito.when(conn.getPrimaryConnection(Oidc.class)).thenReturn(c);
    ConnectionData cd = new ConnectionData(OIDC_PROVIDER_ID, OIDC_PROVIDER_USER_ID,
        OIDC_DISPLAY_NAME, OIDC_PROFILE_URL, OIDC_IMAGE_URL,
        OIDC_ACCESS_TOKEN, OIDC_SECRET, OIDC_REFRESH_TOKEN, OIDC_EXPIRE_TIME);
    Mockito.when(c.createData()).thenReturn(cd);*/
    TestUtil.setPrivateField(oc, "repository", repository);
    return oc;
  }

}
