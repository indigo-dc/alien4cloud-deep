package es.upv.indigodc.service.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maintains information pertinent to a deployment.
 */
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentInfo implements Serializable {  

  /**
   * 
   */
  private static final long serialVersionUID = 8452877443031991069L;
  /**
   * The Deployment PaaS ID as given by A4C.
   */
  protected String a4cDeploymentPaasId;
  /**
   * The deployment UUID as returned by the orchestrator.
   */
  protected String orchestratorDeploymentId;
  /**
   * The deployment ID as given by A4C.
   */
  protected String a4cDeploymentId;
  /**
   * The A4C ID of the orchestrator that manages this deployment.
   */
  protected String orchestratorId;
  /**
   * The current status of this deployment.
   */
  protected DeploymentStatus status;
  /**
   * The outputs returned by the orchestrator.
   */
  protected Map<String, String> outputs;
  
  /** If an error occurred when trying to get the deployment **/
  protected Throwable errorDeployment;

  /**
   * Check if this deployment has outputs.
   * @return true if the deployment has outputs; false otherwise.
   */
  @JsonIgnore
  public synchronized boolean hasOutputs() {return outputs != null;}

  /**
   * Sets the deployment UUID as returned by the orchestrator if this deployment doesn have one already.
   * @param orchestratorDeploymentId The deployment UUID as returned by the orchestrator.
   * @return the old ID.
   */
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
