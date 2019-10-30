package es.upv.indigodc;

import alien4cloud.exception.NotFoundException;
import alien4cloud.model.deployment.DeploymentTopology;
import alien4cloud.orchestrators.plugin.ILocationConfiguratorPlugin;
import alien4cloud.orchestrators.plugin.IOrchestratorPlugin;
import alien4cloud.orchestrators.plugin.model.PluginArchive;
import alien4cloud.paas.IPaaSCallback;
import alien4cloud.paas.exception.MaintenanceModeException;
import alien4cloud.paas.exception.OperationExecutionException;
import alien4cloud.paas.exception.PluginConfigurationException;
import alien4cloud.paas.model.AbstractMonitorEvent;
import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.InstanceInformation;
import alien4cloud.paas.model.InstanceStatus;
import alien4cloud.paas.model.NodeOperationExecRequest;
import alien4cloud.paas.model.PaaSDeploymentContext;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.location.LocationConfiguratorFactory;
import es.upv.indigodc.service.BuilderService;
import es.upv.indigodc.service.EventService;
import es.upv.indigodc.service.MappingService;
import es.upv.indigodc.service.OrchestratorConnector;
import es.upv.indigodc.service.model.*;

import java.io.IOException;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import es.upv.indigodc.service.model.response.DeploymentOrchestrator;
import es.upv.indigodc.service.model.response.GetDeploymentsResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Exposes the methods that allow the operations with the Orchestrator.
 *
 * @author asalic
 */
@Slf4j
@Component("indigodc-orchestrator")
@Scope("prototype")
public class IndigoDcOrchestrator implements IOrchestratorPlugin<CloudConfiguration> {

  public final static String DEFAULT_NOT_SET_OUTPUT_VALUE = "<not_set>";
  public final static String TYPE = "IndigoDC";

  protected final static Pattern NOT_FOUND_MATCHER = Pattern.compile("(?=.*not)(?=.*found)");

  private ReentrantLock updateStatusLock = new ReentrantLock();

  /**
   * The configuration manager used to obtain the
   * {@link es.upv.indigodc.configuration.CloudConfiguration} instance that holds
   * the parameters of the plugin.
   */
  @Autowired
  @Qualifier("cloud-configuration-manager")
  private CloudConfigurationManager cloudConfigurationManager;

  /** The service that executes the HTTP(S) calls to the Orchestrator. */
  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;

  /**
   * The service that creates the payload (which includes the TOSCA topologies)
   * that is sent to the Orchestrator using {@link #orchestratorConnector}.
   */
  @Autowired
  @Qualifier("builder-service")
  private BuilderService builderService;

  /** Maintain the IDs of the launched topologies. */
  @Autowired
  @Qualifier("mapping-service")
  private MappingService mappingService;

  /**
   * Manages the instantiation of a new location configurator using a location
   * type.
   */
  @Inject
  private LocationConfiguratorFactory locationConfiguratorFactory;

  /** Manages the events produced by the Orchestrator. */
  @Inject
  private EventService eventService;
//
//  @Autowired
//  private StatusManager statusManager;
//
  @Getter
  protected String orchestratorId;

  @Override
  public void init(Map<String, PaaSTopologyDeploymentContext> activeDeployments) {
    if (activeDeployments != null) {
      //statusManager.initActiveDeployments(activeDeployments);
    	mappingService.init(activeDeployments);
    }
  }

  /**
   * Method called when this instance is scrapped.
   */
//  @PreDestroy
  public void destroy() {
    log.info("IndigoDcOrchestrator destroy");
    eventService.unsubscribe(orchestratorId);
   // statusManager.destroy();
  }

  @Override
  public void setConfiguration(String orchestratorId, CloudConfiguration configuration)
      throws PluginConfigurationException {
    if (configuration == null) {
      throw new PluginConfigurationException("Configuration must not be null");
    }
    this.orchestratorId = orchestratorId;
    cloudConfigurationManager.addCloudConfiguration(orchestratorId, configuration);
    eventService.subscribe(configuration, orchestratorId);
  }

  @Override
  public void deploy(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    CloudConfiguration configuration = cloudConfigurationManager
        .getCloudConfiguration(deploymentContext.getDeployment().getOrchestratorId());
    String a4cUuidDeployment = deploymentContext.getDeploymentPaaSId();//.getDeployment().getId();

    try {
      final String yamlPaasTopology = builderService.buildApp(deploymentContext,
          configuration.getImportIndigoCustomTypes());
      log.info("Deploying on: " + configuration.getOrchestratorEndpoint());
      log.info("Topology: " + yamlPaasTopology);
      OrchestratorResponse response = orchestratorConnector.callDeploy(configuration.getOrchestratorEndpoint(),
              yamlPaasTopology);
      log.info(response.toString());
      final String orchestratorUuidDeployment = response.getOrchestratorUuidDeployment();
      log.info("uuid a4c: " + a4cUuidDeployment);
      log.info("uuid orchestrator: " + orchestratorUuidDeployment);
      
      mappingService.registerDeployment(new DeploymentInfo(a4cUuidDeployment, orchestratorUuidDeployment,
              deploymentContext.getDeploymentId(),
          deploymentContext.getDeployment().getOrchestratorId(), 
          DeploymentStatus.DEPLOYMENT_IN_PROGRESS, null, null));
      // eventService.subscribe(configuration);
      //statusManager.addStatus(deploymentContext, orchestratorUuidDeployment, DeploymentStatus.DEPLOYMENT_IN_PROGRESS);
      callback.onSuccess(null);
    } catch (NoSuchFieldException | IOException | OrchestratorIamException er) {
      callback.onFailure(er);
      log.error("Error deployment", er);
//      mappingService.registerDeployment(new DeploymentInfo(a4cUuidDeployment,
//    		  null,
//              deploymentContext.getDeploymentId(),
//          deploymentContext.getDeployment().getOrchestratorId(),
//          DeploymentStatus.FAILURE, null, null));
      //mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment, DeploymentStatus.FAILURE);
    }
  }

  @Override
  public ILocationConfiguratorPlugin getConfigurator(String locationType) {
    return locationConfiguratorFactory.newInstance(locationType);
  }

  @Override
  public void undeploy(PaaSDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    final CloudConfiguration configuration = cloudConfigurationManager
        .getCloudConfiguration(deploymentContext.getDeployment().getOrchestratorId());
    String a4cUuidDeployment = deploymentContext.getDeploymentPaaSId();//.getDeployment().getId();
    try {
      DeploymentInfo deploymentInfo = mappingService.getByA4CDeploymentPaasId(a4cUuidDeployment);
//          statusManager
//          getDeploymentInfo(deploymentContext.getDeploymentPaaSId());
      if (deploymentInfo != null) {
        final String orchestratorUuidDeployment = deploymentInfo.getOrchestratorDeploymentId();
        log.info("Deployment paas id: " + a4cUuidDeployment);
        log.info("uuid: " + orchestratorUuidDeployment);
        final OrchestratorResponse response = orchestratorConnector.callUndeploy(configuration.getOrchestratorEndpoint(),
            orchestratorUuidDeployment);
        log.info(response.toString());
//        mappingService.registerDeploymentInfo(orchestratorUuidDeployment, a4cUuidDeployment,
//            deploymentContext.getDeployment().getOrchestratorId(), DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS);
//        statusManager.updateStatus(deploymentContext.getDeploymentPaaSId(), 
//            DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS, deploymentInfo.getOutputs(), null);
        mappingService.unregisterDeployment(a4cUuidDeployment);
        callback.onSuccess(null);
        // eventService.unsubscribe();
      } else {
        //callback.onFailure(new NotFoundException(
            log.info(String.format("Deployment with ID %s not found in the list of deployments",
                deploymentContext.getDeploymentPaaSId()));
            callback.onSuccess(null);
          //));
      }
        
    } catch (IOException er) {
      log.error("Error undeployment", er);
      callback.onFailure(er);
      //mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment, DeploymentStatus.FAILURE);
    } catch (NoSuchFieldException er) {
      log.error("Error undeployment", er);
      callback.onFailure(er);
     // mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment, DeploymentStatus.FAILURE);
    } catch (OrchestratorIamException er) {
      switch (er.getHttpCode()) {
        case 404:
          if (NOT_FOUND_MATCHER.matcher(er.getTitle().toLowerCase()).find()) {
            log.warn("Deployment not found ", er);
            mappingService.unregisterDeployment(a4cUuidDeployment);
            callback.onSuccess(null);
          } else {
            callback.onFailure(er);
            log.error("Error undeployment ", er);
          }
          break;
        default:
          log.error("Error undeployment ", er);
          callback.onFailure(er);
      }
      //mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment, DeploymentStatus.FAILURE);
    }
  }

  @Override
  public void getEventsSince(Date date, int maxEvents, IPaaSCallback<AbstractMonitorEvent[]> eventCallback) {
    eventCallback.onSuccess(eventService.flushEvents(date, maxEvents));
    // log.info("call getEventsSince");

  }

  @Override
  public void getInstancesInformation(PaaSTopologyDeploymentContext deploymentContext,
      IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback) {
    log.info("call getInstancesInformation");
    final DeploymentInfo di =  mappingService.getByA4CDeploymentPaasId(
    		deploymentContext.getDeploymentPaaSId());
    final String groupId = deploymentContext.getDeploymentPaaSId();
    final Map<String, Map<String, InstanceInformation>> topologyInfo = new HashMap<>();
    final Map<String, String> runtimeProps = new HashMap<>();
    final Map<String, InstanceInformation> instancesInfo = new HashMap<>();
    final Map<String, String> outputsVars = extractTopologyOutputs(deploymentContext
            .getDeploymentTopology());
    if (di != null) {
    	try {
          if (di.getOrchestratorDeploymentId() != null) {
            final CloudConfiguration configuration = cloudConfigurationManager
                    .getCloudConfiguration(deploymentContext
                            .getDeployment().getOrchestratorId());
            OrchestratorResponse response = orchestratorConnector
                    .callDeploymentStatus(
                            configuration.getOrchestratorEndpoint(),
                            di.getOrchestratorDeploymentId());
            log.info(response.toString());

            //log.info(response.getResponse().toString());

            Util.InstanceStatusInfo instanceStatusInfo = Util
                    .indigoDcStatusToInstanceStatus(response.getStatusTopologyDeployment().toUpperCase());
            Map<String, String> outputsOrchestrator = response.getOutputs();
            if (outputsOrchestrator != null) {
              outputsOrchestrator.entrySet().stream()
                  .forEach(outputE -> outputsVars.replace(outputE.getKey(), outputE.getValue()));
              di.setOutputs(outputsVars);
            }
            final InstanceInformation instanceInformation = new InstanceInformation(
                    instanceStatusInfo.getState(),
                    instanceStatusInfo.getInstanceStatus(), runtimeProps, runtimeProps,
                    // outputs);
                    outputsVars, outputsVars);
            instancesInfo.put(di.getA4cDeploymentPaasId(), instanceInformation);
            topologyInfo.put(groupId, instancesInfo);
            callback.onSuccess(topologyInfo);
          }
      } catch (StatusNotFoundException | NoSuchFieldException | IOException e) {
        e.printStackTrace();
        callback.onFailure(e);
        } catch (OrchestratorIamException er) {
          final InstanceInformation instanceInformation = 
        		  new InstanceInformation(IndigoDcDeploymentStatus.UNKNOWN.name(),
                          InstanceStatus.FAILURE,
              runtimeProps, runtimeProps, outputsVars, outputsVars);
          instancesInfo.put(di.getA4cDeploymentPaasId(), instanceInformation);
          topologyInfo.put(di.getA4cDeploymentPaasId(), instancesInfo);
          callback.onSuccess(topologyInfo);
          instancesInfo.put(di.getA4cDeploymentPaasId(), instanceInformation);
          topologyInfo.put(di.getA4cDeploymentPaasId(), instancesInfo);
          switch (er.getHttpCode()) {
          case 404:
            callback.onSuccess(topologyInfo);
            break;
          default:
            callback.onFailure(er);
          }
          log.error("Error deployment ", er);
        } 

          // Map<String, String> outputs = new HashMap<>();
          // outputs.put("Compute_public_address", "none");
          // runtimeProps.put("Compute_public_address", "value");
//          DeploymentInfo deploymentInfo = statusManager.
//              getDeploymentInfo(deploymentContext.getDeploymentPaaSId());
//         if (deploymentInfo != null) {
//           final String groupId = deploymentContext.getDeploymentPaaSId();
//           final Map<String, Map<String, InstanceInformation>> topologyInfo = new HashMap<>();
//           final Map<String, String> runtimeProps = new HashMap<>();
//           final Map<String, InstanceInformation> instancesInfo = new HashMap<>();
//           final Map<String, String> outputsVars = extractTopologyOutputs(deploymentContext.getDeploymentTopology());
//           Util.InstanceStatusInfo instanceStatusInfo;
//          try {
//            instanceStatusInfo = Util
//               .deploymentStatusToInstanceStatus(deploymentInfo.getStatus());
//            if (deploymentInfo.hasOutputs()) {
//              deploymentInfo.getOutputs().entrySet().stream()
//                  .forEach(outputE -> outputsVars.replace(outputE.getKey(), outputE.getValue()));
//            }
//            final InstanceInformation instanceInformation = new InstanceInformation(instanceStatusInfo.getState(),
//                instanceStatusInfo.getInstanceStatus(), runtimeProps, runtimeProps,
//                // outputs);
//                outputsVars, outputsVars);
//            instancesInfo.put(deploymentInfo.getA4cDeploymentPaasId(), instanceInformation);
//            topologyInfo.put(groupId, instancesInfo);
//            callback.onSuccess(topologyInfo);
//          } catch (StatusNotFoundException e) {
//            e.printStackTrace();
//            callback.onFailure(e);
//          }
////        } catch (NoSuchFieldException er) {
////          callback.onFailure(er);
////          log.error("Error getInstancesInformation", er);
////        } catch (IOException er) {
////          callback.onFailure(er);
////          log.error("Error getInstancesInformation", er);
////        } catch (OrchestratorIamException er) {
////          final InstanceInformation instanceInformation = new InstanceInformation("UNKNOWN", InstanceStatus.FAILURE,
////              runtimeProps, runtimeProps, outputsVars, outputsVars);
////          instancesInfo.put(a4cUuidDeployment, instanceInformation);
////          topologyInfo.put(a4cUuidDeployment, instancesInfo);
////          callback.onSuccess(topologyInfo);
////          instancesInfo.put(a4cUuidDeployment, instanceInformation);
////          topologyInfo.put(a4cUuidDeployment, instancesInfo);
////          switch (er.getHttpCode()) {
////          case 404:
////            callback.onSuccess(topologyInfo);
////            break;
////          default:
////            callback.onFailure(er);
////          }
////          log.error("Error deployment ", er);
////        } catch (StatusNotFoundException er) {
////          callback.onFailure(er);
////          log.error("Error deployment ", er);
////        }
    } else {
      log.warn("Deployment with DeploymentPaaSId " + deploymentContext.getDeploymentPaaSId() + " not registered in the plugin registry." );
      final InstanceInformation instanceInformation = new InstanceInformation(
              IndigoDcDeploymentStatus.UNDEPLOYED.name(),
              InstanceStatus.SUCCESS, runtimeProps, runtimeProps,
              // outputs);
              outputsVars, outputsVars);
      instancesInfo.put(deploymentContext.getDeploymentPaaSId(), instanceInformation);
      topologyInfo.put(groupId, instancesInfo);
      callback.onSuccess(topologyInfo);
    }
//    String a4cUuidDeployment = deploymentContext.getDeployment().getId();
//    // deploymentContext.getDeploymentTopology().get
//    final String groupId = deploymentContext.getDeploymentPaaSId();
//    // final String
//    final OrchestratorDeploymentMapping orchestratorDeploymentMapping = mappingService
//        .getByAlienDeploymentId(a4cUuidDeployment);
    
//    if (orchestratorDeploymentMapping != null) {
//      final String orchestratorUuidDeployment = orchestratorDeploymentMapping.getOrchestratorUuidDeployment();
//      // .getDeploymentId();//.getDeploymentPaaSId();
//
//      final CloudConfiguration configuration = cloudConfigurationManager
//          .getCloudConfiguration(deploymentContext.getDeployment().getOrchestratorId());
//      try {
//        OrchestratorResponse response = orchestratorConnector.callDeploymentStatus(configuration,
//            orchestratorUuidDeployment);
//
//        log.info(response.getResponse().toString());
//        Util.InstanceStatusInfo instanceStatusInfo = Util
//            .indigoDcStatusToInstanceStatus(response.getStatusTopologyDeployment().toUpperCase());

        // Map<String, String> outputs = new HashMap<>();
        // outputs.put("Compute_public_address", "none");
        // runtimeProps.put("Compute_public_address", "value");
//        DeploymentInfo deploymentInfo = statusManager.
//            getDeploymentInfo(deploymentContext.getDeploymentPaaSId());
//       if (deploymentInfo != null) {
//         final String groupId = deploymentContext.getDeploymentPaaSId();
//         final Map<String, Map<String, InstanceInformation>> topologyInfo = new HashMap<>();
//         final Map<String, String> runtimeProps = new HashMap<>();
//         final Map<String, InstanceInformation> instancesInfo = new HashMap<>();
//         final Map<String, String> outputsVars = extractTopologyOutputs(deploymentContext.getDeploymentTopology());
//         Util.InstanceStatusInfo instanceStatusInfo;
//        try {
//          instanceStatusInfo = Util
//             .deploymentStatusToInstanceStatus(deploymentInfo.getStatus());
//          if (deploymentInfo.hasOutputs()) {
//            deploymentInfo.getOutputs().entrySet().stream()
//                .forEach(outputE -> outputsVars.replace(outputE.getKey(), outputE.getValue()));
//          }
//          final InstanceInformation instanceInformation = new InstanceInformation(instanceStatusInfo.getState(),
//              instanceStatusInfo.getInstanceStatus(), runtimeProps, runtimeProps,
//              // outputs);
//              outputsVars, outputsVars);
//          instancesInfo.put(deploymentInfo.getA4cDeploymentPaasId(), instanceInformation);
//          topologyInfo.put(groupId, instancesInfo);
//          callback.onSuccess(topologyInfo);
//        } catch (StatusNotFoundException e) {
//          e.printStackTrace();
//          callback.onFailure(e);
//        }
////      } catch (NoSuchFieldException er) {
////        callback.onFailure(er);
////        log.error("Error getInstancesInformation", er);
////      } catch (IOException er) {
////        callback.onFailure(er);
////        log.error("Error getInstancesInformation", er);
////      } catch (OrchestratorIamException er) {
////        final InstanceInformation instanceInformation = new InstanceInformation("UNKNOWN", InstanceStatus.FAILURE,
////            runtimeProps, runtimeProps, outputsVars, outputsVars);
////        instancesInfo.put(a4cUuidDeployment, instanceInformation);
////        topologyInfo.put(a4cUuidDeployment, instancesInfo);
////        callback.onSuccess(topologyInfo);
////        instancesInfo.put(a4cUuidDeployment, instanceInformation);
////        topologyInfo.put(a4cUuidDeployment, instancesInfo);
////        switch (er.getHttpCode()) {
////        case 404:
////          callback.onSuccess(topologyInfo);
////          break;
////        default:
////          callback.onFailure(er);
////        }
////        log.error("Error deployment ", er);
////      } catch (StatusNotFoundException er) {
////        callback.onFailure(er);
////        log.error("Error deployment ", er);
////      }
//    } else {
//      callback.onFailure(new NotFoundException(
//          String.format("Deployment with ID %s not found in the list of deployments", 
//              deploymentContext.getDeploymentPaaSId())));
//    }
  }

  @Override
  public void getStatus(PaaSDeploymentContext deploymentContext, 
      IPaaSCallback<DeploymentStatus> callback) {
    log.info("call get status");
    //statusManager.getStatus(deploymentContext, callback);
    final DeploymentInfo di =  mappingService.getByA4CDeploymentPaasId(
    		deploymentContext.getDeploymentPaaSId());
    if (di != null) {
	    try {
	      if (di.getOrchestratorDeploymentId() != null) {
            CloudConfiguration configuration = cloudConfigurationManager
                    .getCloudConfiguration(deploymentContext
                            .getDeployment().getOrchestratorId());
            final OrchestratorResponse response = orchestratorConnector.callDeploymentStatus(
                    configuration.getOrchestratorEndpoint(),
                    di.getOrchestratorDeploymentId());
            log.info(response.toString());
            final String statusTopologyDeployment = response.getStatusTopologyDeployment();
            callback.onSuccess(Util.indigoDcStatusToDeploymentStatus(statusTopologyDeployment));
          } else
            updateStatusesDeploymentsUser(deploymentContext, callback, di);
	    } catch (NoSuchFieldException | IOException | StatusNotFoundException e) {
	      e.printStackTrace();
	      callback.onFailure(e);
	    } catch (OrchestratorIamException e) {
	        int code = e.getHttpCode();
	        switch (code) {
	            case 401: break;
	            default: break;
	        }
	      callback.onFailure(e);
	      e.printStackTrace();
	    }
    } else {
      log.warn("Deployment with DeploymentPaaSId " + deploymentContext.getDeploymentPaaSId() +
              " not registered in the plugin registry." );
      callback.onSuccess(DeploymentStatus.UNDEPLOYED);
    }

  }

  protected void updateStatusesDeploymentsUser(PaaSDeploymentContext deploymentContext,
                                               IPaaSCallback<DeploymentStatus> callback,
                                               final DeploymentInfo di) {

    try {
      updateStatusLock.tryLock();
      final CloudConfiguration configuration = cloudConfigurationManager
              .getCloudConfiguration(deploymentContext
                      .getDeployment().getOrchestratorId());
      callback.onSuccess(di.getStatus());
      log.info("Get the orchestrators UUIDs for all deployments managed by the currently logged in user");
      OrchestratorResponse response = orchestratorConnector.callGetDeployments(configuration.getOrchestratorEndpoint());
      final GetDeploymentsResponse gdr = response.<GetDeploymentsResponse>getResponse(GetDeploymentsResponse.class);
      final List<DeploymentOrchestrator> deployments = gdr.getContent();
      final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      for (DeploymentOrchestrator deployment: deployments) {
        response = orchestratorConnector.callGetTemplate(configuration.getOrchestratorEndpoint(),
                deployment.getUuid());
        log.info(response.toString());
        JsonNode root = mapper.readTree(response.getResponse().toString());
//        JsonNode metadata = root.get("metadata");
//        if (metadata != null) {
          JsonNode description = root.get("description");
          if (description != null) {
            String a4cInfo = description.asText();
            int pos = a4cInfo.lastIndexOf('{');
            if (pos > -1) {
              a4cInfo = a4cInfo.substring(pos);
              A4cOrchestratorInfo info = mapper.readValue(a4cInfo, A4cOrchestratorInfo.class);
              DeploymentInfo tmpDi = mappingService.getByA4CDeploymentPaasId(info.getDeploymentPaasId());

              if (tmpDi != null) {
                tmpDi.setOrchestratorDeploymentIdIfNull(deployment.getUuid());
              } else
                log.warn("Update deployment statuses encountered a deployment with template name " + description.asText()
                        + "that is not registered in the mapping service");
            } else
              log.warn("Unable to find additional information needed to match the orchestrator deployment with what we have on A4C");
          } else
            log.warn("Deployment with UUID " + deployment.getUuid() + " doesn't have A4C info in the description");
//        } else {
//          log.warn("Deployment with UUID " + deployment.getUuid() + " doesn't have a metadata field");
//        }

      }
    } catch (NoSuchFieldException | IOException e) {
      e.printStackTrace();
    } catch (OrchestratorIamException e) {
      e.printStackTrace();
    } finally {
      updateStatusLock.unlock();
    }

    // If another thread calls this method and we are in the process of updating the info
    // return the status of the
    if (updateStatusLock.isLocked())
      callback.onSuccess(di.getStatus());

  }

  protected Map<String, String> extractTopologyOutputs(DeploymentTopology topo) {
    final Map<String, String> results = new HashMap<>();
    if (topo.getOutputAttributes() != null) {
      topo.getOutputAttributes().entrySet().stream()
          .forEach(outputAttributeEntry -> outputAttributeEntry.getValue().forEach(outputAttribute -> results
              .put(outputAttributeEntry.getKey() + "_" + outputAttribute, DEFAULT_NOT_SET_OUTPUT_VALUE)));
    }
    if (topo.getOutputCapabilityProperties() != null) {
      topo.getOutputCapabilityProperties().entrySet().stream()
          .forEach(outputCapaPropEntry -> outputCapaPropEntry.getValue().entrySet().stream()
              .forEach(outputCapaPropSubEntry -> outputCapaPropSubEntry.getValue()
                  .forEach(outputCapaProp -> results.put(
                      outputCapaPropEntry.getKey() + "_" + outputCapaPropEntry.getKey() + "_" + outputCapaProp,
                      DEFAULT_NOT_SET_OUTPUT_VALUE))));
    }
    if (topo.getOutputProperties() != null) {
      topo.getOutputProperties().entrySet().stream()
          .forEach(outputPropertyEntry -> outputPropertyEntry.getValue().forEach(outputProperty -> results
              .put(outputPropertyEntry.getKey() + "_" + outputProperty, DEFAULT_NOT_SET_OUTPUT_VALUE)));
    }
    if (topo.getOutputs() != null) {
      topo.getOutputs().entrySet().stream()
          .forEach(outputEntry -> results.put(outputEntry.getKey(), DEFAULT_NOT_SET_OUTPUT_VALUE));
    }
    return results;
  }

  @Override
  public void update(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    return;
  }

  @Override
  public List<PluginArchive> pluginArchives() {
    return Collections.emptyList();
  }

  /** ****** Not implemented. */
  @Override
  public void scale(PaaSDeploymentContext deploymentContext, String nodeTemplateId, int instances,
      IPaaSCallback<?> callback) {
  }

  @Override
  public void launchWorkflow(PaaSDeploymentContext deploymentContext, String workflowName, Map<String, Object> inputs,
      IPaaSCallback<?> callback) {
    throw new NotImplementedException();
  }

  @Override
  public void executeOperation(PaaSTopologyDeploymentContext deploymentContext, NodeOperationExecRequest request,
      IPaaSCallback<Map<String, String>> operationResultCallback) throws OperationExecutionException {
  }

  @Override
  public void switchInstanceMaintenanceMode(PaaSDeploymentContext deploymentContext, String nodeId, String instanceId,
      boolean maintenanceModeOn) throws MaintenanceModeException {
  }

  @Override
  public void switchMaintenanceMode(PaaSDeploymentContext deploymentContext, boolean maintenanceModeOn)
      throws MaintenanceModeException {
  }
}
