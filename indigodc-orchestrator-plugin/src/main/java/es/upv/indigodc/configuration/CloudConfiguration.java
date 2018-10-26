package es.upv.indigodc.configuration;

import alien4cloud.ui.form.annotation.FormProperties;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Container class that has all the fields that allow the user to configure an orchestrator
 * instance. These properties are exposed to the user, some of them with default values
 *
 * @author asalic
 */
@Getter
@Setter
@FormProperties({"clientId", "clientSecret", "tokenEndpoint", "tokenEndpointCert", "clientScopes",
    "orchestratorEndpoint", "orchestratorEndpointCert", "iamHost", "iamHostCert",
    "orchestratorPollInterval", "importIndigoCustomTypes"})
@AllArgsConstructor
@NoArgsConstructor
public class CloudConfiguration {

  @NotNull
  private String clientId;
  @NotNull
  private String clientSecret;
  @NotNull
  private String tokenEndpoint;
  @NotNull
  private String tokenEndpointCert;
  @NotNull
  private String clientScopes;
  @NotNull
  private String orchestratorEndpoint;
  @NotNull
  private String orchestratorEndpointCert;
  @NotNull
  private String iamHost;
  @NotNull
  private String iamHostCert;
  private int orchestratorPollInterval;
  @NotNull
  private String importIndigoCustomTypes;
}
