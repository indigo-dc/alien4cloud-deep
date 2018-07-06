package es.upv.indigodc.service.model;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Utility class to store mapping between an AlienDeployment and Marathon. */
@Getter
@AllArgsConstructor
public class AlienDeploymentMapping {

  public static final AlienDeploymentMapping EMPTY =
      new AlienDeploymentMapping(
          "unknown_deployment_id", "unknown_orchestrator_id", DeploymentStatus.UNKNOWN);

  private String deploymentId;
  private String orchetratorId;
  private DeploymentStatus status;
}
