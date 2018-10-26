package es.upv.indigodc;

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
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.location.LocationConfiguratorFactory;
import es.upv.indigodc.service.BuilderService;
import es.upv.indigodc.service.EventService;
import es.upv.indigodc.service.MappingService;
import es.upv.indigodc.service.OrchestratorConnector;
import es.upv.indigodc.service.UserService;
import es.upv.indigodc.service.model.OrchestratorDeploymentMapping;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import es.upv.indigodc.service.model.StatusNotFoundException;

import java.io.IOException;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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

  public static String TYPE = "IndigoDC";

  /**
   * The configuration manager used to obtain the
   * {@link es.upv.indigodc.configuration.CloudConfiguration} instance that holds the parameters of
   * the plugin.
   */
  @Autowired
  @Qualifier("cloud-configuration-manager")
  private CloudConfigurationManager cloudConfigurationManager;

  /** The service that executes the HTTP(S) calls to the Orchestrator. */
  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;

  /**
   * The service that creates the payload (which includes the TOSCA topologies) that is sent to the
   * Orchestrator using {@link #orchestratorConnector}.
   */
  @Autowired
  @Qualifier("builder-service")
  private BuilderService builderService;

  /** Maintain the IDs of the launched topologies. */
  @Autowired
  @Qualifier("mapping-service")
  private MappingService mappingService;

  /** Manages the instantiation of a new location configurator using a location type. */
  @Inject
  private LocationConfiguratorFactory locationConfiguratorFactory;

  /** Manages the events produced by the Orchestrator. */
  @Inject
  private EventService eventService;

  /** Manages the logged in user that executes this instance of service. */
  @Autowired
  private UserService userService;

  @Override
  public void init(Map<String, PaaSTopologyDeploymentContext> activeDeployments) {
    if (activeDeployments != null) {
      mappingService.init(activeDeployments.values());
    }
  }

  /**
   * Method called when this instance is scrapped.
   */
  public void destroy() {

  }

  @Override
  public void setConfiguration(String orchestratorId, CloudConfiguration configuration)
      throws PluginConfigurationException {
    if (configuration == null) {
      throw new PluginConfigurationException("Configuration must not be null");
    }
    cloudConfigurationManager.addCloudConfiguration(orchestratorId, configuration);
  }

  @Override
  public void deploy(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    CloudConfiguration configuration = cloudConfigurationManager
        .getCloudConfiguration(deploymentContext.getDeployment().getOrchestratorId());
    String a4cUuidDeployment = deploymentContext.getDeployment().getId();

    try {
      final String yamlPaasTopology =
          builderService.buildApp(deploymentContext, configuration.getImportIndigoCustomTypes());
      log.info("Deploying on: " + configuration.getOrchestratorEndpoint());
      log.info("Topology: " + yamlPaasTopology);
      OrchestratorResponse response = orchestratorConnector.callDeploy(configuration,
          userService.getCurrentUser().getUsername(),
          userService.getCurrentUser().getPlainPassword(), yamlPaasTopology);
      final String orchestratorUuidDeployment = response.getOrchestratorUuidDeployment();
      log.info("uuid a4c: " + a4cUuidDeployment);
      log.info("uuid orchestrator: " + orchestratorUuidDeployment);
      mappingService.registerDeploymentInfo(orchestratorUuidDeployment, a4cUuidDeployment,
          deploymentContext.getDeployment().getOrchestratorId(),
          DeploymentStatus.DEPLOYMENT_IN_PROGRESS);
      // eventService.subscribe(configuration);
      callback.onSuccess(null);
    } catch (NoSuchFieldException er) {
      callback.onFailure(er);
      log.error("Error deployment", er);
      mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment,
          DeploymentStatus.FAILURE);
    } catch (IOException er) {
      callback.onFailure(er);
      log.error("Error deployment ", er);
      mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment,
          DeploymentStatus.FAILURE);
    } catch (OrchestratorIamException er) {
      callback.onFailure(er);
      log.error("Error deployment ", er);
      mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment,
          DeploymentStatus.FAILURE);
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
    String a4cUuidDeployment = deploymentContext.getDeployment().getId();
    try {
      if (mappingService.getByAlienDeploymentId(a4cUuidDeployment) != null) {
        final String orchestratorUuidDeployment = mappingService
            .getByAlienDeploymentId(a4cUuidDeployment).getOrchestratorUuidDeployment();
        log.info("Deployment paas id: " + a4cUuidDeployment);
        log.info("uuid: " + orchestratorUuidDeployment);
        final OrchestratorResponse result = orchestratorConnector.callUndeploy(configuration,
            userService.getCurrentUser().getUsername(),
            userService.getCurrentUser().getPlainPassword(), orchestratorUuidDeployment);
        mappingService.registerDeploymentInfo(orchestratorUuidDeployment, a4cUuidDeployment,
            deploymentContext.getDeployment().getOrchestratorId(),
            DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS);

        // eventService.unsubscribe();
      }
      callback.onSuccess(null);
    } catch (IOException er) {
      log.error("Error undeployment", er);
      callback.onFailure(er);
      mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment,
          DeploymentStatus.FAILURE);
    } catch (NoSuchFieldException er) {
      log.error("Error undeployment", er);
      callback.onFailure(er);
      mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment,
          DeploymentStatus.FAILURE);
    } catch (OrchestratorIamException er) {
      callback.onFailure(er);
      log.error("Error deployment ", er);
      mappingService.registerDeploymentInfoAlienToIndigoDc(a4cUuidDeployment,
          DeploymentStatus.FAILURE);
    }
  }

  @Override
  public void getEventsSince(Date date, int maxEvents,
      IPaaSCallback<AbstractMonitorEvent[]> eventCallback) {
    eventCallback.onSuccess(eventService.flushEvents(date, maxEvents));
    // log.info("call getEventsSince");

  }

  @Override
  public void getInstancesInformation(PaaSTopologyDeploymentContext deploymentContext,
      IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback) {
    log.info("call getInstancesInformation");
    String a4cUuidDeployment = deploymentContext.getDeployment().getId();

    // deploymentContext.getDeploymentTopology().get
    final Map<String, Map<String, InstanceInformation>> topologyInfo = new HashMap<>();
    final Map<String, String> runtimeProps = new HashMap<>();
    final Map<String, InstanceInformation> instancesInfo = new HashMap<>();
    final String groupId = deploymentContext.getDeploymentPaaSId();
    // final String
    final OrchestratorDeploymentMapping orchestratorDeploymentMapping =
        mappingService.getByAlienDeploymentId(a4cUuidDeployment);

    if (orchestratorDeploymentMapping != null) {
      final String orchestratorUuidDeployment =
          orchestratorDeploymentMapping.getOrchestratorUuidDeployment(); 
      // .getDeploymentId();//.getDeploymentPaaSId();

      final CloudConfiguration configuration = cloudConfigurationManager
          .getCloudConfiguration(deploymentContext.getDeployment().getOrchestratorId());
      try {
        OrchestratorResponse response = orchestratorConnector.callDeploymentStatus(configuration,
            userService.getCurrentUser().getUsername(),
            userService.getCurrentUser().getPlainPassword(), orchestratorUuidDeployment);

        log.info(response.getResponse().toString());
        Util.InstanceStatusInfo instanceStatusInfo = Util
            .indigoDcStatusToInstanceStatus(response.getStatusTopologyDeployment().toUpperCase());

        // Map<String, String> outputs = new HashMap<>();
        // outputs.put("Compute_public_address", "none");
        // runtimeProps.put("Compute_public_address", "value");
        final InstanceInformation instanceInformation =
            new InstanceInformation(instanceStatusInfo.getState(),
                instanceStatusInfo.getInstanceStatus(), runtimeProps, runtimeProps,
                // outputs);
                response.getOutputs());
        instancesInfo.put(a4cUuidDeployment, instanceInformation);
        topologyInfo.put(groupId, instancesInfo);
        callback.onSuccess(topologyInfo);
      } catch (NoSuchFieldException er) {
        callback.onFailure(er);
        log.error("Error getInstancesInformation", er);
      } catch (IOException er) {
        callback.onFailure(er);
        log.error("Error getInstancesInformation", er);
      } catch (OrchestratorIamException er) {
        final InstanceInformation instanceInformation = new InstanceInformation("UNKNOWN",
            InstanceStatus.FAILURE, runtimeProps, runtimeProps, new HashMap<>());
        instancesInfo.put(a4cUuidDeployment, instanceInformation);
        topologyInfo.put(a4cUuidDeployment, instancesInfo);
        callback.onSuccess(topologyInfo);
        instancesInfo.put(a4cUuidDeployment, instanceInformation);
        topologyInfo.put(a4cUuidDeployment, instancesInfo);
        switch (er.getHttpCode()) {
          case 404:
            callback.onSuccess(topologyInfo);
            break;
          default:
            callback.onFailure(er);
        }
        log.error("Error deployment ", er);
      } catch (StatusNotFoundException er) {
        callback.onFailure(er);
        log.error("Error deployment ", er);
      }
    } else {
      final InstanceInformation instanceInformation = new InstanceInformation("UNKNOWN",
          InstanceStatus.FAILURE, runtimeProps, runtimeProps, new HashMap<>());
      instancesInfo.put(a4cUuidDeployment, instanceInformation);
      topologyInfo.put(a4cUuidDeployment, instancesInfo);
      callback.onSuccess(topologyInfo);
    }
  }

  @Override
  public void getStatus(PaaSDeploymentContext deploymentContext,
      IPaaSCallback<DeploymentStatus> callback) {
    log.info("call get status");
    String a4cUuidDeployment = deploymentContext.getDeployment().getId();

    final OrchestratorDeploymentMapping orchestratorDeploymentMapping =
        mappingService.getByAlienDeploymentId(a4cUuidDeployment);
    if (orchestratorDeploymentMapping != null) {
      final String orchestratorUuidDeployment =
          orchestratorDeploymentMapping.getOrchestratorUuidDeployment();
      if (orchestratorUuidDeployment != null) {
        final CloudConfiguration configuration = cloudConfigurationManager
            .getCloudConfiguration(deploymentContext.getDeployment().getOrchestratorId());
        try {
          OrchestratorResponse response = orchestratorConnector.callDeploymentStatus(configuration,
              userService.getCurrentUser().getUsername(),
              userService.getCurrentUser().getPlainPassword(), orchestratorUuidDeployment);
          String statusTopologyDeployment = response.getStatusTopologyDeployment();
          callback.onSuccess(
              Util.indigoDcStatusToDeploymentStatus(statusTopologyDeployment.toUpperCase()));

        } catch (NoSuchFieldException er) {
          log.error("Error getStatus", er);
          callback.onFailure(er);
          callback.onSuccess(DeploymentStatus.UNKNOWN);
        } catch (IOException er) {
          log.error("Error getStatus", er);
          callback.onFailure(er);
          callback.onSuccess(DeploymentStatus.UNKNOWN);
        } catch (OrchestratorIamException er) {
          switch (er.getHttpCode()) {
            case 404:
              callback.onSuccess(DeploymentStatus.UNDEPLOYED);
              break;
            default:
              callback.onFailure(er);
          }
          log.error("Error deployment ", er);
        } catch (StatusNotFoundException er) {
          callback.onFailure(er);
          er.printStackTrace();
        }
      } else {
        callback.onSuccess(DeploymentStatus.UNDEPLOYED);
      }
    } else {
      callback.onSuccess(DeploymentStatus.UNDEPLOYED);
    }
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
      IPaaSCallback<?> callback) {}

  @Override
  public void launchWorkflow(PaaSDeploymentContext deploymentContext, String workflowName,
      Map<String, Object> inputs, IPaaSCallback<?> callback) {
    throw new NotImplementedException();
  }

  @Override
  public void executeOperation(PaaSTopologyDeploymentContext deploymentContext,
      NodeOperationExecRequest request, IPaaSCallback<Map<String, String>> operationResultCallback)
      throws OperationExecutionException {}

  @Override
  public void switchInstanceMaintenanceMode(PaaSDeploymentContext deploymentContext, String nodeId,
      String instanceId, boolean maintenanceModeOn) throws MaintenanceModeException {}

  @Override
  public void switchMaintenanceMode(PaaSDeploymentContext deploymentContext,
      boolean maintenanceModeOn) throws MaintenanceModeException {}
}
