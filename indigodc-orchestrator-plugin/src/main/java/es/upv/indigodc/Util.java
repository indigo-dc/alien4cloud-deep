package es.upv.indigodc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonProcessingException;

import alien4cloud.exception.NotFoundException;
import alien4cloud.paas.model.DeploymentStatus;
import es.upv.indigodc.service.OrchestratorConnector;
import es.upv.indigodc.service.model.OrchestratorResponse;

public class Util {
	
	public static String throwableToString(Throwable e) {
    	StringWriter sw = new StringWriter();
    	e.printStackTrace(new PrintWriter(sw));
    	return sw.toString();
	}
	
  public static DeploymentStatus indigoDCStatusToDeploymentStatus(final String status) throws JsonProcessingException, IOException {
    switch (status) {
    case "UNKNOWN": return DeploymentStatus.UNKNOWN;
    case "CREATE_COMPLETE": return DeploymentStatus.DEPLOYED;
    case "CREATE_FAILED": return DeploymentStatus.FAILURE;
    case "CREATE_IN_PROGRESS": return DeploymentStatus.DEPLOYMENT_IN_PROGRESS;
    case "DELETE_COMPLETE": return DeploymentStatus.UNDEPLOYED;
    case "DELETE_FAILED": return DeploymentStatus.FAILURE;
    case "DELETE_IN_PROGRESS": return DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS;
    case "UPDATE_COMPLETE": return DeploymentStatus.UPDATED;
    case "UPDATE_FAILED": return DeploymentStatus.UPDATE_FAILURE;
    case "UPDATE_IN_PROGRESS": return DeploymentStatus.UPDATE_IN_PROGRESS;
    default: throw new NotFoundException("Status \"" + status + "\" not supported yet");
    }
  }


}
