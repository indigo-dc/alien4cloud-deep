package es.upv.indigodc.location;

import es.upv.indigodc.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

public class LocationConfiguratorFactoryTest {

    @Test
    public void newInstance_match_predefined_instance_name() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        LocationConfigurator lc = Mockito.mock(LocationConfigurator.class);
        Mockito.when(applicationContext.getBean(LocationConfigurator.class))
                .thenReturn(lc);
        LocationConfiguratorFactory lcf = new LocationConfiguratorFactory();
        TestUtil.setPrivateField(lcf, "applicationContext", applicationContext);
        Assertions.assertEquals(lc, lcf.newInstance(LocationConfigurator.LOCATION_TYPE));
    }

    @Test
    public void newInstance_no_match_predefined_instance_name() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        LocationConfigurator lc = Mockito.mock(LocationConfigurator.class);
        Mockito.when(applicationContext.getBean(LocationConfigurator.class))
                .thenReturn(lc);
        LocationConfiguratorFactory lcf = new LocationConfiguratorFactory();
        TestUtil.setPrivateField(lcf, "applicationContext", applicationContext);
        Assertions.assertEquals(null, lcf.newInstance("<NO_MATCH>"));
    }
}
