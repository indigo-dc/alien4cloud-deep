package es.upv.indigodc.service.model;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class OrchestratorDeploymentMapping {
  public static final OrchestratorDeploymentMapping EMPTY =
      new OrchestratorDeploymentMapping("unknown_deployment_id", DeploymentStatus.UNKNOWN);

  private String orchestratorUUIDDeployment;
  private DeploymentStatus status;
}
