package es.upv.indigodc.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;


/**
 * Contains the configuration for an instance of the orchestrator plugin.
 * @author asalic
 *
 */
@Getter 
@Setter
@Component("cloud-configuration-manager")
public class CloudConfigurationManager {

  /**
   * The ID of the orchestrator instance.
   */
  private String orchestratorId;
  /**
   * The configuration properties for an orchestrator instance.
   */
  private CloudConfiguration configuration;
}
