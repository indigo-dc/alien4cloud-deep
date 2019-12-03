package es.upv.indigodc.service;


import alien4cloud.model.deployment.DeploymentTopology;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import alien4cloud.paas.model.DeploymentStatus;
import es.upv.indigodc.service.model.DeploymentInfo;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class MappingServiceTest {
	public static final String ORCHESTRATOR_ID = "orcId";
	public static final String ALIEN_DEPLOYMENT_ID = "alienDeploymentId";
	public static final String ALIEN_DEPLOYMENT_PAAS_ID = "alienDeploymentPaasId";
	public static final String ALIEN_DEPLOYMENT_TOPOLOGY_ID = "alienDeploymentTopologyId";
	public static final String ALIEN_DEPLOYMENT_ID_NULL_ORCHESTRATOR_DEPLOYMENT_ID = "alienDeploymentIdNull";
	public static final String ALIEN_DEPLOYMENT_ID_INVALID = "none";
	public static final String ORCHESTRATOR_DEPLOYMENT_ID = "orchestratorDeploymentId";
	public static final String ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS = "CREATE_IN_PROGRESS";
	public static final String ORCHESTRATOR_STATUS_DEPLOYMENT_NOT_HANDLED = "NOT_HANDLED";


	@Test
	/**
	 * Add deployment and then retrieve it
	 */
	public void addDeploymentRetrieveIt() {
		MappingService ms = new MappingService();
		ms.init(new HashMap<String, PaaSTopologyDeploymentContext>());
		ms.registerDeployment(new DeploymentInfo("alienDeploymentPaasId", "orchestratorUuidDeployment",
		    "alienDeploymentId",
		    "orchestratorId", DeploymentStatus.DEPLOYED, null, null));
		DeploymentInfo odm = ms.getByA4CDeploymentPaasId("alienDeploymentPaasId");
		Assertions.assertEquals(odm.getOrchestratorDeploymentId(), "orchestratorUuidDeployment");
		DeploymentInfo adm = ms.getByOrchestratorDeploymentId("orchestratorUuidDeployment");
		Assertions.assertEquals(adm.getA4cDeploymentPaasId(), "alienDeploymentPaasId");
		Assertions.assertEquals(adm.getOrchestratorId(), "orchestratorId");
	}

	public void initWithUnknownStatusDeployment() {
		Map<String, PaaSTopologyDeploymentContext> activeDeployments = new HashMap<>();
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		Mockito.when(deploymentContext.getDeploymentPaaSId()).thenReturn("alienDeploymentPaasId");
		Mockito.when(deploymentContext.getDeploymentId()).thenReturn("alienDeploymentPaasId");
		DeploymentTopology dt = Mockito.mock(DeploymentTopology.class);
		Mockito.when(dt.getOrchestratorId()).thenReturn("orchestratorId");
		Mockito.when(deploymentContext.getDeploymentTopology()).thenReturn(dt);
		MappingService ms = new MappingService();

		activeDeployments.put("alienDeploymentPaasId", deploymentContext);
		DeploymentInfo odm = ms.getByA4CDeploymentPaasId("alienDeploymentPaasId");
		Assertions.assertNotEquals(odm, null);
		Assertions.assertEquals(odm.getOrchestratorDeploymentId(), "orchestratorUuidDeployment");
	}

	public void initAndReturnNullForInexistent() {
		Map<String, PaaSTopologyDeploymentContext> activeDeployments = new HashMap<>();
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		Mockito.when(deploymentContext.getDeploymentPaaSId()).thenReturn("alienDeploymentPaasId");
		Mockito.when(deploymentContext.getDeploymentId()).thenReturn("alienDeploymentPaasId");
		DeploymentTopology dt = Mockito.mock(DeploymentTopology.class);
		Mockito.when(dt.getOrchestratorId()).thenReturn("orchestratorId");
		Mockito.when(deploymentContext.getDeploymentTopology()).thenReturn(dt);
		MappingService ms = new MappingService();

		activeDeployments.put("alienDeploymentPaasId", deploymentContext);
		DeploymentInfo odm = ms.getByA4CDeploymentPaasId("notAnId");
		Assertions.assertEquals(odm, null);
	}
}