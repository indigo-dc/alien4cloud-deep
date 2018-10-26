package es.upv.indigodc;

/**
 * The statuses returned by the IndigoDC orchestrator. Each element has the exact name as the string
 * returned by the Orchestrator.
 * 
 * @author asalic
 *
 */
public enum IndigoDcDeploymentStatus {

  UNKNOWN, CREATE_COMPLETE, CREATE_FAILED, CREATE_IN_PROGRESS, DELETE_COMPLETE, DELETE_FAILED, 
  DELETE_IN_PROGRESS, UPDATE_COMPLETE, UPDATE_FAILED, UPDATE_IN_PROGRESS


}
