package es.upv.indigodc.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeploymentInfoPair {  

  protected String a4cDeploymentPaasId;
  protected String orchestratorDeploymentId;
  protected String orchestratorId;

}
