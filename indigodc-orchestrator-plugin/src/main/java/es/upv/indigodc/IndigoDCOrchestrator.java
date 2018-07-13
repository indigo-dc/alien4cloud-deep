package es.upv.indigodc;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
import es.upv.indigodc.service.model.OrchestratorDeploymentMapping;
import es.upv.indigodc.service.model.OrchestratorIAMException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("indigodc-orchestrator")
@Scope("prototype")
public class IndigoDCOrchestrator implements IOrchestratorPlugin<CloudConfiguration> {

  public static String TYPE = "IndigoDC";


  @Autowired
  @Qualifier("cloud-configuration-manager")
  private CloudConfigurationManager cloudConfigurationHolder;

  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;

  @Autowired
  @Qualifier("builder-service")
  private BuilderService builderService;

  @Autowired
  @Qualifier("mapping-service")
  private MappingService mappingService;

  @Inject private LocationConfiguratorFactory locationConfiguratorFactory;

  @Inject private EventService eventService;

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
    cloudConfigurationHolder.setConfiguration(configuration);
    cloudConfigurationHolder.setOrchestratorId(orchestratorId);
  }

  @Override
  public void deploy(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    CloudConfiguration configuration =
        cloudConfigurationHolder
            .getConfiguration(); // deploymentContext.getDeployment().getOrchestratorId();

    try {
      final String yamlPaasTopology = builderService.buildApp(deploymentContext, 
          cloudConfigurationHolder.getConfiguration().getImportIndigoCustomTypes());
      log.info("Topology: " + yamlPaasTopology);
      OrchestratorResponse response =
          orchestratorConnector.callDeploy(configuration, yamlPaasTopology);
      final String orchestratorUUIDDeployment =
          OrchestratorConnector.getOrchestratorUUIDDeployment(response);
      log.info("uuid a4c: " + deploymentContext.getDeploymentPaaSId());
      log.info("uuid orchestrator: " + orchestratorUUIDDeployment);
      mappingService.registerDeploymentInfo(
          orchestratorUUIDDeployment,
          deploymentContext.getDeploymentPaaSId(),
          deploymentContext.getDeployment().getOrchestratorId(),
          DeploymentStatus.DEPLOYMENT_IN_PROGRESS);
      callback.onSuccess(null);
    } catch (NoSuchFieldException e) {
      callback.onFailure(e);
      log.error("Error deployment", e);
      mappingService.registerDeploymentInfoAlienToIndigoDC(
          deploymentContext.getDeploymentPaaSId(), DeploymentStatus.FAILURE);
    } catch (IOException e) {
      callback.onFailure(e);
      log.error("Error deployment ", e);
      mappingService.registerDeploymentInfoAlienToIndigoDC(
          deploymentContext.getDeploymentPaaSId(), DeploymentStatus.FAILURE);
    } catch (OrchestratorIAMException e) {
      callback.onFailure(e);
      log.error("Error deployment ", e);
      mappingService.registerDeploymentInfoAlienToIndigoDC(
          deploymentContext.getDeploymentPaaSId(), DeploymentStatus.FAILURE);
    }
  }

  @Override
  public ILocationConfiguratorPlugin getConfigurator(String locationType) {
    return locationConfiguratorFactory.newInstance(locationType);
  }

  @Override
  public void undeploy(PaaSDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    final CloudConfiguration configuration =
        cloudConfigurationHolder
            .getConfiguration(); // deploymentContext.getDeployment().getOrchestratorId();

    try {
      if (mappingService.getByAlienDeploymentId(deploymentContext.getDeploymentPaaSId()) != null) {
        final String orchestratorUUIDDeployment =
            mappingService
                .getByAlienDeploymentId(deploymentContext.getDeploymentPaaSId())
                .getOrchestratorUUIDDeployment();
        log.info("Deployment paas id: " + deploymentContext.getDeploymentPaaSId());
        log.info("uuid: " + orchestratorUUIDDeployment);
        final OrchestratorResponse result =
            orchestratorConnector.callUndeploy(configuration, orchestratorUUIDDeployment);
        mappingService.registerDeploymentInfo(
            orchestratorUUIDDeployment,
            deploymentContext.getDeploymentPaaSId(),
            deploymentContext.getDeployment().getOrchestratorId(),
            DeploymentStatus.UNDEPLOYMENT_IN_PROGRESS);
      }
      callback.onSuccess(null);
    } catch (IOException e) {
      log.error("Error undeployment", e);
      callback.onFailure(e);
      mappingService.registerDeploymentInfoAlienToIndigoDC(
          deploymentContext.getDeploymentPaaSId(), DeploymentStatus.FAILURE);
    } catch (NoSuchFieldException e) {
      log.error("Error undeployment", e);
      callback.onFailure(e);
      mappingService.registerDeploymentInfoAlienToIndigoDC(
          deploymentContext.getDeploymentPaaSId(), DeploymentStatus.FAILURE);
    } catch (OrchestratorIAMException e) {
      callback.onFailure(e);
      log.error("Error deployment ", e);
      mappingService.registerDeploymentInfoAlienToIndigoDC(
          deploymentContext.getDeploymentPaaSId(), DeploymentStatus.FAILURE);
    }
  }

  @Override
  public void getEventsSince(
      Date date, int maxEvents, IPaaSCallback<AbstractMonitorEvent[]> eventCallback) {
    eventCallback.onSuccess(eventService.flushEvents(date, maxEvents));
    // log.info("call getEventsSince");

  }

  @Override
  public void getInstancesInformation(
      PaaSTopologyDeploymentContext deploymentContext,
      IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback) {
    log.info("call getInstancesInformation");
    final Map<String, Map<String, InstanceInformation>> topologyInfo = new HashMap<>();
    final Map<String, String> runtimeProps = new HashMap<>();
    final Map<String, InstanceInformation> instancesInfo = new HashMap<>();
    final String groupID = deploymentContext.getDeploymentPaaSId();
    final OrchestratorDeploymentMapping indigoDCDeploymentMapping =
        mappingService.getByAlienDeploymentId(deploymentContext.getDeployment().getId());
    final String a4cUUIDDeployment = deploymentContext.getDeployment().getId();

    if (indigoDCDeploymentMapping != null) {
      final String orchestratorUUIDDeployment =
          indigoDCDeploymentMapping
              .getOrchestratorUUIDDeployment(); // .getDeploymentId();//.getDeploymentPaaSId();

      final CloudConfiguration configuration = cloudConfigurationHolder.getConfiguration();
      try {
        OrchestratorResponse response =
            orchestratorConnector.callDeploymentStatus(configuration, orchestratorUUIDDeployment);
        Util.InstanceStatusInfo instanceStatusInfo =
            Util.indigoDCStatusToInstanceStatus(
                OrchestratorConnector.getStatusTopologyDeployment(response).toUpperCase());

        final InstanceInformation instanceInformation =
            new InstanceInformation(
                instanceStatusInfo.getState(),
                instanceStatusInfo.getInstanceStatus(),
                runtimeProps,
                runtimeProps,
                new HashMap<>());
        instancesInfo.put(a4cUUIDDeployment, instanceInformation);
        topologyInfo.put(groupID, instancesInfo);
        callback.onSuccess(topologyInfo);
      } catch (NullPointerException e) {
        log.error("Null error", e);
        callback.onSuccess(topologyInfo);
      } catch (NoSuchFieldException e) {
        callback.onFailure(e);
        log.error("Error getInstancesInformation", e);
      } catch (IOException e) {
        callback.onFailure(e);
        log.error("Error getInstancesInformation", e);
      } catch (OrchestratorIAMException e) {
        final InstanceInformation instanceInformation =
            new InstanceInformation(
                "UNKNOWN", InstanceStatus.FAILURE, runtimeProps, runtimeProps, new HashMap<>());
        instancesInfo.put(a4cUUIDDeployment, instanceInformation);
        topologyInfo.put(a4cUUIDDeployment, instancesInfo);
        callback.onSuccess(topologyInfo);
        instancesInfo.put(a4cUUIDDeployment, instanceInformation);
        topologyInfo.put(a4cUUIDDeployment, instancesInfo);
        switch (e.getHttpCode()) {
          case 404:
            callback.onSuccess(topologyInfo);
            break;
          default:
            callback.onFailure(e);
        }
        log.error("Error deployment ", e);
      }
    } else {
      final InstanceInformation instanceInformation =
          new InstanceInformation(
              "UNKNOWN", InstanceStatus.FAILURE, runtimeProps, runtimeProps, new HashMap<>());
      instancesInfo.put(a4cUUIDDeployment, instanceInformation);
      topologyInfo.put(a4cUUIDDeployment, instancesInfo);
      callback.onSuccess(topologyInfo);
    }
  }

  @Override
  public void getStatus(
      PaaSDeploymentContext deploymentContext, IPaaSCallback<DeploymentStatus> callback) {
    log.info("call get status");
    final OrchestratorDeploymentMapping indigoDCDeploymentMapping =
        mappingService.getByAlienDeploymentId(deploymentContext.getDeploymentPaaSId());
    if (indigoDCDeploymentMapping != null) {
      final String orchestratorUUIDDeployment =
          indigoDCDeploymentMapping.getOrchestratorUUIDDeployment();
      if (orchestratorUUIDDeployment != null) {
        final CloudConfiguration configuration = cloudConfigurationHolder.getConfiguration();
        try {
          OrchestratorResponse response =
              orchestratorConnector.callDeploymentStatus(configuration, orchestratorUUIDDeployment);

          callback.onSuccess(
              Util.indigoDCStatusToDeploymentStatus(
                  OrchestratorConnector.getStatusTopologyDeployment(response).toUpperCase()));

        } catch (RuntimeException e) {
          log.error("Error getStatus", e);
          callback.onFailure(e);
          callback.onSuccess(DeploymentStatus.UNKNOWN);
        } catch (NoSuchFieldException e) {
          log.error("Error getStatus", e);
          callback.onFailure(e);
          callback.onSuccess(DeploymentStatus.UNKNOWN);
        } catch (IOException e) {
          log.error("Error getStatus", e);
          callback.onFailure(e);
          callback.onSuccess(DeploymentStatus.UNKNOWN);
        } catch (OrchestratorIAMException e) {
          switch (e.getHttpCode()) {
            case 404:
              callback.onSuccess(DeploymentStatus.UNDEPLOYED);
              break;
            default:
              callback.onFailure(e);
          }
          log.error("Error deployment ", e);
        }
      } else callback.onSuccess(DeploymentStatus.UNDEPLOYED);
    } else callback.onSuccess(DeploymentStatus.UNDEPLOYED);
  }

  @Override
  public void update(PaaSTopologyDeploymentContext deploymentContext, IPaaSCallback<?> callback) {
    return;
  }

  @Override
  public List<PluginArchive> pluginArchives() {
    return Collections.emptyList();
  }

  /** ****** Not implemented */
  @Override
  public void scale(
      PaaSDeploymentContext deploymentContext,
      String nodeTemplateId,
      int instances,
      IPaaSCallback<?> callback) {
    throw new NotImplementedException();
  }

  @Override
  public void launchWorkflow(
      PaaSDeploymentContext deploymentContext,
      String workflowName,
      Map<String, Object> inputs,
      IPaaSCallback<?> callback) {
    throw new NotImplementedException();
  }

  @Override
  public void executeOperation(
      PaaSTopologyDeploymentContext deploymentContext,
      NodeOperationExecRequest request,
      IPaaSCallback<Map<String, String>> operationResultCallback)
      throws OperationExecutionException {
    throw new NotImplementedException();
  }

  @Override
  public void switchInstanceMaintenanceMode(
      PaaSDeploymentContext deploymentContext,
      String nodeId,
      String instanceId,
      boolean maintenanceModeOn)
      throws MaintenanceModeException {
    throw new NotImplementedException();
  }

  @Override
  public void switchMaintenanceMode(
      PaaSDeploymentContext deploymentContext, boolean maintenanceModeOn)
      throws MaintenanceModeException {
    throw new NotImplementedException();
  }
}
