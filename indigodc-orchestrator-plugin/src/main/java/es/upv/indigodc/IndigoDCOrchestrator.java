package es.upv.indigodc;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Collection;
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
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;

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
import alien4cloud.paas.model.InstanceStatus;
import alien4cloud.paas.model.NodeOperationExecRequest;
import alien4cloud.paas.model.PaaSDeploymentContext;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.Util;
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
  
  public static String TMP_ORCHETRATOR_DEMO = "{\n" + 
      "  \"template\" : \"tosca_definitions_version: tosca_simple_yaml_1_0\\n\\nimports:\\n  - indigo_custom_types: https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml\\n\\ndescription: >\\n  TOSCA test for launching a Kubernetes Virtual Cluster.\\n\\ntopology_template:\\n  inputs:\\n    wn_num:\\n      type: integer\\n      description: Number of WNs in the cluster\\n      default: 1\\n      required: yes\\n    fe_cpus:\\n      type: integer\\n      description: Numer of CPUs for the front-end node\\n      default: 2\\n      required: yes\\n    fe_mem:\\n      type: scalar-unit.size\\n      description: Amount of Memory for the front-end node\\n      default: 2 GB\\n      required: yes\\n    wn_cpus:\\n      type: integer\\n      description: Numer of CPUs for the WNs\\n      default: 1\\n      required: yes\\n    wn_mem:\\n      type: scalar-unit.size\\n      description: Amount of Memory for the WNs\\n      default: 2 GB\\n      required: yes\\n\\n    admin_username:\\n      type: string\\n      description: Username of the admin user\\n      default: kubeuser\\n    admin_token:\\n      type: string\\n      description: Access Token for the admin user\\n      default: not_very_secret_token\\n\\n  node_templates:\\n\\n    jupyterhub:\\n      type: tosca.nodes.indigo.JupyterHub\\n      properties:\\n        spawner: kubernetes\\n      requirements:\\n        - host: lrms_server\\n        - dependency: lrms_front_end\\n\\n    lrms_front_end:\\n      type: tosca.nodes.indigo.LRMS.FrontEnd.Kubernetes\\n      properties:\\n        admin_username:  { get_input: admin_username }\\n        admin_token: { get_input: admin_token }\\n      requirements:\\n        - host: lrms_server\\n\\n    lrms_server:\\n      type: tosca.nodes.indigo.Compute\\n      capabilities:\\n        endpoint:\\n          properties:\\n            dns_name: kubeserver\\n            network_name: PUBLIC\\n            port: 8000\\n            protocol: tcp\\n        host:\\n          properties:\\n            num_cpus: { get_input: fe_cpus }\\n            mem_size: { get_input: fe_mem }\\n        os:\\n          properties:\\n            image: ubuntu-16.04-vmi\\n            #type: linux\\n            #distribution: ubuntu\\n            #version: 16.04\\n\\n    wn_node:\\n      type: tosca.nodes.indigo.LRMS.WorkerNode.Kubernetes\\n      properties:\\n        front_end_ip: { get_attribute: [ lrms_server, private_address, 0 ] }\\n      requirements:\\n        - host: lrms_wn\\n\\n    lrms_wn:\\n      type: tosca.nodes.indigo.Compute\\n      capabilities:\\n        scalable:\\n          properties:\\n            count: { get_input: wn_num }\\n        host:\\n          properties:\\n            num_cpus: { get_input: wn_cpus }\\n            mem_size: { get_input: wn_mem }\\n        os:\\n          properties:\\n            image: ubuntu-16.04-vmi\\n            #type: linux\\n            #distribution: ubuntu\\n            #version: 16.04\\n\\n  outputs:\\n    jupyterhub_url:\\n      value: { concat: [ 'http://', get_attribute: [ lrms_server, public_address, 0 ], ':8000' ] }\\n    cluster_ip:\\n      value: { get_attribute: [ lrms_server, public_address, 0 ] }\\n    cluster_creds:\\n      value: { get_attribute: [ lrms_server, endpoint, credential, 0 ] }\",\n" + 
      "  \"parameters\" : {\n" + 
      "    \n" + 
      "  }\n" + 
      "}";
  
  
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
      yamlPaasTopology = TMP_ORCHETRATOR_DEMO;//builderService.buildApp(deploymentContext, 1);
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
    	log.info("Deployment paas id: " + deploymentContext.getDeploymentPaaSId());
        log.info("uuid: " + UUIDTopologyDeployment);
    	result = orchestratorConnector.callUndeploy(configuration, UUIDTopologyDeployment);        
        mappingService.registerDeploymentInfo(UUIDTopologyDeployment, deploymentContext.getDeploymentPaaSId(), 
            DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS);
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
    //log.info("call getEventsSince");
    
  }

  @Override
  public void getInstancesInformation(PaaSTopologyDeploymentContext deploymentContext,
      IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback) {
    // TODO Auto-generated method stub
	  log.info("call getInstancesInformation");
//      final Map<String, Map<String, InstanceInformation>> topologyInfo = newHashMap();
//      final String groupID = deploymentContext.getDeploymentPaaSId();
//      deploymentContext.getPaaSTopology().getNonNatives().forEach(paaSNodeTemplate -> {
//          Map<String, InstanceInformation> instancesInfo = newHashMap();
//          final String appID = groupID + "/" + paaSNodeTemplate.getId().toLowerCase();
//
//          try {
//              // Marathon tasks are alien instances
//              final Collection<Task> tasks = marathonClient.getAppTasks(appID).getTasks();
//              tasks.forEach(task -> {
//                  final InstanceInformation instanceInformation = this.getInstanceInformation(task);
//                  instancesInfo.put(task.getId(), instanceInformation);
//              });
//
//              topologyInfo.put(paaSNodeTemplate.getId(), instancesInfo);
//          } catch (Exception e) {
//              switch (e.getStatus()) {
//              case 404: // The app cannot be found in marathon - we display no information
//                  break;
//              default:
//            	  callback.onFailure(e);
//              }
//          }
//      });
//      callback.onSuccess(topologyInfo);
  }
//  
//  protected InstanceInformation getInstanceInformation(Task task) {
//      final Map<String, String> runtimeProps = newHashMap();
//
//      // Outputs Marathon endpoints as host:port1,port2, ...
//      final Collection<String> ports = Collections2.transform(task.getPorts(), Functions.toStringFunction());
//      runtimeProps.put("endpoint", "http://".concat(task.getHost().concat(":").concat(String.join(",", ports))));
//
//      InstanceStatus instanceStatus;
//      String state;
//
//      // Leverage Mesos's TASK_STATUS - TODO: add Mesos 1.0 task states
//      switch (task.getState()) {
//      case "TASK_RUNNING":
//          state = "started";
//          // Retrieve health checks results - if no healthcheck then assume healthy
//          instanceStatus = Optional.ofNullable(task.getHealthCheckResults())
//                  .map(healthCheckResults -> healthCheckResults.stream().findFirst().map(HealthCheckResult::isAlive)
//                          .map(alive -> alive ? InstanceStatus.SUCCESS : InstanceStatus.FAILURE).orElse(InstanceStatus.PROCESSING))
//                  .orElse(InstanceStatus.SUCCESS);
//          break;
//      case "TASK_STARTING":
//          state = "starting";
//          instanceStatus = InstanceStatus.PROCESSING;
//          break;
//      case "TASK_STAGING":
//          state = "creating";
//          instanceStatus = InstanceStatus.PROCESSING;
//          break;
//      case "TASK_ERROR":
//          state = "stopped";
//          instanceStatus = InstanceStatus.FAILURE;
//          break;
//      default:
//          state = "uninitialized"; // Unknown
//          instanceStatus = InstanceStatus.PROCESSING;
//      }
//
//      return new InstanceInformation(state, instanceStatus, runtimeProps, runtimeProps, newHashMap());
//  }

  @Override
  public void getStatus(PaaSDeploymentContext deploymentContext, IPaaSCallback<DeploymentStatus> callback) {
	  log.info("call get status");
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
