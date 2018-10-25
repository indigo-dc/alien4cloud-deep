package es.upv.indigodc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import es.upv.indigodc.configuration.CloudConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IndigoDcOrchestratorFactoryTest {
	
//	@Test
//	public void testDefaultConfNoPrivateInfo() {
//		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
//		CloudConfiguration cc = fact.getDefaultConfiguration();
//
//		Assertions.assertEquals(cc.getClientId(), "none");
//		Assertions.assertEquals(cc.getClientSecret(), "none");
//	}
	
	@Test
	public void badPathToDefaultConf() throws NoSuchFieldException, SecurityException, Exception {		
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactoryBadPath();
		CloudConfiguration cc = fact.getDefaultConfiguration();
		Assertions.assertEquals(cc.getClientId(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getClientScopes(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getClientSecret(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getIamHost(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getIamHostCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getImportIndigoCustomTypes(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpoint(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getTokenEndpoint(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorPollInterval(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE_POLL);
	}
	
	protected static class IndigoDcOrchestratorFactoryBadPath  extends IndigoDcOrchestratorFactory{
		
		@Override
		  protected String getCloudConfDefaultFile() {
			  return "fake";
		  }
	}

}
