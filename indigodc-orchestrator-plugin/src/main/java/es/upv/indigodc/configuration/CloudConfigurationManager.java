package es.upv.indigodc.configuration;

import org.springframework.stereotype.Component;

@Component("cloud-configuration-manager")
public class CloudConfigurationManager {
  
  
  private String orchestratorId;
  private CloudConfiguration configuration;
  
  public void setCloudConfiguration(String orchestratorId, CloudConfiguration configuration) {
    this.orchestratorId = orchestratorId;
    this.configuration = configuration;
  }
  
  public CloudConfiguration getCloudConfiguration() {
    return this.configuration;
  }

}
