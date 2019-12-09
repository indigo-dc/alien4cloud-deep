package es.upv.indigodc.service.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps the response received from the orchestrator when getting a list of all deployments created
 * by a user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDeploymentsResponse {

  /** The list of links. */
  protected List<Link> links;
  /** the list of deployments with information about each one. */
  protected List<DeploymentOrchestrator> content;
  /** Information about the page retrieved by this call. */
  protected Page page;
}
