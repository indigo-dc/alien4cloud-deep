package es.upv.indigodc.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;

import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.service.model.DeploymentInfo;
import es.upv.indigodc.service.model.DeploymentInfoPair;

//@Component
public class StatusManagerStorage {

  protected Map<String, DeploymentInfo> deploymentInfos;
  
  public StatusManagerStorage() {
    deploymentInfos = new HashMap<>();
  }
  
  public synchronized DeploymentInfo getDeploymentInfo(String deploymentPaasId) {
    DeploymentInfo deploymentInfo = deploymentInfos.get(deploymentPaasId);
    if (deploymentInfo != null) {
      return SerializationUtils.<DeploymentInfo>clone(deploymentInfo);
    } else {
      return null;
    }

  }

  public synchronized void addStatus(PaaSTopologyDeploymentContext topo, String orchestratorDeploymentId,
      DeploymentStatus status) {
    DeploymentInfo deploymentInfo = new DeploymentInfo();
    deploymentInfo.setA4cDeploymentPaasId(topo.getDeploymentPaaSId());
    deploymentInfo.setStatus(status);
    deploymentInfo.setOrchestratorId(topo.getDeployment().getOrchestratorId());
    deploymentInfo.setOrchestratorDeploymentId(orchestratorDeploymentId);
    deploymentInfos.put(topo.getDeploymentPaaSId(), deploymentInfo);
  }

  public synchronized List<DeploymentInfoPair> getActiveDeploymentsIds() {
    return deploymentInfos.values().stream().map(di -> new DeploymentInfoPair(di.getA4cDeploymentPaasId(),
        di.getOrchestratorDeploymentId(), di.getOrchestratorId())).collect(Collectors.toList());
  }


  public synchronized void rmStatus(String a4cDeploymentPaasId) {
    deploymentInfos.remove(a4cDeploymentPaasId);
  }

  public synchronized void updateStatus(String a4cDeploymentPaasId, DeploymentStatus status,
      Map<String, String> outputs, Throwable er) {
    DeploymentInfo di = deploymentInfos.get(a4cDeploymentPaasId);
    // Checked if it wasn't removed
    if (a4cDeploymentPaasId != null) {
      di.setStatus(status);
      di.setErrorDeployment(er);
      di.setOutputs(outputs);
    }
  }

  
}
