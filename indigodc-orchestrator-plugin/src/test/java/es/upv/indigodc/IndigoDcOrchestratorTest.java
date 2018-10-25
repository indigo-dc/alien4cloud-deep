package es.upv.indigodc;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.alien4cloud.tosca.editor.EditionContextManager;
import org.alien4cloud.tosca.exporter.ArchiveExportService;
import org.alien4cloud.tosca.model.Csar;
import org.alien4cloud.tosca.model.definitions.AbstractPropertyValue;
import org.alien4cloud.tosca.model.definitions.PropertyValue;
import org.alien4cloud.tosca.model.templates.Topology;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import alien4cloud.model.deployment.Deployment;
import alien4cloud.model.deployment.DeploymentTopology;
import alien4cloud.paas.IPaaSCallback;
import alien4cloud.paas.exception.PluginConfigurationException;
import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.InstanceInformation;
import alien4cloud.paas.model.PaaSDeploymentContext;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import alien4cloud.security.model.User;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.service.BuilderService;
import es.upv.indigodc.service.BuilderServiceTest;
import es.upv.indigodc.service.MappingService;
import es.upv.indigodc.service.OrchestratorConnector;
import es.upv.indigodc.service.UserService;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import es.upv.indigodc.service.model.StatusNotFoundException;

public class IndigoDcOrchestratorTest {
	
	public static final String ORCHESTRATOR_ID = "orcId";
	public static final String ALIEN_DEPLOYMENT_ID = "alienDeploymentId";
	public static final String ALIEN_DEPLOYMENT_ID_INVALID = "none";
	public static final String ORCHESTRATOR_DEPLOYMENT_ID = "orchestratorDeploymentId";
	public static final String ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS = "CREATE_IN_PROGRESS";
	public static final String ORCHESTRATOR_STATUS_DEPLOYMENT_NOT_HANDLED = "NOT_HANDLED";
	
	@Test
	public void initWithNoDeployments() throws JsonParseException, JsonMappingException, PluginConfigurationException, IOException, IllegalArgumentException, IllegalAccessException {
		IndigoDcOrchestrator idco = getIndigoDcOrchestratorWithTestConfig();
		idco.init(new HashMap<>());
	}
	
	@Test
	public void setNullConfiguration() throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, IllegalArgumentException, IllegalAccessException {
		IndigoDcOrchestrator idco = getIndigoDcOrchestratorWithTestConfig();
		Executable setConfiguration = () -> {idco.setConfiguration("id", null);};
		Assertions.assertThrows(PluginConfigurationException.class, setConfiguration);
	}
	
	
	@Test
	public void deployMockWithNoError() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		
		idco.deploy(deploymentContext, callback);		
	}
	
	@Test
	public void deployMockWithNoSuchFieldException() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		deployMockWithError(NoSuchFieldException.class);	
	}
	
	@Test
	public void deployMockWithIOException() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {

		deployMockWithError(IOException.class);
	}
	
	@Test
	public void deployMockWithOrchestratorIamException() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		deployMockWithError(OrchestratorIamException.class);
	}
	
	@Disabled
	protected void deployMockWithError(Class errorClass) 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		Mockito.when(orchestratorConnector.callDeploy(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenThrow(errorClass);
		idco.deploy(deploymentContext, callback);	
	}
	
	@Test
	public void undeployMockWithNoErrors() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		
		idco.undeploy(deploymentContext, callback);			
	}
	
	@Test
	public void undeployMockWithIOException() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		undeployMockWithError(IOException.class);		
	}
	
	@Test
	public void undeployMockWithNoSuchFieldException() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		undeployMockWithError(NoSuchFieldException.class);
	}
	
	@Test
	public void undeployMockWithOrchestratorIamException() 
			throws PluginConfigurationException, JsonParseException, JsonMappingException, IOException, NoSuchFieldException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		undeployMockWithError(OrchestratorIamException.class);
	}
	
	@Disabled
	protected void undeployMockWithError(Class errorClass) 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		Mockito.when(orchestratorConnector.callUndeploy(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenThrow(errorClass);
		idco.undeploy(deploymentContext, callback);			
	}
	
	@Test
	public void getMockInstancesInformationNoErrors() 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		OrchestratorResponse response = 
				new OrchestratorResponse(200, HttpMethod.POST, 
						new StringBuilder(String.format("{\"uuid\": \"%s\", \"status\": \"%s\"}", 
								ORCHESTRATOR_DEPLOYMENT_ID, 
								ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS)));
		Mockito.when(orchestratorConnector.callDeploymentStatus(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenReturn(response);
		idco.getInstancesInformation(deploymentContext, callback);	
	}
	
	@Test
	public void getMockInstancesInformationNoErrorsInvalidA4CUuidDeployment() 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);

		Mockito.when(deploymentContext.getDeployment().getId()).thenReturn(ALIEN_DEPLOYMENT_ID_INVALID);
		idco.getInstancesInformation(deploymentContext, callback);	
	}
	
	@Test
	public void getMockInstancesInformationNoSuchFieldException() 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		getMockInstancesInformationError(NoSuchFieldException.class, ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS);
	}
	
	@Test
	public void getMockInstancesInformationOrchestratorIamException() 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		getMockInstancesInformationError(OrchestratorIamException.class, ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS);
	}
	
	@Test
	public void getMockInstancesInformationIOException() 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		getMockInstancesInformationError(IOException.class, ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS);
	}
	
	@Test
	public void getMockInstancesInformationStatusNotFoundException() 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, StatusNotFoundException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		OrchestratorResponse response = 
				new OrchestratorResponse(200, HttpMethod.POST, 
						new StringBuilder(String.format("{\"uuid\": \"%s\", \"status\": \"%s\"}", 
								ORCHESTRATOR_DEPLOYMENT_ID, 
								ORCHESTRATOR_STATUS_DEPLOYMENT_NOT_HANDLED)));
		Mockito.when(orchestratorConnector.callDeploymentStatus(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenReturn(response);
		idco.getInstancesInformation(deploymentContext, callback);
	}
	
	@Disabled
	protected void getMockInstancesInformationError(Class errorClass, String orchestratorStatus) 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSTopologyDeploymentContext deploymentContext = Mockito.mock(PaaSTopologyDeploymentContext.class);
		IPaaSCallback<Map<String, Map<String, InstanceInformation>>> callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		Mockito.when(orchestratorConnector.callDeploymentStatus(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenThrow(errorClass);
		idco.getInstancesInformation(deploymentContext, callback);	
	}
	
	
	@Test
	public void getMockStatusNoErrors() 			
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, 
			IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSDeploymentContext deploymentContext = Mockito.mock(PaaSDeploymentContext.class);
		IPaaSCallback<DeploymentStatus> callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		OrchestratorResponse response = 
				new OrchestratorResponse(200, HttpMethod.POST, 
						new StringBuilder(String.format("{\"uuid\": \"%s\", \"status\": \"%s\"}", 
								ORCHESTRATOR_DEPLOYMENT_ID, 
								ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS)));
		Mockito.when(orchestratorConnector.callDeploymentStatus(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenReturn(response);
		idco.getStatus(deploymentContext, callback);	
	}

	@Test
	public void getMockStatusIOException() 
			throws JsonProcessingException, PluginConfigurationException, 
		NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, OrchestratorIamException {
		getMockStatusError(IOException.class, ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS);
	}
	
	@Test
	public void getMockStatusNoSuchFieldException() 
			throws JsonProcessingException, PluginConfigurationException, 
		NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, OrchestratorIamException {
		getMockStatusError(NoSuchFieldException.class, ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS);
	}
	
	@Test
	public void getMockStatusOrchestratorIamException() 
			throws JsonProcessingException, PluginConfigurationException, 
		NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, OrchestratorIamException {
		getMockStatusError(OrchestratorIamException.class, ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS);
	}
	
	
	@Disabled
	protected void getMockStatusError(Class errorClass, String orchestratorStatus) 
			throws JsonProcessingException, PluginConfigurationException, NoSuchFieldException, IOException, OrchestratorIamException, IllegalArgumentException, IllegalAccessException {
		PaaSDeploymentContext deploymentContext = Mockito.mock(PaaSDeploymentContext.class);
		IPaaSCallback<DeploymentStatus> callback = Mockito.mock(IPaaSCallback.class);
		OrchestratorConnector orchestratorConnector = Mockito.mock(OrchestratorConnector.class);
		IndigoDcOrchestrator idco = setupIndigoDcOrchestratorWithTestConfigDeploy(
				deploymentContext, callback, orchestratorConnector);
		Mockito.when(orchestratorConnector.callDeploymentStatus(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenThrow(errorClass);
		idco.getStatus(deploymentContext, callback);	
	}
	
	@Disabled
	protected IndigoDcOrchestrator setupIndigoDcOrchestratorWithTestConfigDeploy(PaaSDeploymentContext deploymentContext,
			IPaaSCallback callback, OrchestratorConnector orchestratorConnector) 
			throws JsonProcessingException, IOException, PluginConfigurationException, NoSuchFieldException, OrchestratorIamException, 
			IllegalArgumentException, IllegalAccessException {
		IndigoDcOrchestrator idco = getIndigoDcOrchestratorWithTestConfig();
		OrchestratorResponse response = 
				new OrchestratorResponse(200, HttpMethod.POST, 
						new StringBuilder(String.format("{\"uuid\": \"%s\", \"status\": \"%s\"}", 
								ORCHESTRATOR_DEPLOYMENT_ID, 
								ORCHESTRATOR_STATUS_DEPLOYMENT_CREATE_IN_PROGRESS)));//Mockito.mock(OrchestratorResponse.class);
		//Mockito.when(response.getOrchestratorUuidDeployment()).thenReturn("orchestratorUuidDeployment");
		Mockito.when(orchestratorConnector.callDeploy(
				Mockito.<CloudConfiguration>notNull(), 
				Mockito.<String>any(), Mockito.<String>any(), 
				Mockito.<String>any())).thenReturn(response);

		TestUtil.setPrivateField(idco, "orchestratorConnector", orchestratorConnector);	
		
		Deployment deployment = Mockito.mock(Deployment.class);
		DeploymentTopology deploymentTopology =  Mockito.mock(DeploymentTopology.class);
		Mockito.when(deploymentContext.getDeployment()).thenReturn(deployment);
		Mockito.when(deploymentContext.getDeployment().getOrchestratorId()).thenReturn(ORCHESTRATOR_ID);
		Mockito.when(deploymentContext.getDeployment().getId()).thenReturn(ALIEN_DEPLOYMENT_ID);
		
		Map<String, AbstractPropertyValue> vals = new HashMap<>();
		AbstractPropertyValue abstractPropertyValue =  Mockito.mock(AbstractPropertyValue.class);
		PropertyValue propertyValue =  Mockito.mock(PropertyValue.class);
		vals.put("AbstractPropertyValue", abstractPropertyValue);
		vals.put("PropertyValue", propertyValue);
		Mockito.when(deploymentContext.getDeploymentTopology()).thenReturn(deploymentTopology);
		Mockito.when(deploymentContext.getDeploymentTopology().getAllInputProperties()).thenReturn(vals);
		Mockito.when(deploymentContext.getDeploymentTopology().getInitialTopologyId()).thenReturn("initialTopologyId");
		return idco;
	}
	
	@Disabled
	protected IndigoDcOrchestrator getIndigoDcOrchestratorWithTestConfig() 
			throws JsonParseException, JsonMappingException, IOException, PluginConfigurationException, IllegalArgumentException, IllegalAccessException {
		CloudConfigurationManager cfm = new CloudConfigurationManager();
		IndigoDcOrchestrator idco = new IndigoDcOrchestrator();
		TestUtil.setPrivateField(idco, "cloudConfigurationManager", cfm);
		MappingService mappingService = new MappingService();
		mappingService.registerDeploymentInfo(ORCHESTRATOR_DEPLOYMENT_ID,  ALIEN_DEPLOYMENT_ID, ORCHESTRATOR_ID, DeploymentStatus.DEPLOYED);
		TestUtil.setPrivateField(idco, "mappingService", mappingService);
		UserService userService = Mockito.mock(UserService.class);
		User user = new User();
		user.setPlainPassword("plainPassword");
		user.setUsername("username");
		Mockito.when(userService.getCurrentUser()).thenReturn(user);
		TestUtil.setPrivateField(idco, "userService", userService);
		BuilderService builderService = new BuilderService() {
			
			@Override
			  protected Csar getEditionContextManagerCsar() {
				  return Mockito.mock(Csar.class);
			  }
			@Override
			  protected Topology getEditionContextManagerTopology() {
				  return Mockito.mock(Topology.class);
			  }
		};//Mockito.mock(BuilderService.class);
		
		
		EditionContextManager editionContextManager = Mockito.mock(EditionContextManager.class);
		ArchiveExportService exportService = Mockito.mock(ArchiveExportService.class);

		URL url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_a4c.yaml");
	    String yamlA4c =
	        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		Mockito.when(exportService.getYaml(Mockito.<Csar>any(), Mockito.<Topology>any())).thenReturn(yamlA4c);
		Mockito.doNothing().when(editionContextManager).init(Mockito.<String>any());

		TestUtil.setPrivateFieldSuperClass(builderService, "editionContextManager", editionContextManager);
		TestUtil.setPrivateFieldSuperClass(builderService, "exportService", exportService);		
		TestUtil.setPrivateField(idco, "builderService", builderService);	
		
		
		CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_test.json");
		idco.setConfiguration(ORCHESTRATOR_ID, cc);
		return idco;
	}
}
