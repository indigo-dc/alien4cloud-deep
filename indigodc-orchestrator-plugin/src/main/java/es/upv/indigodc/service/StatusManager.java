package es.upv.indigodc.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.IndigoDcDeploymentStatus;
import es.upv.indigodc.Util;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.location.LocationConfiguratorFactory;
import es.upv.indigodc.service.model.DeploymentInfo;
import es.upv.indigodc.service.model.DeploymentInfoPair;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import java.io.IOException;
import javax.inject.Inject;
import lombok.Getter;

public class StatusManager {
  
  protected final Map<String, DeploymentInfo> deploymentInfos = new HashMap<>();
  protected ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  
  @Autowired
  protected CloudConfigurationManager cloudConfigurationManager;
  
  @Autowired
  protected UserService userService;
  
  
  
  /** The service that executes the HTTP(S) calls to the Orchestrator. */
  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;
  
  public static class StatusObtainer implements Runnable {
    
    protected StatusManager statusManager;
    protected OrchestratorConnector orchestratorConnector;
    protected CloudConfigurationManager cloudConfigurationManager;
    protected UserService userService;

    public StatusObtainer(StatusManager statusManager, 
            OrchestratorConnector orchestratorConnector,
            CloudConfigurationManager cloudConfigurationManager,
            UserService userService) {
      this.statusManager = statusManager;
      this.orchestratorConnector = orchestratorConnector;
      this.cloudConfigurationManager = cloudConfigurationManager;
      this.userService = userService;
    }
    
    @Override
    public void run() {
      try {
        List<DeploymentInfoPair> activeDeployments = 
                this.statusManager.getActiveDeploymentsIds();
        for (DeploymentInfoPair dip: activeDeployments) {
            try {
                OrchestratorResponse response = 
                        orchestratorConnector.callDeploymentStatus(
                        this.cloudConfigurationManager.getCloudConfiguration(dip.getOrchestratorId()),
                        userService.getToken(),
                        dip.getOrchestratorDeploymentId());
                String statusTopologyDeployment = response.getStatusTopologyDeployment();
                this.statusManager.updateStatus(
                        dip.getA4cDeploymentPaasId(), 
                        Util.indigoDcStatusToDeploymentStatus(statusTopologyDeployment),
                        null);
               
            } catch (IOException | NoSuchFieldException | OrchestratorIamException er) {
              er.printStackTrace();
              this.statusManager.updateStatus(
                        dip.getA4cDeploymentPaasId(), 
                        DeploymentStatus.UNKNOWN,
                        er);
            }
        }
        
      } catch (Throwable er) {
        er.printStackTrace();
      }
      
    }
    
  }
  
  
  public void initActiveDeployments(Map<String, PaaSTopologyDeploymentContext> activeDeployments) {
    for (Map.Entry<String, PaaSTopologyDeploymentContext> topoE: activeDeployments.entrySet()) {
      addStatus(topoE.getValue(), null);
    }
    executor.scheduleWithFixedDelay(
            new StatusObtainer(this, 
                orchestratorConnector, cloudConfigurationManager,
                userService), 
            0, 10, TimeUnit.SECONDS);
  }
  
  public synchronized void addStatus(PaaSTopologyDeploymentContext topo, 
          String orchestratorDeploymentId) {
    DeploymentInfo deploymentInfo = new DeploymentInfo();
    deploymentInfo.setA4cDeploymentPaasId(topo.getDeploymentPaaSId());
    deploymentInfo.setStatus(DeploymentStatus.UNKNOWN);
    deploymentInfo.setOrchestratorId(topo.getDeployment().getOrchestratorId());
    deploymentInfo.setOrchestratorDeploymentId(orchestratorDeploymentId);
    deploymentInfos.put(topo.getDeploymentPaaSId(), deploymentInfo);
  }
  
  public synchronized List<DeploymentInfoPair> getActiveDeploymentsIds() {
    return deploymentInfos.values().stream().map(di -> 
            new DeploymentInfoPair(di.getA4cDeploymentPaasId(), 
                    di.getOrchestratorDeploymentId(), di.getOrchestratorId()))
            .collect(Collectors.toList());
  }
  
  public void destroy() {
    executor.shutdown();
  }
  
  public synchronized void rmStatus(String a4cDeploymentPaasId) {
    deploymentInfos.remove(a4cDeploymentPaasId);
  }
  
  public synchronized void updateStatus(String a4cDeploymentPaasId, 
          DeploymentStatus status, Throwable er) {
    DeploymentInfo di = deploymentInfos.get(a4cDeploymentPaasId);
    // Checked if it wasn't removed
    if (a4cDeploymentPaasId != null) {
      di.setStatus(status);
      di.setErrorDeployment(er);
    }
  }
}
