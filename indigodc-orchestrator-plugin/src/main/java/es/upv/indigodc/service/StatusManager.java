package es.upv.indigodc.service;

import alien4cloud.paas.IPaaSCallback;
import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.InstanceInformation;
import alien4cloud.paas.model.PaaSDeploymentContext;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.service.model.DeploymentInfo;
import es.upv.indigodc.service.model.DeploymentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executor;

@Component
@Scope("prototype")
@Slf4j
//@EnableAsync
public class StatusManager implements ApplicationContextAware {

  @Autowired
  private ApplicationContext ctx;
  //protected StatusObtainer statusObtainer;

//  protected ConcurrentMap<String, DeploymentInfo> deploymentInfos;

  @Autowired
  @Qualifier("statusObtainerScheduler")
  //private ScheduledExecutorService executor;
  private Executor executor;

  @Autowired
  protected CloudConfigurationManager cloudConfigurationManager;
  
  @Autowired
  protected MappingService mappingService;

  /** The service that executes the HTTP(S) calls to the Orchestrator. */
  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;
  
//  //@Autowired
//  protected StatusManagerStorage statusManagerStorage;
  
  //protected Map<String, DeploymentInfo> deploymentInfos;

  @Override
  public void setApplicationContext(ApplicationContext ac) throws BeansException {
    this.ctx = ac;
  }

//  @PostConstruct
//  public void init() {
//    //statusManagerStorage = new StatusManagerStorage();
//    //statusManagerStorage = new StatusManagerStorage();
//    //executor = Executors.newScheduledThreadPool(1);
//    deploymentInfos = new ConcurrentHashMap<>();
//    //StatusObtainer statusObtainer = ctx.getBean(StatusObtainer.class, statusManagerStorage);
//    //executor.scheduleWithFixedDelay(statusObtainer, 0, 10, TimeUnit.SECONDS);
//    //executor.scheduleWithFixedDelay(statusObtainer, 5000);
//  }
//
//
//
//  public void initActiveDeployments(Map<String, PaaSTopologyDeploymentContext> activeDeployments) {
//    for (Map.Entry<String, PaaSTopologyDeploymentContext> topoE : activeDeployments.entrySet()) {
//      addStatus(topoE.getValue(), null, DeploymentStatus.UNKNOWN);
//    }
//
//  }
//  
//  public synchronized DeploymentInfo getDeploymentInfo(String deploymentPaasId) {
//    DeploymentInfo deploymentInfo = deploymentInfos.get(deploymentPaasId);
//    if (deploymentInfo != null) {
//      return SerializationUtils.<DeploymentInfo>clone(deploymentInfo);
//    } else {
//      return null;
//    }
//
//  }
//
//  public synchronized void addStatus(PaaSTopologyDeploymentContext topo, String orchestratorDeploymentId,
//      DeploymentStatus status) {
//    DeploymentInfo deploymentInfo = new DeploymentInfo();
//    deploymentInfo.setA4cDeploymentPaasId(topo.getDeploymentPaaSId());
//    deploymentInfo.setStatus(status);
//    deploymentInfo.setOrchestratorId(topo.getDeployment().getOrchestratorId());
//    deploymentInfo.setOrchestratorDeploymentId(orchestratorDeploymentId);
//    deploymentInfos.put(topo.getDeploymentPaaSId(), deploymentInfo);
//  }
//
//  public synchronized List<DeploymentInfoPair> getActiveDeploymentsIds() {
//    return deploymentInfos.values().stream().map(di -> new DeploymentInfoPair(di.getA4cDeploymentPaasId(),
//        di.getOrchestratorDeploymentId(), di.getOrchestratorId())).collect(Collectors.toList());
//  }
//
//
//  public synchronized void rmStatus(String a4cDeploymentPaasId) {
//    deploymentInfos.remove(a4cDeploymentPaasId);
//  }
//
//  public synchronized void updateStatus(String a4cDeploymentPaasId, DeploymentStatus status,
//      Map<String, String> outputs, Throwable er) {
//    DeploymentInfo di = deploymentInfos.get(a4cDeploymentPaasId);
//    // Checked if it wasn't removed
//    if (a4cDeploymentPaasId != null) {
//      di.setStatus(status);
//      di.setErrorDeployment(er);
//      di.setOutputs(outputs);
//    }
//  }
//
//  public synchronized DeploymentInfo getDeploymentInfo(String deploymentPaasId) {
////    DeploymentInfo deploymentInfo = deploymentInfos.get(deploymentPaasId);
////    if (deploymentInfo != null) {
////      return SerializationUtils.<DeploymentInfo>clone(deploymentInfo);
////    } else {
////      return null;
////    }
//    return statusManagerStorage.getDeploymentInfo(deploymentPaasId);
//
//  }
//
//  public synchronized void addStatus(PaaSTopologyDeploymentContext topo, String orchestratorDeploymentId,
//      DeploymentStatus status) {
////    DeploymentInfo deploymentInfo = new DeploymentInfo();
////    deploymentInfo.setA4cDeploymentPaasId(topo.getDeploymentPaaSId());
////    deploymentInfo.setStatus(status);
////    deploymentInfo.setOrchestratorId(topo.getDeployment().getOrchestratorId());
////    deploymentInfo.setOrchestratorDeploymentId(orchestratorDeploymentId);
////    deploymentInfos.put(topo.getDeploymentPaaSId(), deploymentInfo);
//    statusManagerStorage.addStatus(topo, orchestratorDeploymentId, status);
//  }
//
//  public synchronized List<DeploymentInfoPair> getActiveDeploymentsIds() {
////    return deploymentInfos.values().stream().map(di -> new DeploymentInfoPair(di.getA4cDeploymentPaasId(),
////        di.getOrchestratorDeploymentId(), di.getOrchestratorId())).collect(Collectors.toList());
//    return statusManagerStorage.getActiveDeploymentsIds();
//  }
//
//  @PreDestroy
//  public void destroy() {
//    log.info("StatusManager destroy");
//    executor.shutdown();
//  }
//
//  public synchronized void rmStatus(String orchestratoId, String a4cDeploymentPaasId) {
////    deploymentInfos.remove(a4cDeploymentPaasId);
//    statusManagerStorage.rmStatus(a4cDeploymentPaasId);
//  }
//
//  public synchronized void updateStatus(String a4cDeploymentPaasId, DeploymentStatus status,
//      Map<String, String> outputs, Throwable er) {
////    DeploymentInfo di = deploymentInfos.get(a4cDeploymentPaasId);
////    // Checked if it wasn't removed
////    if (a4cDeploymentPaasId != null) {
////      di.setStatus(status);
////      di.setErrorDeployment(er);
////      di.setOutputs(outputs);
////    }
//    
//    statusManagerStorage.updateStatus(a4cDeploymentPaasId, status, outputs, er);
//  }
  
  public void getStatus(PaaSDeploymentContext deploymentContext, 
      IPaaSCallback<DeploymentStatus> callback) {
    final DeploymentInfo di =  mappingService.getByA4CDeploymentPaasId(
    		deploymentContext.getDeploymentPaaSId());
//<<<<<<< HEAD
//      String token = connRepository.getPrimaryConnection(Oidc.class).createData().getAccessToken();
      
//=======
//      //String token = connRepository.getPrimaryConnection(Oidc.class).createData().getAccessToken();
//      StatusObtainer so = ctx.getBean(StatusObtainer.class, null, callback, di);
//>>>>>>> spring-social-plugin-integration
    if (di != null) {
    	StatusObtainer so = ctx.getBean(StatusObtainer.class, callback, di);
      	executor.execute(so);
    } else
    	callback.onFailure(new DeploymentNotFoundException(deploymentContext.getDeploymentPaaSId(),
    			deploymentContext.getDeploymentId()));
    //so.run();
  }
  
  public void getInstancesInformation(PaaSTopologyDeploymentContext deploymentContext,
      IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback) {
      
    
  }

}
