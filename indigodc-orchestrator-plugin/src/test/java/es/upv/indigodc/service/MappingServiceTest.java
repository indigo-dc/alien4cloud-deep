package es.upv.indigodc.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import alien4cloud.paas.model.DeploymentStatus;
import es.upv.indigodc.service.model.AlienDeploymentMapping;
import es.upv.indigodc.service.model.OrchestratorDeploymentMapping;

public class MappingServiceTest {
	
	@Test
	/**
	 * Add deployment and then retrieve it
	 */
	public void addDeploymentRetrieveIt() {
		MappingService ms = new MappingService();
		ms.registerDeploymentInfo("orchestratorUuidDeployment", "alienDeploymentId", "orchestratorId", DeploymentStatus.DEPLOYED);
		OrchestratorDeploymentMapping odm = ms.getByAlienDeploymentId("alienDeploymentId");
		Assertions.assertEquals(odm.getOrchestratorUuidDeployment(), "orchestratorUuidDeployment");
		AlienDeploymentMapping adm = ms.getByOrchestratorUuidDeployment("orchestratorUuidDeployment");
		Assertions.assertEquals(adm.getDeploymentId(), "alienDeploymentId");
		Assertions.assertEquals(adm.getOrchetratorId(), "orchestratorId");
	}
}
