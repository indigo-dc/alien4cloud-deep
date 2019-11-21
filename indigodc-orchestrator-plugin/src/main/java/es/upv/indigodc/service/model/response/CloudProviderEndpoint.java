package es.upv.indigodc.service.model.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information about the cloud provider for a deployment
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties
public class CloudProviderEndpoint {

    /**
     * API endpoint
     */
    private String cpEndpoint;

    /**
     * Id of the compute service
     */
    private String cpComputeServiceId;

    /**
     * Deployment type
     */
    private String deploymentType;

    /**
     * The URI of a vault for the deployment
     */
    private String vaultUri;

}
