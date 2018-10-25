package es.upv.indigodc.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
		assertEquals(odm.getOrchestratorUuidDeployment(), "orchestratorUuidDeployment");
		AlienDeploymentMapping adm = ms.getByOrchestratorUuidDeployment("orchestratorUuidDeployment");
		assertEquals(adm.getDeploymentId(), "alienDeploymentId");
		assertEquals(adm.getOrchetratorId(), "orchestratorId");
	}
}
