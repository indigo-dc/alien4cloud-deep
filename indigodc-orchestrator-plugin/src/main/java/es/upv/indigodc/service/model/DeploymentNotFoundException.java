package es.upv.indigodc.service.model;

/** Exception thrown when a deployment has not been found in the list managed by the plugin. */
public class DeploymentNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -4249734961214513949L;

  /**
   * Constructor for an exception thrown when a deployment doesn't exist.
   *
   * @param a4cDeploymentPaasId The Alien4CLoud PaaSId of the deployment
   * @param a4cDeploymentId The Alien4CLoud ID of the deployment
   */
  public DeploymentNotFoundException(String a4cDeploymentPaasId, String a4cDeploymentId) {
    super(
        "Deployment with PaaS id "
            + a4cDeploymentPaasId
            + " and deployment id "
            + a4cDeploymentId
            + " cannot be found in the plugin registry.");
  }
}
