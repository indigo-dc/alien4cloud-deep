package es.upv.indigodc.service;

import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.oidc.deep.api.DeepOrchestrator;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Manage and do the calls to the REST API exposed by the IndigoDC Orchestrator; Connect to the
 * authorization system to obtain a token for the calls to the IndigoDC Orchestrator.
 *
 * @author asalic
 */
@Slf4j
@Service("orchestrator-connector")
public class OrchestratorConnector {  
  
  /** Web service path for deployments operations; It is appended to the orchestrator endpoint. */
  public static final String WS_PATH_DEPLOYMENTS = "/deployments";

  private static final Logger LOGGER = Logger.getLogger(OrchestratorConnector.class.getName());

  @Autowired
  protected ConnectionRepository repository;

  private DeepOrchestrator getClient() {
    Connection<DeepOrchestrator> connection = repository.findPrimaryConnection(DeepOrchestrator.class);
    DeepOrchestrator deepOrchestrator = connection != null ? connection.getApi() : null;
    return deepOrchestrator;
  }

  /**
   * Obtain the list of deployments created by the the user with the client id stored in the cloud
   * configuration for the plugin.
   *
   * @return The response from the orchestrator
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callGetDeployments(String orchestrarorUrl)
      throws IOException, NoSuchFieldException, OrchestratorIamException {
    return buildResponse(() -> getClient().callGetDeployments(orchestrarorUrl));
  }

  /**
   * Deploy an alien 4 cloud topology It is already adapted to the normative TOSCA supported by the
   * Orchestrator.
   *
   * @param yamlTopology The actual topology accepted by the orchestrator. It is a string formated
   *        and packed for the orchestrator e.g. new lines are replaced with their representation of
   *        '\n'.
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callDeploy(String orchestratorUrl,
      String yamlTopology)
      throws IOException, NoSuchFieldException, OrchestratorIamException {
    log.info("call Deploy");
    log.info("Topology to be sent to the orchestrator: \n" + yamlTopology);
    return buildResponse(() ->
            getClient().callDeploy(orchestratorUrl, yamlTopology));
  }

  /**
   * Get the status of a deployment with a given deployment ID.
   *
   * @param deploymentId The id of the deployment given by the orchestrator we need the information for
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callDeploymentStatus(String orchestrarorUrl, String deploymentId)
      throws IOException, NoSuchFieldException, OrchestratorIamException {
    log.info("call deployment status for UUID " + deploymentId);
    return buildResponse(() -> getClient().callDeploymentStatus(orchestrarorUrl, deploymentId));
  }

  /**
   *
   * @param deploymentId The id of the deployment given by the orchestrator we need the information for
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *    *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *    *         and 299.
   */
  public OrchestratorResponse callGetTemplate(String orchestrarorUrl, String deploymentId)
          throws IOException, NoSuchFieldException, OrchestratorIamException {
    log.info("call get template for UUID " + deploymentId);
    return buildResponse(() -> getClient().callGetTemplate(orchestrarorUrl, deploymentId));
  }


  /**
   * Invoke the undeploy REST API for a given deployment ID.
   *
   * @param deploymentId The id of the deployment we need to undeploy
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callUndeploy(String orchestrarorUrl,
      String deploymentId)
      throws IOException, NoSuchFieldException, OrchestratorIamException {
    log.info("call undeploy");
    return buildResponse(() -> getClient().callUndeploy(orchestrarorUrl, deploymentId));
  }

  private OrchestratorResponse buildResponse(Supplier<ResponseEntity<String>> func) throws OrchestratorIamException, IOException {
    try {
      ResponseEntity<String> response = func.get();
      int responseCode = response.getStatusCode().value();
      if (300 <= responseCode && responseCode <= 599) {
        throw new OrchestratorIamException(responseCode, response.getBody(), response.getBody());
      }
      return new OrchestratorResponse(response);
    } catch (HttpStatusCodeException e) {
      throw new OrchestratorIamException(e.getStatusCode().value(), e.getResponseBodyAsString(), e.getResponseBodyAsString());
    }
  }
}
