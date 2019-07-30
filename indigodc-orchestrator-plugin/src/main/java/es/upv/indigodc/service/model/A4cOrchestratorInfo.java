package es.upv.indigodc.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class A4cOrchestratorInfo {

    protected String deploymentPaasId;
    protected String deploymentId;
    protected String[] locationIds;
    protected String orchestratorId;
    protected String versionId;
}
