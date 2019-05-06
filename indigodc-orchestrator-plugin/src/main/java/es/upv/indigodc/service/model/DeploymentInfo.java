package es.upv.indigodc.service.model;

import java.util.Map;
import alien4cloud.paas.model.DeploymentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeploymentInfo {  

  protected String a4cDeploymentPaasId;
  protected String orchestratorDeploymentId;
  
  protected String orchestratorId;
  
  protected DeploymentStatus status; 
  
  protected Map<String, String> outputsValues;
  
  /** If an error occurred when trying to get the deployment **/
  protected Throwable errorDeployment;

}
