package es.upv.indigodc.service.model;

import java.io.Serializable;
import java.util.Map;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeploymentInfo implements Serializable {  

  /**
   * 
   */
  private static final long serialVersionUID = 8452877443031991069L;
  protected String a4cDeploymentPaasId;
  protected String orchestratorDeploymentId;
  
  protected String orchestratorId;
  
  protected DeploymentStatus status; 
  
  protected Map<String, String> outputs;
  
  /** If an error occurred when trying to get the deployment **/
  protected Throwable errorDeployment;

}
