package es.upv.indigodc.service;

import org.springframework.stereotype.Service;

/**
 * Maintains a list of supported TOSCA types by the plugin.
 *
 * @author asalic
 */
@Service
public class ArtifactRegistryService {

  /** The list of supported TOSCA types. */
  private static String[] SUPPORTED_TOSCA_TYPES = {
      // "alien.nodes.LinuxFileSystem",
      // "tosca.nodes.BlockStorage",
      // "tosca.nodes.Container.Application",
      // "tosca.nodes.Container.Application.Docker",
      // "tosca.nodes.Container.Runtime",
      // "tosca.nodes.DBMS",
      // "tosca.nodes.DBMS.MySQL",
      // "tosca.nodes.Database",
      // "tosca.nodes.Database.MySQL",
      // "tosca.nodes.LoadBalancer",
      // "tosca.nodes.Network",
      // "tosca.nodes.ObjectStorage",
      // "tosca.nodes.Root",
      // "tosca.nodes.SoftwareComponent",
      // "tosca.nodes.WebApplication",
      // "tosca.nodes.WebServer",
      // "tosca.nodes.WebServer.Apache",
      // "tosca.nodes.indigo.Ambertools",
      // "tosca.nodes.indigo.CmsServices",
      // "tosca.nodes.indigo.CmsWnConfig",
      // "tosca.nodes.indigo.Compute",
      // "tosca.nodes.indigo.Container.Application.Docker",
      // "tosca.nodes.indigo.Container.Application.Docker.Chronos",
      // "tosca.nodes.indigo.Container.Application.Docker.Marathon",
      // "tosca.nodes.indigo.Container.Runtime.Docker",
      // "tosca.nodes.indigo.DariahRepository",
      // "tosca.nodes.indigo.Database.MySQL",
      // "tosca.nodes.indigo.Database.MySQL",
      // "tosca.nodes.indigo.ElasticCluster",
      // "tosca.nodes.indigo.MonitoredCompute",
      // "tosca.nodes.indigo.GalaxyPortal",
      // "tosca.nodes.indigo.GalaxyPortalAndStorage",
      // "alien.nodes.indigodc.Container",
      // "alien.nodes.indigodc.ComputeN",
      // "tosca.nodes.Compute",
      // "tosca.nodes.Database",
      // "tosca.nodes.indigodc-plugin.MyCompute",
      // "tosca.nodes.indigodc-plugin.MyIndigoDCCompute:"

  };

  /**
   * Get an array of the supported artifact types.
   *
   * @return An array of the supported artifact types.
   */
  public String[] getSupportedArtifactTypes() {
    return SUPPORTED_TOSCA_TYPES;
  }
}
