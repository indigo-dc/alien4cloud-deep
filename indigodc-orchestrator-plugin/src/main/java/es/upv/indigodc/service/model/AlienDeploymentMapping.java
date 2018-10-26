package es.upv.indigodc.service.model;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Utility class to store mapping between an AlienDeployment and Marathon. */
@Getter
@AllArgsConstructor
public class AlienDeploymentMapping {

  public static final AlienDeploymentMapping EMPTY = new AlienDeploymentMapping(
      "unknown_deployment_id", "unknown_orchestrator_id", DeploymentStatus.UNKNOWN);

  /**
   * The id of the whole deployment (basically the instance if the topology launched at one time on
   * the orchestrator).
   */
  private String deploymentId;
  /**
   * The id of the orchestrator on which the deployment with the id {@link #deploymentId} has been
   * launched.
   */
  private String orchetratorId;
  /** The status of the deployment at a given time (when the call to get the info was executed). */
  private DeploymentStatus status;
}
