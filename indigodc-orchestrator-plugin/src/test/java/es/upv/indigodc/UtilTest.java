package es.upv.indigodc;

import org.junit.Test;

import es.upv.indigodc.service.model.StatusNotFoundException;

public class UtilTest {
	
	@Test
	public void checkAllSupportedA4CDeploymentStatuses() throws StatusNotFoundException {
		for (IndigoDcDeploymentStatus val:IndigoDcDeploymentStatus.values()) {
			Util.indigoDcStatusToDeploymentStatus(val.name());
			Util.indigoDcStatusToInstanceStatus(val.name());
		}
	}
	
	
	@Test(expected=StatusNotFoundException.class)
	public void checkAllSupportedA4CDeploymentStatusesStatusNotFound() throws StatusNotFoundException {
			Util.indigoDcStatusToDeploymentStatus("bad value");
			Util.indigoDcStatusToInstanceStatus("bad value");
	}
	

}
