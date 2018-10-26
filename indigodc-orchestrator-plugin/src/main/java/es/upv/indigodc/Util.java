package es.upv.indigodc;

import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.InstanceStatus;
import es.upv.indigodc.service.model.StatusNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Util class containing methods used in different components.
 *
 * @author asalic
 */
public class Util {

  /**
   * Store A4C status {@link alien4cloud.paas.model.InstanceStatus} and the Orchestrator state of
   * the instance.
   *
   * @author asalic
   */
  @Getter
  @AllArgsConstructor
  public static class InstanceStatusInfo {
    /** The A4C instance status. */
    protected InstanceStatus instanceStatus;
    /** The Orchestrator instance state . */
    protected String state;
  }

  // /**
  // * Transform a Throwable to string.
  // *
  // * @param e the Throwable to be transformed
  // * @return the Throwable in String format
  // */
  // public static String throwableToString(Throwable e) {
  // StringWriter sw = new StringWriter();
  // e.printStackTrace(new PrintWriter(sw));
  // return sw.toString();
  // }

  /**
   * Converts the status of a deployment on the IndigoDataCloud Orchestrator to the ALien4Cloud
   * deployment status.
   *
   * @param status the IndigoDataCloud Orchestrator deployment status
   * @return the ALien4Cloud deployment status
   * @throws StatusNotFoundException when the orchestrator status cannot be mapped to A4C
   */
  public static DeploymentStatus indigoDcStatusToDeploymentStatus(final String status)
      throws StatusNotFoundException {

    try {
      switch (IndigoDcDeploymentStatus.valueOf(status)) {
        case UNKNOWN:
          return DeploymentStatus.UNKNOWN;
        case CREATE_COMPLETE:
          return DeploymentStatus.DEPLOYED;
        case CREATE_FAILED:
          return DeploymentStatus.FAILURE;
        case CREATE_IN_PROGRESS:
          return DeploymentStatus.DEPLOYMENT_IN_PROGRESS;
        case DELETE_COMPLETE:
          return DeploymentStatus.UNDEPLOYED;
        case DELETE_FAILED:
          return DeploymentStatus.FAILURE;
        case DELETE_IN_PROGRESS:
          return DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS;
        case UPDATE_COMPLETE:
          return DeploymentStatus.UPDATED;
        case UPDATE_FAILED:
          return DeploymentStatus.UPDATE_FAILURE;
        case UPDATE_IN_PROGRESS:
          return DeploymentStatus.UPDATE_IN_PROGRESS;
        default:
          throw new StatusNotFoundException(status);
      }
    } catch (IllegalArgumentException ex) {
      throw new StatusNotFoundException(status);
    }
  }

  /**
   * Converts the status of an instance of a node on the IndigoDataCloud Orchestrator to the
   * ALien4Cloud instance of a node status.
   *
   * @param status the IndigoDataCloud Orchestrator status
   * @return the Alien4Cloud status
   * @throws StatusNotFoundException when the orchestrator status cannot be mapped to A4C
   */
  public static InstanceStatusInfo indigoDcStatusToInstanceStatus(final String status)
      throws StatusNotFoundException {
    try {
      switch (IndigoDcDeploymentStatus.valueOf(status)) {
        case UNKNOWN:
          return new InstanceStatusInfo(InstanceStatus.FAILURE,
              IndigoDcDeploymentStatus.UNKNOWN.name());
        case CREATE_COMPLETE:
          return new InstanceStatusInfo(InstanceStatus.SUCCESS,
              IndigoDcDeploymentStatus.CREATE_COMPLETE.name());
        case CREATE_FAILED:
          return new InstanceStatusInfo(InstanceStatus.FAILURE,
              IndigoDcDeploymentStatus.CREATE_FAILED.name());
        case CREATE_IN_PROGRESS:
          return new InstanceStatusInfo(InstanceStatus.PROCESSING,
              IndigoDcDeploymentStatus.CREATE_IN_PROGRESS.name());
        case DELETE_COMPLETE:
          return new InstanceStatusInfo(InstanceStatus.SUCCESS,
              IndigoDcDeploymentStatus.DELETE_COMPLETE.name());
        case DELETE_FAILED:
          return new InstanceStatusInfo(InstanceStatus.FAILURE,
              IndigoDcDeploymentStatus.DELETE_FAILED.name());
        case DELETE_IN_PROGRESS:
          return new InstanceStatusInfo(InstanceStatus.PROCESSING,
              IndigoDcDeploymentStatus.DELETE_IN_PROGRESS.name());
        case UPDATE_COMPLETE:
          return new InstanceStatusInfo(InstanceStatus.SUCCESS,
              IndigoDcDeploymentStatus.UPDATE_COMPLETE.name());
        case UPDATE_FAILED:
          return new InstanceStatusInfo(InstanceStatus.FAILURE,
              IndigoDcDeploymentStatus.UPDATE_FAILED.name());
        case UPDATE_IN_PROGRESS:
          return new InstanceStatusInfo(InstanceStatus.PROCESSING,
              IndigoDcDeploymentStatus.UPDATE_IN_PROGRESS.name());
        default:
          throw new StatusNotFoundException(status);
      }
    } catch (IllegalArgumentException ex) {
      throw new StatusNotFoundException(status);
    }
  }
}
