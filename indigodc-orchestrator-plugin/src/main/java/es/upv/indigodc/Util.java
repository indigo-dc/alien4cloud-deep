package es.upv.indigodc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonProcessingException;

import alien4cloud.exception.NotFoundException;
import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.InstanceStatus;
import es.upv.indigodc.service.OrchestratorConnector;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Util class containing methods used in different components
 * @author asalic
 *
 */
public class Util {

  @Getter
  @Setter
  @AllArgsConstructor
  public static class InstanceStatusInfo {
    protected InstanceStatus instanceStatus;
    protected String state;
  }

  /**
   * Transform a Throwable to string
   * @param e the Throwable to be transformed
   * @return the Throwable in String format
   */
  public static String throwableToString(Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  /**
   * Converts the status of a deployment on the IndigoDataCloud Orchestrator to the ALien4Cloud deployment status
   * @param status the IndigoDataCloud Orchestrator deployment status
   * @return the ALien4Cloud deployment status
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static DeploymentStatus indigoDCStatusToDeploymentStatus(final String status)
      throws JsonProcessingException, IOException {
    switch (status) {
      case "UNKNOWN":
        return DeploymentStatus.UNKNOWN;
      case "CREATE_COMPLETE":
        return DeploymentStatus.DEPLOYED;
      case "CREATE_FAILED":
        return DeploymentStatus.FAILURE;
      case "CREATE_IN_PROGRESS":
        return DeploymentStatus.DEPLOYMENT_IN_PROGRESS;
      case "DELETE_COMPLETE":
        return DeploymentStatus.UNDEPLOYED;
      case "DELETE_FAILED":
        return DeploymentStatus.FAILURE;
      case "DELETE_IN_PROGRESS":
        return DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS;
      case "UPDATE_COMPLETE":
        return DeploymentStatus.UPDATED;
      case "UPDATE_FAILED":
        return DeploymentStatus.UPDATE_FAILURE;
      case "UPDATE_IN_PROGRESS":
        return DeploymentStatus.UPDATE_IN_PROGRESS;
      default:
        throw new NotFoundException("Status \"" + status + "\" not supported yet");
    }
  }

  /**
   * Converts the status of an instance of a node on the IndigoDataCloud Orchestrator to 
   * the ALien4Cloud instance of a node status
   * @param status the IndigoDataCloud Orchestrator status
   * @return the ALien4Cloud status
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static InstanceStatusInfo indigoDCStatusToInstanceStatus(final String status)
      throws JsonProcessingException, IOException {
    switch (status) {
      case "UNKNOWN":
        return new InstanceStatusInfo(InstanceStatus.FAILURE, "UNKNOWN");
      case "CREATE_COMPLETE":
        return new InstanceStatusInfo(InstanceStatus.SUCCESS, "CREATE_COMPLETE");
      case "CREATE_FAILED":
        return new InstanceStatusInfo(InstanceStatus.FAILURE, "CREATE_FAILED");
      case "CREATE_IN_PROGRESS":
        return new InstanceStatusInfo(InstanceStatus.PROCESSING, "CREATE_IN_PROGRESS");
      case "DELETE_COMPLETE":
        return new InstanceStatusInfo(InstanceStatus.SUCCESS, "DELETE_COMPLETE");
      case "DELETE_FAILED":
        return new InstanceStatusInfo(InstanceStatus.FAILURE, "DELETE_FAILED");
      case "DELETE_IN_PROGRESS":
        return new InstanceStatusInfo(InstanceStatus.PROCESSING, "DELETE_IN_PROGRESS");
      case "UPDATE_COMPLETE":
        return new InstanceStatusInfo(InstanceStatus.SUCCESS, "UPDATE_COMPLETE");
      case "UPDATE_FAILED":
        return new InstanceStatusInfo(InstanceStatus.FAILURE, "UPDATE_FAILED");
      case "UPDATE_IN_PROGRESS":
        return new InstanceStatusInfo(InstanceStatus.PROCESSING, "UPDATE_IN_PROGRESS");
      default:
        throw new NotFoundException("Status \"" + status + "\" not supported yet");
    }
  }
}
