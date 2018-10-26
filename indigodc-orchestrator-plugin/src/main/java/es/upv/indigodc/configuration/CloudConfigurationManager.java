package es.upv.indigodc.configuration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;


/**
 * Contains the configuration for an instance of the orchestrator plugin.
 * 
 * @author asalic
 *
 */

@Component("cloud-configuration-manager")
public class CloudConfigurationManager {

  private Map<String, CloudConfiguration> configurations;

  // /**
  // * The ID of the orchestrator instance.
  // */
  // private String orchestratorId;
  // /**
  // * The configuration properties for an orchestrator instance.
  // */
  // private CloudConfiguration configuration;

  public CloudConfigurationManager() {
    configurations = new HashMap<>();
  }

  public void addCloudConfiguration(String orchestratorId, CloudConfiguration configuration) {
    configurations.put(orchestratorId, configuration);
  }

  public CloudConfiguration getCloudConfiguration(String orchestratorId) {
    return configurations.get(orchestratorId);
  }

}
