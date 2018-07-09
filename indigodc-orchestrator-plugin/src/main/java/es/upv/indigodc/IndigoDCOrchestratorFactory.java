package es.upv.indigodc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.alien4cloud.tosca.model.definitions.PropertyDefinition;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import alien4cloud.model.orchestrators.ArtifactSupport;
import alien4cloud.model.orchestrators.locations.LocationSupport;
import alien4cloud.orchestrators.plugin.IOrchestratorPluginFactory;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.location.LocationConfigurator;
import es.upv.indigodc.service.ArtifactRegistryService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("indigodc-orchestrator-factory")
public class IndigoDCOrchestratorFactory
    implements IOrchestratorPluginFactory<IndigoDCOrchestrator, CloudConfiguration> {

  public static final String CLOUD_CONFIGURATION_DEFAULTS_FILE =
      "/provider/cloud_conf_default.json";

  @Autowired private BeanFactory beanFactory;

  @Autowired private ArtifactRegistryService artifactRegistryService;

  @Override
  public void destroy(IndigoDCOrchestrator arg0) {
    // TODO Auto-generated method stub
  }

  @Override
  public ArtifactSupport getArtifactSupport() {
    return new ArtifactSupport(artifactRegistryService.getSupportedArtifactTypes());
  }

  @Override
  public Class<CloudConfiguration> getConfigurationType() {
    return CloudConfiguration.class;
  }

  @Override
  public CloudConfiguration getDefaultConfiguration() {
    ObjectMapper mapper = new ObjectMapper();
    InputStream is =
        IndigoDCOrchestratorFactory.class.getResourceAsStream(CLOUD_CONFIGURATION_DEFAULTS_FILE);
    CloudConfiguration c;
    try {
      c = mapper.readValue(is, CloudConfiguration.class);
    } catch (IOException e) {
      e.printStackTrace();
      log.error(e.toString());
      c =
          new CloudConfiguration(
              "clientId",
              "csecret",
              "tendpoint",
              "tendpointcert",
              "cscopes",
              "oendpoint",
              "oendpointCert",
              "iamhost",
              "iamhostCert",
              "u",
              "p",
              5,
              "https://raw.githubusercontent.com/indigo-dc/tosca-types/devel-deep/custom_types.yaml");
    }
    return c;
  }

  @Override
  public Map<String, PropertyDefinition> getDeploymentPropertyDefinitions() {
    return Maps.newHashMap();
  }

  @Override
  public LocationSupport getLocationSupport() {
    return new LocationSupport(false, new String[] {LocationConfigurator.LOCATION_TYPE});
  }

  @Override
  public String getType() {
    return IndigoDCOrchestrator.TYPE;
  }

  @Override
  public IndigoDCOrchestrator newInstance() {
    // log.warn("***************************************************************IndigoDCOrchestrator.newInstance");
    return beanFactory.getBean(IndigoDCOrchestrator.class);
  }
}
