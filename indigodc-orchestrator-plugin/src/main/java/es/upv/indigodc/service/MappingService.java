package es.upv.indigodc.service;

import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import com.google.common.collect.Maps;
import es.upv.indigodc.service.model.DeploymentInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Holds the relationships between different IDs (given by Alien4Cloud and 
 * the Orchestrator respectively) of what was deployed.
 *
 * @author asalic
 */
@Service("mapping-service")
@Scope("prototype")
@Slf4j
public class MappingService {

  protected Map<String, DeploymentInfo> deployments = Maps.newConcurrentMap();

  /**
   * Initialize the service with those topologies that are deployed on the Orchestrator.
   * 
   * @param activeDeployments The list of deployed topologies.
   */
  public void init(Map<String, PaaSTopologyDeploymentContext> activeDeployments) {
    for (Map.Entry<String, PaaSTopologyDeploymentContext> ad : activeDeployments.entrySet()) {
      deployments.put(
          ad.getValue().getDeploymentPaaSId(),
          new DeploymentInfo(
              ad.getValue().getDeploymentPaaSId(),
              null,
              ad.getValue().getDeploymentId(),
              ad.getValue().getDeploymentTopology().getOrchestratorId(),
              DeploymentStatus.UNKNOWN,
              null,
              null));
    }
  }

  public synchronized void registerDeployment(DeploymentInfo di) {
    deployments.put(di.getA4cDeploymentPaasId(), di);
  }

  public synchronized void unregisterDeployment(String a4cDeploymentPaasId) {
    deployments.remove(a4cDeploymentPaasId);
  }

  public synchronized List<String> getA4cDeploymentPaasIds() {
    return deployments.keySet().stream().collect(Collectors.toList());
  }

  public synchronized DeploymentInfo getByA4CDeploymentPaasId(String a4cDeploymentPaasId) {
    return deployments.get(a4cDeploymentPaasId);
  }

  /**
   * Get the information about a deployment by Orchestrator's Deployment ID.
   * 
   * @param orchestratorDeploymentId Orchestrator's Deployment ID.
   * @return the deployment information if exists, otherwise null
   */
  public synchronized DeploymentInfo getByOrchestratorDeploymentId(
      String orchestratorDeploymentId) {
    Optional<DeploymentInfo> diR =
        deployments
            .values()
            .stream()
            .filter(di -> di.getOrchestratorDeploymentId().equals(orchestratorDeploymentId))
            .findFirst();
    if (diR.isPresent()) {
      return diR.get();
    } else {
      return null;
    }
  }

  //  /** Map marathon deployment ids, which are ephemeral, to alien deployment ids. */
  //  private final Map<String, AlienDeploymentMapping> indigoDcToAlienDeploymentMap =
  //      Maps.newConcurrentMap();
  //
  //  private final Map<String, OrchestratorDeploymentMapping> alienToIndigoDcDeploymentMap =
  //      Maps.newConcurrentMap();
  //
  //  /**
  //   * Register a running deployment into the MappingService.
  //   *
  //   * @param orchestratorUuidDeployment the id of the deployment in the Indigo DC orchestrator
  //   * @param alienDeploymentId the id of the deployment in Alien
  //   * @param status The running status of the deployment, Deploying or Undeploying.
  //   */
  //  public void registerDeploymentInfo(String orchestratorUuidDeployment, String
  // alienDeploymentId,
  //      String orchestratorId, DeploymentStatus status) {
  //    indigoDcToAlienDeploymentMap.put(orchestratorUuidDeployment,
  //        new AlienDeploymentMapping(alienDeploymentId, orchestratorId, status));
  //    alienToIndigoDcDeploymentMap.put(alienDeploymentId,
  //        new OrchestratorDeploymentMapping(orchestratorUuidDeployment, status));
  //  }
  //
  //  /**
  //   * Adds a deployment status.
  //   *
  //   * @param alienDeploymentId the A4C deployment ID
  //   * @param status its status
  //   */
  //  public void registerDeploymentInfoAlienToIndigoDc(String alienDeploymentId,
  //      DeploymentStatus status) {
  //    alienToIndigoDcDeploymentMap.put(alienDeploymentId,
  //        new OrchestratorDeploymentMapping(null, status));
  //  }
  //
  //  public OrchestratorDeploymentMapping getByAlienDeploymentId(String alienDeploymentId) {
  //    return alienToIndigoDcDeploymentMap.get(alienDeploymentId);
  //  }
  //
  //  /**
  //   * Returns a deployment mapping by orchestrator returned deploymnet ID.
  //   *
  //   * @param orchestratorUuidDeployment the ID of the deployment returned from the orchestrator
  //   * @return the mapping of the A4C deployment
  //   */
  //  public AlienDeploymentMapping getByOrchestratorUuidDeployment(String
  // orchestratorUuidDeployment) {
  //    return indigoDcToAlienDeploymentMap.get(orchestratorUuidDeployment);
  //  }
  //
  //  /**
  //   * Initializes the list of active deployments.
  //   *
  //   * @param activeDeployments a list of active deployments
  //   */
  //  public void init(Collection<PaaSTopologyDeploymentContext> activeDeployments) {
  //    log.info("call");
  //    activeDeployments.forEach(context -> {
  //      // Initialize a new group mapping
  //      final String groupId = context.getDeploymentPaaSId().toLowerCase();
  //      // Fill app mapping
  //      // registerGroupMapping(groupId, context.getDeploymentId());
  //      // context.getPaaSTopology().getNonNatives().forEach(nodeTemplate ->
  //      // registerAppMapping(groupId, nodeTemplate.getId().toLowerCase(),
  //      // nodeTemplate.getId()));
  //    });
  //  }
}
