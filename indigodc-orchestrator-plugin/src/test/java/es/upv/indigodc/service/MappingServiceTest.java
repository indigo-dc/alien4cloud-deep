package es.upv.indigodc.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import alien4cloud.paas.model.DeploymentStatus;
import es.upv.indigodc.service.model.AlienDeploymentMapping;
import es.upv.indigodc.service.model.DeploymentInfo;
import es.upv.indigodc.service.model.OrchestratorDeploymentMapping;

public class MappingServiceTest {
	
	@Test
	/**
	 * Add deployment and then retrieve it
	 */
	public void addDeploymentRetrieveIt() {
		MappingService ms = new MappingService();
		ms.init();
		ms.registerDeployment(new DeploymentInfo("alienDeploymentId", "orchestratorUuidDeployment",  
		    "orchestratorId", DeploymentStatus.DEPLOYED, null, null));
		DeploymentInfo odm = ms.getByA4CDeploymentPaasId("alienDeploymentId");
		Assertions.assertEquals(odm.getOrchestratorDeploymentId(), "orchestratorUuidDeployment");
		DeploymentInfo adm = ms.getByOrchestratorDeploymentId("orchestratorUuidDeployment");
		Assertions.assertEquals(adm.getA4cDeploymentPaasId(), "alienDeploymentId");
		Assertions.assertEquals(adm.getOrchestratorId(), "orchestratorId");
	}
}
