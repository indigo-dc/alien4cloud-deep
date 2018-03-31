package es.upv.indigodc;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import alien4cloud.exception.NotFoundException;
import alien4cloud.orchestrators.plugin.ILocationConfiguratorPlugin;
import alien4cloud.orchestrators.plugin.IOrchestratorPlugin;
import alien4cloud.orchestrators.plugin.model.PluginArchive;
import alien4cloud.paas.IPaaSCallback;
import alien4cloud.paas.exception.MaintenanceModeException;
import alien4cloud.paas.exception.OperationExecutionException;
import alien4cloud.paas.exception.PaaSAlreadyDeployedException;
import alien4cloud.paas.exception.PluginConfigurationException;
import alien4cloud.paas.model.AbstractMonitorEvent;
import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.InstanceInformation;
import alien4cloud.paas.model.NodeOperationExecRequest;
import alien4cloud.paas.model.PaaSDeploymentContext;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.location.LocationConfiguratorFactory;
import es.upv.indigodc.service.BuilderService;
import es.upv.indigodc.service.EventService;
import es.upv.indigodc.service.MappingService;
import es.upv.indigodc.service.OrchestratorConnector;
import es.upv.indigodc.service.SslContextBuilder;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("indigodc-orchestrator")
@Scope("prototype")
public class IndigoDCOrchestrator  implements IOrchestratorPlugin<CloudConfiguration>{
  
  public static String TYPE = "IndigoDC";
  
  
  @Resource(name = "cloud-configuration-manager")
  private CloudConfigurationManager cloudConfigurationHolder;
  
  @Resource(name = "orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;
  
  @Resource(name = "builder-service")
  private BuilderService builderService;
  
  @Resource(name = "mapping-service")
  private MappingService mappingService;
  
  @Inject
  private LocationConfiguratorFactory locationConfiguratorFactory;  

  @Inject
  private EventService eventService;
  
  @Override
  public void init(Map<String, PaaSTopologyDeploymentContext> activeDeployments) {
    mappingService.init(activeDeployments.values());    
  }
  
  @Override
  public void setConfiguration(String orchestratorId, CloudConfiguration configuration)
      throws PluginConfigurationException {
    if (configuration == null) {
      throw new PluginConfigurationException("Configuration must not be null");
    }
    cloudConfigurationHolder.setCloudConfiguration(orchestratorId, configuration);
    
  }

  @Override
  public void deploy(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    CloudConfiguration configuration = cloudConfigurationHolder.getCloudConfiguration();//deploymentContext.getDeployment().getOrchestratorId();
    String yamlPaasTopology;
    try {
      yamlPaasTopology = builderService.buildApp(deploymentContext, 1);
      OrchestratorResponse response = orchestratorConnector.callDeploy(configuration, yamlPaasTopology);
      String uuid = OrchestratorConnector.getUUIDTopologyDeployment(response);
      log.info("Deployment paas id: " + deploymentContext.getDeploymentPaaSId());
      log.info("uuid: " + uuid);
      mappingService.registerDeploymentInfo(uuid, 
    		  deploymentContext.getDeploymentPaaSId(), DeploymentStatus.DEPLOYMENT_IN_PROGRESS);
    } catch (NoSuchFieldException | 
        IOException e) {
      e.printStackTrace();
      callback.onFailure(e);
      log.error(e.toString());
    }

    callback.onSuccess(null);
  }
  
  @Override
  public ILocationConfiguratorPlugin getConfigurator(String locationType) {
    return locationConfiguratorFactory.newInstance(locationType);
  }
  
  @Override
  public void undeploy(PaaSDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    // TODO: Add force option in Marathon-client to always force undeployment - better : cancel running deployment
 
      CloudConfiguration configuration = cloudConfigurationHolder.getCloudConfiguration();//deploymentContext.getDeployment().getOrchestratorId();

      OrchestratorResponse result;
      try {
    	String UUIDTopologyDeployment = mappingService.getByAlienDeploymentId(deploymentContext.getDeploymentPaaSId()).getIndigoDCDeploymentId();
        result = orchestratorConnector.callUndeploy(configuration, deploymentContext.getDeploymentPaaSId());
        log.info("Deployment paas id: " + deploymentContext.getDeploymentPaaSId());
        log.info("uuid: " + UUIDTopologyDeployment);
        mappingService.registerDeploymentInfo(UUIDTopologyDeployment, deploymentContext.getDeploymentPaaSId(), DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS);
      } catch (Exception e) {
  		log.error(Util.throwableToString(e));
        callback.onFailure(e);
      }

    callback.onSuccess(null);
  }


  @Override
  public void getEventsSince(Date date, int maxEvents, IPaaSCallback<AbstractMonitorEvent[]> eventCallback) {
	// TODO: implement an event listener for the orchestrator
    eventCallback.onSuccess(eventService.flushEvents());
    
  }

  @Override
  public void getInstancesInformation(PaaSTopologyDeploymentContext deploymentContext,
      IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback) {
    // TODO Auto-generated method stub
	  log.info("call");
    
  }

  @Override
  public void getStatus(PaaSDeploymentContext deploymentContext, IPaaSCallback<DeploymentStatus> callback) {
	  log.info("call");
    final String a4cDeploymentId = deploymentContext.getDeploymentPaaSId();
	String UUIDTopologyDeployment = mappingService.getByAlienDeploymentId(deploymentContext.getDeploymentPaaSId()).getIndigoDCDeploymentId();
    final CloudConfiguration configuration = cloudConfigurationHolder.getCloudConfiguration();
    try {
    	OrchestratorResponse response = orchestratorConnector.callDeploymentStatus(configuration, UUIDTopologyDeployment);

        callback.onSuccess(getIndigoDCDeploymentStatus(response));
    } catch (RuntimeException e) {
		log.error(Util.throwableToString(e));
    	callback.onFailure(e.getCause());
    } catch (NoSuchFieldException e) {
		log.error(Util.throwableToString(e));
    	callback.onFailure(e.getCause());
	} catch (IOException e) {
		log.error(Util.throwableToString(e));
    	callback.onFailure(e.getCause());
	}
    
  }
  
  protected DeploymentStatus getIndigoDCDeploymentStatus(OrchestratorResponse orchestratorResponse) throws JsonProcessingException, IOException {
	  String status = OrchestratorConnector.getStatusTopologyDeployment(orchestratorResponse).toUpperCase();
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



  @Override
  public void update(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<PluginArchive> pluginArchives() {
    return Collections.emptyList();
  }
  
  
  /********Not implemented*/
  
  @Override
  public void scale(PaaSDeploymentContext deploymentContext, String nodeTemplateId, int instances,
      IPaaSCallback<?> callback) {
    throw new NotImplementedException();
    
  }
  
  @Override
  public void launchWorkflow(PaaSDeploymentContext deploymentContext, String workflowName, Map<String, Object> inputs,
      IPaaSCallback<?> callback) {
    throw new NotImplementedException();
    
  }
  

  @Override
  public void executeOperation(PaaSTopologyDeploymentContext deploymentContext, NodeOperationExecRequest request,
      IPaaSCallback<Map<String, String>> operationResultCallback) throws OperationExecutionException {
    throw new NotImplementedException();
    
  }
  
  @Override
  public void switchInstanceMaintenanceMode(PaaSDeploymentContext deploymentContext, String nodeId, String instanceId,
      boolean maintenanceModeOn) throws MaintenanceModeException {
    throw new NotImplementedException();
    
  }

  @Override
  public void switchMaintenanceMode(PaaSDeploymentContext deploymentContext, boolean maintenanceModeOn)
      throws MaintenanceModeException {
    throw new NotImplementedException();
    
  }
  


}
