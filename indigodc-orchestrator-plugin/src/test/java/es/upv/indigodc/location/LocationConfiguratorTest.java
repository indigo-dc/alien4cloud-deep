package es.upv.indigodc.location;

import alien4cloud.deployment.matching.services.nodes.MatchingConfigurations;
import alien4cloud.deployment.matching.services.nodes.MatchingConfigurationsParser;
import alien4cloud.orchestrators.plugin.model.PluginArchive;
import alien4cloud.plugin.model.ManagedPlugin;
import alien4cloud.tosca.parser.ParsingException;
import alien4cloud.tosca.parser.ParsingResult;
import com.google.common.collect.Maps;
import es.upv.indigodc.TestUtil;
import es.upv.indigodc.service.BuilderServiceTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LocationConfiguratorTest {


    @Test
    public void pluginArchives_empty() {
        LocationConfigurator locationConfiguratorLocal = new LocationConfigurator();
        TestUtil.setPrivateField(locationConfiguratorLocal, "archives", null);
        Assertions.assertEquals(locationConfiguratorLocal.pluginArchives().isEmpty(), true);
    }

    @Test
    public void getResourcesTypes_empty_archives_empty() {
        LocationConfigurator locationConfiguratorLocal = new LocationConfigurator();
        TestUtil.setPrivateField(locationConfiguratorLocal, "archives", new ArrayList<PluginArchive>());
        Assertions.assertEquals(locationConfiguratorLocal.getResourcesTypes().isEmpty(), true);
    }

    @Test
    public void instances_empty() {
        LocationConfigurator locationConfigurator = new LocationConfigurator();
        Assertions.assertEquals(locationConfigurator.instances(null).isEmpty(), true);
    }

    @Test
    public void getMatchingConfigurations_no_configuration_provided() {
        LocationConfigurator locationConfigurator = new LocationConfigurator();
        ManagedPlugin mp = Mockito.mock(ManagedPlugin.class);
        Mockito.when(mp.getPluginPath()).thenReturn(Paths.get("test"));
        TestUtil.setPrivateField(locationConfigurator, "selfContext", mp);
        Assertions.assertEquals(locationConfigurator.getMatchingConfigurations(), null);
    }
    
    @Test
    public void getMatchingConfigurations_empty_configuration_provided() throws URISyntaxException, ParsingException {
        LocationConfigurator locationConfigurator = new LocationConfigurator();
        String pathTest = "provider/common/matching/config.yml";
        Path path = Paths.get(BuilderServiceTest.class
        		.getClassLoader().getResource(pathTest)
        		.toURI());
        ManagedPlugin mp = Mockito.mock(ManagedPlugin.class);
        Path pluginPath = Mockito.mock(Path.class);
        Mockito.when(mp.getPluginPath()).thenReturn(pluginPath);
        Mockito.when(pluginPath.resolve(pathTest)).thenReturn(path);
        MatchingConfigurationsParser matchingConfigurationsParser = Mockito.mock(MatchingConfigurationsParser.class);
        ParsingResult<MatchingConfigurations> pr = Mockito.mock(ParsingResult.class);
        Mockito.when(matchingConfigurationsParser.parseFile(path)).thenReturn(pr);
        MatchingConfigurations mcs = Mockito.mock(MatchingConfigurations.class);
        Mockito.when(pr.getResult()).thenReturn(mcs);
        Mockito.when(mcs.getMatchingConfigurations()).thenReturn(Maps.newHashMap());

        
        TestUtil.setPrivateField(locationConfigurator, "selfContext", mp);
        TestUtil.setPrivateField(locationConfigurator, "matchingConfigurationsParser", matchingConfigurationsParser);
        Assertions.assertTrue(locationConfigurator.getMatchingConfigurations().isEmpty());
    }

}
