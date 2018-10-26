package es.upv.indigodc.location;

import alien4cloud.deployment.matching.services.nodes.MatchingConfigurationsParser;
import alien4cloud.model.deployment.matching.MatchingConfiguration;
import alien4cloud.model.orchestrators.locations.LocationResourceTemplate;
import alien4cloud.orchestrators.plugin.ILocationConfiguratorPlugin;
import alien4cloud.orchestrators.plugin.ILocationResourceAccessor;
import alien4cloud.orchestrators.plugin.model.PluginArchive;
import alien4cloud.plugin.model.ManagedPlugin;
import alien4cloud.tosca.parser.ParsingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class contains the configuration setup for a plugin location. We support only one location.
 *
 * @author asalic
 */
@Component
@Scope("prototype")
public class LocationConfigurator implements ILocationConfiguratorPlugin {

  /** The name of the location. */
  public static final String LOCATION_TYPE = "Deep Orchestrator Location";

  @Inject
  private ManagedPlugin selfContext;

  // @Inject private ArchiveParser archiveParser;

  @Inject
  private MatchingConfigurationsParser matchingConfigurationsParser;

  protected List<PluginArchive> archives;
  protected Map<String, MatchingConfiguration> matchingConfigurations;

  @Override
  public List<PluginArchive> pluginArchives() {
    if (archives == null) {
      archives = Lists.newArrayList();
      // try {
      // addToArchive(archives, "provider/common/configuration");
      // } catch (ParsingException e) {
      // log.error(e.getMessage());
      // throw new PluginParseException(e.getMessage());
      // }
    }
    return archives;
  }

  @Override
  public List<String> getResourcesTypes() {
    return getAllResourcesTypes();
  }

  @Override
  public List<LocationResourceTemplate> instances(
      ILocationResourceAccessor locationResourceAccessor) {
    return Lists.newArrayList();
  }
  
  @Override
  public Map<String, MatchingConfiguration> getMatchingConfigurations() {
    // return getMatchingConfigurations("provider/common/matching/config.yml");
    // Match all nodes defined in the Alien4Cloud list of components
    return Maps.newHashMap();
  }

  /**
   * Returns the matching nodes provided by a location.
   *
   * @param matchingConfigRelativePath file containing the the rules used to match the nodes of the
   *        location
   * @return A list of locations resources templates that users can define or null if the plugin
   *         doesn't support auto-configuration of resources..
   */
  public Map<String, MatchingConfiguration> getMatchingConfigurations(
      String matchingConfigRelativePath) {
    if (matchingConfigurations == null) {
      Path matchingConfigPath = selfContext.getPluginPath().resolve(matchingConfigRelativePath);
      try {
        this.matchingConfigurations = matchingConfigurationsParser.parseFile(matchingConfigPath)
            .getResult().getMatchingConfigurations();
      } catch (ParsingException er) {
        return Maps.newHashMap();
      }
    }
    return matchingConfigurations;
  }

  private List<String> getAllResourcesTypes() {
    List<String> resourcesTypes = Lists.newArrayList();
    for (PluginArchive pluginArchive : this.pluginArchives()) {
      for (String nodeType : pluginArchive.getArchive().getNodeTypes().keySet()) {
        resourcesTypes.add(nodeType);
      }
    }
    return resourcesTypes;
  }
  //
  // private void addToArchive(List<PluginArchive> archives, String path) throws ParsingException {
  // Path archivePath = selfContext.getPluginPath().resolve(path);
  // // Parse the archives
  // ParsingResult<ArchiveRoot> result =
  // archiveParser.parseDir(archivePath, AlienConstants.GLOBAL_WORKSPACE_ID);
  // PluginArchive pluginArchive = new PluginArchive(result.getResult(), archivePath);
  // archives.add(pluginArchive);
  // }
}
