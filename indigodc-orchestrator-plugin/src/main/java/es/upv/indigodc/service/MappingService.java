package es.upv.indigodc.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.google.common.collect.Maps;

import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.service.model.AlienDeploymentMapping;
import es.upv.indigodc.service.model.OrchestratorDeploymentMapping;
import lombok.extern.slf4j.Slf4j;

@Service("mapping-service")
@Slf4j
public class MappingService {

  /** Map marathon deployment ids, which are ephemeral, to alien deployment ids */
  private final Map<String, AlienDeploymentMapping> indigoDCToAlienDeploymentMap =
      Maps.newConcurrentMap();

  private final Map<String, OrchestratorDeploymentMapping> alienToIndigoDCDeploymentMap =
      Maps.newConcurrentMap();

  /**
   * Register a running deployment into the MappingService.
   *
   * @param indigoDCDeploymentId the id of the deployment in the Indigo DC orchestrator
   * @param alienDeploymentId the id of the deployment in Alien
   * @param status The running status of the deployment, Deploying or Undeploying.
   */
  public void registerDeploymentInfo(
      String orchestratorUUIDDeployment,
      String alienDeploymentId,
      String orchestratorID,
      DeploymentStatus status) {
    indigoDCToAlienDeploymentMap.put(
        orchestratorUUIDDeployment,
        new AlienDeploymentMapping(alienDeploymentId, orchestratorID, status));
    alienToIndigoDCDeploymentMap.put(
        alienDeploymentId, new OrchestratorDeploymentMapping(orchestratorUUIDDeployment, status));
  }

  public void registerDeploymentInfoAlienToIndigoDC(
      String alienDeploymentId, DeploymentStatus status) {
    alienToIndigoDCDeploymentMap.put(
        alienDeploymentId, new OrchestratorDeploymentMapping(null, status));
  }

  public OrchestratorDeploymentMapping getByAlienDeploymentId(String alienDeploymentId) {
    return alienToIndigoDCDeploymentMap.get(alienDeploymentId);
  }

  public AlienDeploymentMapping getByOrchestratorUUIDDeployment(String orchestratorUUIDDeployment) {
    return indigoDCToAlienDeploymentMap.get(orchestratorUUIDDeployment);
  }

  public void init(Collection<PaaSTopologyDeploymentContext> activeDeployments) {
    log.info("call");
    activeDeployments.forEach(
        context -> {
          // Initialize a new group mapping
          final String groupId = context.getDeploymentPaaSId().toLowerCase();
          // Fill app mapping
          // registerGroupMapping(groupId, context.getDeploymentId());
          //          context.getPaaSTopology().getNonNatives().forEach(nodeTemplate ->
          //          	registerAppMapping(groupId, nodeTemplate.getId().toLowerCase(),
          // nodeTemplate.getId()));
        });
  }
}
