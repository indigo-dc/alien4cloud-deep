package es.upv.indigodc.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Used to get a subset of the available information about a deployment.
 * This class offers a view of the current status.
 */
@Getter
@Setter
@AllArgsConstructor
public class DeploymentInfoPair {

  /**
   * The deployment PaaS ID as given by A4C.
   */
  protected String a4cDeploymentPaasId;
  /**
   * The deployment UUID as returned by the orchestrator.
   */
  protected String orchestratorDeploymentId;
  /**
   * The  orchestrator ID given by A4C.
   */
  protected String orchestratorId;

}
