package es.upv.indigodc.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

import es.upv.indigodc.IndigoDcOrchestratorFactory;

public class CloudConfigurationManagerTest {
	
	@Test
	public void multipleOrchestratorsSameConf() {
		IndigoDcOrchestratorFactory fact = new IndigoDcOrchestratorFactory();
		CloudConfiguration cc = fact.getDefaultConfiguration();
		CloudConfigurationManager ccm = new CloudConfigurationManager();
		ccm.addCloudConfiguration("1", cc);
		ccm.addCloudConfiguration("2", cc);
		CloudConfiguration cc3 = fact.getDefaultConfiguration();
		cc3.setIamHost(IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		ccm.addCloudConfiguration("3", cc3);
		assertEquals(ccm.getCloudConfiguration("3").getIamHost(), IndigoDcOrchestratorFactory.NO_DEFAULT_CONF_FILE);
		assertEquals(ccm.getCloudConfiguration("2"), cc);
		ccm.addCloudConfiguration("2", cc3);
		assertNotSame(ccm.getCloudConfiguration("2"), cc);
		assertEquals(ccm.getCloudConfiguration("2"), cc3);
	}

}
