package es.upv.indigodc.service.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import es.upv.indigodc.IndigoDcDeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * The information about a deployment returned when getting the list of deployments.
 * For more information check the Indigo Orchestrator help
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class DeploymentOrchestrator {

    /**
     * Unique identifier of the deployment
     */
    protected String uuid;

//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//    @JsonSerialize(using = LocalDateTimeSerializer.class)
//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
//    protected LocalDateTime creationTime;
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//    @JsonSerialize(using = LocalDateTimeSerializer.class)
//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
//    protected LocalDateTime updateTime;
    /**
     * When the deployment was created
     */
    protected String creationTime;
    /**
     * When the deployment was updated
     */
    protected String updateTime;
    /**
     * The deployment ID
     */
    protected String physicalId;
    /**
     * The status of the deployment
     */
    protected IndigoDcDeploymentStatus status;
    /**
     * The reason for the status (e.g. why it failed when status is FAILED)
     */
    protected String statusReason;
    /**
     * The map bewteen the outputs names and their values obtained during deployment
     */
    protected Map<String, Object> outputs;
    /**
     * The task
     */
    protected String task;
    /**
     * A callback used by the orchestrator
     */
    protected String callback;
    /**
     * The name of the cloud provider where the deployment has been made / has been attempted
     */
    protected String cloudProviderName;
    /**
     * The name of the cloud provider where the deployment has been made / has been attempted
     */
    protected CloudProviderEndpoint cloudProviderEndpoint;
    /**
     * Information about the creator of the deployment
     */
    protected DeploymentCreator createdBy;
    /**
     * Links
     */
    protected List<Link> links;

}
