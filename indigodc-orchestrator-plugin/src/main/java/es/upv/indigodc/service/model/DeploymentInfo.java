package es.upv.indigodc.service.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentInfo implements Serializable {  

  /**
   * 
   */
  private static final long serialVersionUID = 8452877443031991069L;
  protected String a4cDeploymentPaasId;
  protected String orchestratorDeploymentId;
  protected String a4cDeploymentId;
  
  protected String orchestratorId;
  
  protected DeploymentStatus status; 
  
  protected Map<String, String> outputs;
  
  /** If an error occurred when trying to get the deployment **/
  protected Throwable errorDeployment;
  
  @JsonIgnore
  public boolean hasOutputs() {return outputs != null;}

}
