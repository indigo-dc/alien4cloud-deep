package es.upv.indigodc.service.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  public synchronized boolean hasOutputs() {return outputs != null;}

  @JsonIgnore
  public synchronized String setOrchestratorDeploymentIdIfNull(String orchestratorDeploymentId) {
    String ret = this.orchestratorDeploymentId;
    if (this.orchestratorDeploymentId == null)
      this.orchestratorDeploymentId = orchestratorDeploymentId;
    return ret;
  }

  public synchronized String getA4cDeploymentPaasId() {
    return a4cDeploymentPaasId;
  }

  public synchronized void setA4cDeploymentPaasId(String a4cDeploymentPaasId) {
    this.a4cDeploymentPaasId = a4cDeploymentPaasId;
  }

  public synchronized String getOrchestratorDeploymentId() {
    return orchestratorDeploymentId;
  }

  public synchronized void setOrchestratorDeploymentId(String orchestratorDeploymentId) {
    this.orchestratorDeploymentId = orchestratorDeploymentId;
  }

  public synchronized String getA4cDeploymentId() {
    return a4cDeploymentId;
  }

  public synchronized void setA4cDeploymentId(String a4cDeploymentId) {
    this.a4cDeploymentId = a4cDeploymentId;
  }

  public synchronized String getOrchestratorId() {
    return orchestratorId;
  }

  public synchronized void setOrchestratorId(String orchestratorId) {
    this.orchestratorId = orchestratorId;
  }

  public synchronized DeploymentStatus getStatus() {
    return status;
  }

  public synchronized void setStatus(DeploymentStatus status) {
    this.status = status;
  }

  public synchronized Map<String, String> getOutputs() {
    return outputs;
  }

  public synchronized void setOutputs(Map<String, String> outputs) {
    this.outputs = outputs;
  }

  public synchronized Throwable getErrorDeployment() {
    return errorDeployment;
  }

  public synchronized void setErrorDeployment(Throwable errorDeployment) {
    this.errorDeployment = errorDeployment;
  }
}
