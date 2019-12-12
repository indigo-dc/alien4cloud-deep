package es.upv.indigodc.configuration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Contains the configuration for an instance of the orchestrator plugin.
 *
 * @author asalic
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

  /** Constructor of the Manager. */
  public CloudConfigurationManager() {
    configurations = new HashMap<>();
  }

  /**
   * Add configuration for an Orchestrator.
   *
   * @param orchestratorId the ID of the orchestrator with this configuration
   * @param configuration the configuration for this orchestrator
   */
  public void addCloudConfiguration(String orchestratorId, CloudConfiguration configuration) {
    configurations.put(orchestratorId, configuration);
  }

  /**
   * Return an Orchestrator specific configuration.
   *
   * @param orchestratorId the ID of the orchestrator with this configuration
   * @return the configuration of an orchestrator. Null if no configuration exists for an
   *     Orchestrator identified by its ID.
   */
  public CloudConfiguration getCloudConfiguration(String orchestratorId) {
    return configurations.get(orchestratorId);
  }
}
