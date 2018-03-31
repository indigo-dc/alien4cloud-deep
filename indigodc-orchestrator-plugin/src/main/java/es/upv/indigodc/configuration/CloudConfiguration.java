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

@Getter
@Setter
@ToString
@FormProperties({ "clientId", "clientSecret", "tokenEndpoint", "clientScopes", "orchestratorEndpoint", 
  "iamHost", "user", "password"})
@EqualsAndHashCode
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
  private String clientScopes;
  @NotNull
  private String orchestratorEndpoint;
  @NotNull
  private String iamHost;
  @NotNull
  private String user;  
  @NotNull
  private String password;
  
//  @JsonIgnore
//  private SSLContext sslContext;
  
}
