package es.upv.indigodc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import es.upv.indigodc.configuration.CloudConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IndigoDcOrchestratorFactoryTest {
	
	@Test
	public void testDefaultConfNoPrivateInfo() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		CloudConfiguration cc = fact.getDefaultConfiguration();

		assertEquals(cc.getClientId(), "none");
		assertEquals(cc.getClientSecret(), "none");
	}
	
	@Test
	public void badPathToDefaultConf() throws NoSuchFieldException, SecurityException, Exception {		
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactoryBadPath();
		CloudConfiguration cc = fact.getDefaultConfiguration();
		assertEquals(cc.getClientId(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getClientScopes(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getClientSecret(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getIamHost(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getIamHostCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getImportIndigoCustomTypes(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getOrchestratorEndpoint(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getTokenEndpoint(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(cc.getOrchestratorPollInterval(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE_POLL);
	}
	
	protected static class IndigoDcOrchestratorFactoryBadPath  extends IndigoDcOrchestratorFactory{
		
		@Override
		  protected String getCloudConfDefaultFile() {
			  return "fake";
		  }
	}

}
