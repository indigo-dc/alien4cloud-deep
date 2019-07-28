package es.upv.indigodc.service.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import es.upv.indigodc.IndigoDcDeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class DeploymentOrchestrator {

    protected String uuid;
    protected LocalDateTime creationTime;
    protected LocalDateTime updateTime;
    protected IndigoDcDeploymentStatus status;
    protected String statusReason;
    protected Map<String, Object> outputs;
    protected String task;
    protected String callback;
    protected DeploymentCreator createdBy;
    protected List<Link> links;

}
