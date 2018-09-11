package es.upv.indigodc.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import alien4cloud.ui.form.annotation.FormProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Container class that has all the fields that allow
 * the user to configure an orchestrator instance.
 * These properties are exposed to the user, some of them with default values
 * @author asalic
 *
 */
@Getter
@Setter
@ToString
@FormProperties({
  "clientId",
  "clientSecret",
  "tokenEndpoint",
  "clientScopes",
  "orchestratorEndpoint",
  "iamHost",
  "orchestratorPollInterval",
  "importIndigoCustomTypes"
})
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CloudConfiguration {

  @NotNull private String clientId;
  @NotNull private String clientSecret;
  @NotNull private String tokenEndpoint;
  @NotNull private String tokenEndpointCert;
  @NotNull private String clientScopes;
  @NotNull private String orchestratorEndpoint;
  @NotNull private String orchestratorEndpointCert;
  @NotNull private String iamHost;
  @NotNull private String iamHostCert;
  //@NotNull private String user;
  //@NotNull private String password;
  private int orchestratorPollInterval;
  @NotNull private String importIndigoCustomTypes;

}
