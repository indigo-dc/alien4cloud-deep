package es.upv.indigodc.service.model;

import alien4cloud.paas.model.DeploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter @AllArgsConstructor
public class IndigoDCDeploymentMapping {
    public static final IndigoDCDeploymentMapping EMPTY = new IndigoDCDeploymentMapping("unknown_deployment_id", DeploymentStatus.UNKNOWN);

    private String indigoDCDeploymentId;
    private DeploymentStatus status;
}
