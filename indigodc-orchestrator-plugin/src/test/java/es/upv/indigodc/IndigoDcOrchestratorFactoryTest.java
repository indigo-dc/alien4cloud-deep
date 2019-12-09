package es.upv.indigodc;

import java.util.ArrayList;
import java.util.List;

import es.upv.indigodc.service.ArtifactRegistryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;

import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.location.LocationConfigurator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IndigoDcOrchestratorFactoryTest {
	
	protected static class IndigoDcOrchestratorFactoryBadPath  extends IndigoDcOrchestratorFactory{
		
		@Override
		  protected String getCloudConfDefaultFile() {
			  return "fake";
		  }
	}
	
	@Test
	public void badPathToDefaultConf() throws NoSuchFieldException, SecurityException, Exception {		
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactoryBadPath();
		CloudConfiguration cc = fact.getDefaultConfiguration();
		Assertions.assertEquals(cc.getImportIndigoCustomTypes(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpoint(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorPollInterval(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE_POLL);
	}
	
	@Test
	public void destroy_call_orchestrator_destroy() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		IndigoDcOrchestrator orc = Mockito.mock(IndigoDcOrchestrator.class);
		final List<Integer> destExec = new ArrayList<>();
		Mockito.doAnswer((i) -> { destExec.add(0);return null;}).when(orc).destroy();
		fact.destroy(orc);
		Assertions.assertTrue(!destExec.isEmpty());
	}
	
	@Test
	public void getDeploymentPropertyDefinitions_empty() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		Assertions.assertTrue(fact.getDeploymentPropertyDefinitions().isEmpty());
	}
	
	@Test
	public void getLocationSupport_unique() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		Assertions.assertEquals(fact.getLocationSupport().getTypes().length, 1);
		Assertions.assertFalse(fact.getLocationSupport().isMultipleLocations());		
	}
	
	@Test
	public void getLocationSupport_is_LocationConfigurator_LOCATION_TYPE() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		Assertions.assertEquals(fact.getLocationSupport().getTypes()[0], LocationConfigurator.LOCATION_TYPE);
		
	}
	
	@Test
	public void getType_is_IndigoDcOrchestrator_TYPE() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		Assertions.assertEquals(fact.getType(), IndigoDcOrchestrator.TYPE);
		
	}
	
	@Test
	public void newInstance_creates_new_IndigoDcOrchestrator_with_default_opts() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		BeanFactory bf = Mockito.mock(BeanFactory.class);
		Mockito.when(bf.getBean(IndigoDcOrchestrator.class)).thenReturn(new IndigoDcOrchestrator());
		TestUtil.setPrivateField(fact, "beanFactory", bf);
		Assertions.assertEquals(fact.newInstance().getClass(), IndigoDcOrchestrator.class);
	}

	@Test
	public void getArtifactSupport_with_empty_artifacts() {
		ArtifactRegistryService ars = Mockito.mock(ArtifactRegistryService.class);
		Mockito.when(ars.getSupportedArtifactTypes()).thenReturn(new String[0]);
		IndigoDcOrchestratorFactory factory = new IndigoDcOrchestratorFactory();
		TestUtil.setPrivateField(factory, "artifactRegistryService", ars);
		Assertions.assertTrue(factory.getArtifactSupport().getTypes().length == 0);
	}

	@Test
	public void getConfigurationType_is_CloudConfiguration() {
		IndigoDcOrchestratorFactory factory = new IndigoDcOrchestratorFactory();
		Assertions.assertTrue(factory.getConfigurationType().getCanonicalName()
				.equals(CloudConfiguration.class.getCanonicalName()));
	}

}
