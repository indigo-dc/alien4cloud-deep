package es.upv.indigodc.location;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.alien4cloud.tosca.catalog.ArchiveParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import alien4cloud.deployment.matching.services.nodes.MatchingConfigurationsParser;
import alien4cloud.model.deployment.matching.MatchingConfiguration;
import alien4cloud.model.orchestrators.locations.LocationResourceTemplate;
import alien4cloud.orchestrators.plugin.ILocationConfiguratorPlugin;
import alien4cloud.orchestrators.plugin.ILocationResourceAccessor;
import alien4cloud.orchestrators.plugin.model.PluginArchive;
import alien4cloud.paas.exception.PluginParseException;
import alien4cloud.plugin.model.ManagedPlugin;
import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.parser.ParsingException;
import alien4cloud.tosca.parser.ParsingResult;
import alien4cloud.utils.AlienConstants;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@Scope("prototype")
public class LocationConfigurator implements ILocationConfiguratorPlugin {
  
  public static final String LOCATION_TYPE = "indigodc";
  
  @Inject
  private ManagedPlugin selfContext;

  @Inject
  private ArchiveParser archiveParser;
  
  @Inject
  private MatchingConfigurationsParser matchingConfigurationsParser;
  
  protected List<PluginArchive> archives;
  protected Map<String, MatchingConfiguration> matchingConfigurations;
  
  @Override
  public List<PluginArchive> pluginArchives() {
      if (archives == null) {
          archives = Lists.newArrayList();
//          try {
//              addToArchive(archives, "provider/common/configuration");
//          } catch (ParsingException e) {
//              log.error(e.getMessage());
//              throw new PluginParseException(e.getMessage());
//          }
      }
      return archives;
  }

  @Override
  public List<String> getResourcesTypes() {
      return getAllResourcesTypes();
  }

  @Override
  public Map<String, MatchingConfiguration> getMatchingConfigurations() {
      return Maps.newHashMap();//getMatchingConfigurations("provider/common/matching/config.yml");
  }

  @Override
  public List<LocationResourceTemplate> instances(ILocationResourceAccessor iLocationResourceAccessor) {
      return Lists.newArrayList();
  }
  
  public Map<String, MatchingConfiguration> getMatchingConfigurations(String matchingConfigRelativePath) {
    if (matchingConfigurations == null) {
        Path matchingConfigPath = selfContext.getPluginPath().resolve(matchingConfigRelativePath);
        try {
            this.matchingConfigurations = matchingConfigurationsParser.parseFile(matchingConfigPath).getResult().getMatchingConfigurations();
        } catch (ParsingException e) {
            return Maps.newHashMap();
        }
    }
    return Maps.newHashMap();//matchingConfigurations;
  }
  
  public List<String> getAllResourcesTypes() {
    List<String> resourcesTypes = Lists.newArrayList();
    for (PluginArchive pluginArchive : this.pluginArchives()) {
        for (String nodeType : pluginArchive.getArchive().getNodeTypes().keySet()) {
            resourcesTypes.add(nodeType);
        }
    }
    return resourcesTypes;
}
  
  private void addToArchive(List<PluginArchive> archives, String path) throws ParsingException {
    Path archivePath = selfContext.getPluginPath().resolve(path);
    // Parse the archives
    ParsingResult<ArchiveRoot> result = archiveParser.parseDir(archivePath, AlienConstants.GLOBAL_WORKSPACE_ID);
    PluginArchive pluginArchive = new PluginArchive(result.getResult(), archivePath);
    archives.add(pluginArchive);
}

}
