package es.upv.indigodc.location;

import alien4cloud.model.deployment.matching.MatchingConfiguration;
import alien4cloud.model.orchestrators.locations.LocationResourceTemplate;
import alien4cloud.orchestrators.plugin.ILocationConfiguratorPlugin;
import alien4cloud.orchestrators.plugin.ILocationResourceAccessor;
import alien4cloud.orchestrators.plugin.model.PluginArchive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Factory used to create a location configurator for the plugin. Using this approach we can create
 * multiple location by creating a configurator for each.
 *
 * @author asalic
 */
@Slf4j
@Component
@Scope("prototype")
public class LocationConfiguratorFactory {

  /** The context of the whole application. */
  @Inject
  private ApplicationContext applicationContext;

  /**
   * Generates a new instance for a location.
   *
   * @param locationType RThe type of location to be generated.
   * @return The instance of the location.
   */
  public ILocationConfiguratorPlugin newInstance(String locationType) {

    if (LocationConfigurator.LOCATION_TYPE.equals(locationType)) {
      LocationConfigurator configurator = applicationContext.getBean(LocationConfigurator.class);
      return configurator;
    }
    return new ILocationConfiguratorPlugin() {
      @Override
      public List<PluginArchive> pluginArchives() {
        return new ArrayList<>();
      }

      @Override
      public List<String> getResourcesTypes() {
        return new ArrayList<>();
      }

      @Override
      public Map<String, MatchingConfiguration> getMatchingConfigurations() {
        return new HashMap<>();
      }

      @Override
      public List<LocationResourceTemplate> instances(ILocationResourceAccessor resourceAccessor) {
        return null;
      }
    };
  }
}
