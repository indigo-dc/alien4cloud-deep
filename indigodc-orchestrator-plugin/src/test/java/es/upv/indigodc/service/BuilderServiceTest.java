package es.upv.indigodc.service;


import alien4cloud.model.deployment.Deployment;
import alien4cloud.model.deployment.DeploymentTopology;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import com.google.common.collect.Maps;
import es.upv.indigodc.IndigoDcOrchestrator;
import es.upv.indigodc.TestUtil;
import es.upv.indigodc.configuration.CloudConfiguration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.alien4cloud.tosca.editor.EditionContextManager;
import org.alien4cloud.tosca.exporter.ArchiveExportService;
import org.alien4cloud.tosca.model.Csar;
import org.alien4cloud.tosca.model.definitions.AbstractPropertyValue;
import org.alien4cloud.tosca.model.definitions.PropertyValue;
import org.alien4cloud.tosca.model.definitions.ScalarPropertyValue;
import org.alien4cloud.tosca.model.templates.Topology;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;


@Slf4j
public class BuilderServiceTest {

  public static final String INPUT_NAME = "min_instances";
  public static final String INPUT_VALUE = "3";
  public static final String IMPORT_TOSCA_INDIGODC = "https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml";
  public static final String CALLBACK = "http://localhost";
  public static final String A4C_DEPLOYMENT_TOPOLOGY_ID = "a4c-deployment-topology-id";
  public static final String A4C_PAAS_ID = "a4c-paas-id";
  public static final String A4C_ID = "a4c-id";
  public static final String A4C_ORCHESTRATOR_ID = "a4c-orchestrator-id";
  public static final String A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID = "a4c-deployment-orchestrator-deployment-id";
  public static final String A4C_VERSION_ID = "a4c-version-id";
  public static final String[] A4C_LOCATIONS_IDS = {"a4c-location-id1", "a4c-location-id2"};

	@Disabled
  @Test
  public void convertYamlTopoEditorToOrchestratorFormatJupyterKubernetesDemo()
      throws URISyntaxException, IOException {
    URL url =
        BuilderServiceTest.class.getClassLoader().getResource("test_jupyter_kube_cluster_a4c.yaml");
    String yamlA4c =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url =
        BuilderServiceTest.class
            .getClassLoader()
            .getResource("test_jupyter_kube_cluster_indigodc.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_import_master_test.json");
      PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
      DeploymentTopology deploymentTopology = Mockito.mock(DeploymentTopology.class);
      Mockito.when(deploymentTopology.getId()).thenReturn(A4C_DEPLOYMENT_TOPOLOGY_ID);
      Deployment depl = Mockito.mock(Deployment.class);
      Mockito.when(ptdc.getDeploymentTopology()).thenReturn(deploymentTopology);
      Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
      Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
      Mockito.when(ptdc.getDeployment()).thenReturn(depl);
      Mockito.when(depl.getLocationIds()).thenReturn( A4C_LOCATIONS_IDS);
      Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
      Mockito.when(depl.getOrchestratorDeploymentId()).thenReturn(A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID);
      Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

  @Test
  public void convertYamlTopoEditorToOrchestratorFormatIndigoTypeCompute()
      throws URISyntaxException, IOException {
    URL url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_a4c.yaml");
    String yamlA4c =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_indigodc.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_import_master_test.json");
    PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
    DeploymentTopology deploymentTopology = Mockito.mock(DeploymentTopology.class);
    Mockito.when(deploymentTopology.getId()).thenReturn(A4C_DEPLOYMENT_TOPOLOGY_ID);
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentTopology()).thenReturn(deploymentTopology);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn( A4C_LOCATIONS_IDS);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getOrchestratorDeploymentId()).thenReturn(A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

  @Test
  public void convertYamlTopoEditorToOrchestratorFormatIndigoTypeComputeKepler()
      throws URISyntaxException, IOException {
    URL url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_kepler_a4c.yaml");
    String yamlA4c =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url =
        BuilderServiceTest.class.getClassLoader().getResource("test_compute_kepler_indigodc.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_import_master_test.json");
    PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
    DeploymentTopology deploymentTopology = Mockito.mock(DeploymentTopology.class);
    Mockito.when(deploymentTopology.getId()).thenReturn(A4C_DEPLOYMENT_TOPOLOGY_ID);
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentTopology()).thenReturn(deploymentTopology);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn( A4C_LOCATIONS_IDS);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getOrchestratorDeploymentId()).thenReturn(A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

//  @Test
//  public void getAttributeFromStrToMethod() throws URISyntaxException, IOException {
//    URL url = BuilderServiceTest.class.getClassLoader().getResource("test_get_attribute_a4c.yaml");
//    String yamlA4c =
//        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
//    url = BuilderServiceTest.class.getClassLoader().getResource("test_get_attribute_indigodc.yaml");
//    String yamlIndigoDC =
//        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
//    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_import_master_test.json");
//    PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
//    DeploymentTopology deploymentTopology = Mockito.mock(DeploymentTopology.class);
//    Mockito.when(deploymentTopology.getId()).thenReturn(A4C_DEPLOYMENT_TOPOLOGY_ID);
//    Deployment depl = Mockito.mock(Deployment.class);
//    Mockito.when(ptdc.getDeploymentTopology()).thenReturn(deploymentTopology);
//    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
//    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
//    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
//    Mockito.when(depl.getLocationIds()).thenReturn( A4C_LOCATIONS_IDS);
//    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
//    Mockito.when(depl.getOrchestratorDeploymentId()).thenReturn(A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID);
//    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
//    Assertions.assertEquals(
//        yamlIndigoDC,
//        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
//  }

  @Test
  public void rmEmptyNullPropertiesNodes() throws URISyntaxException, IOException {
    URL url =
        BuilderServiceTest.class
            .getClassLoader()
            .getResource("test_empty_null_property_rm_a4c.yaml");
    String yamlA4c =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url =
        BuilderServiceTest.class
            .getClassLoader()
            .getResource("test_empty_null_property_rm_orchestrator.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_import_master_test.json");
    PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
    DeploymentTopology deploymentTopology = Mockito.mock(DeploymentTopology.class);
    Mockito.when(deploymentTopology.getId()).thenReturn(A4C_DEPLOYMENT_TOPOLOGY_ID);
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentTopology()).thenReturn(deploymentTopology);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn( A4C_LOCATIONS_IDS);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getOrchestratorDeploymentId()).thenReturn(A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

  @Test
  public void rmEmptyNullPropertiesNodesAndRmEmptyProperties()
      throws URISyntaxException, IOException {
    URL url =
        BuilderServiceTest.class
            .getClassLoader()
            .getResource("test_empty_null_property_rm_properties_rm_a4c.yaml");
    String yamlA4c =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url =
        BuilderServiceTest.class
            .getClassLoader()
            .getResource("test_empty_null_property_rm_properties_rm_orchestrator.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_import_master_test.json");
    PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
    DeploymentTopology deploymentTopology = Mockito.mock(DeploymentTopology.class);
    Mockito.when(deploymentTopology.getId()).thenReturn(A4C_DEPLOYMENT_TOPOLOGY_ID);
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentTopology()).thenReturn(deploymentTopology);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn( A4C_LOCATIONS_IDS);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getOrchestratorDeploymentId()).thenReturn(A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

//  @Test
//  public void quoteAllToscaMethods() throws URISyntaxException, IOException {
//    URL url =
//        BuilderServiceTest.class.getClassLoader().getResource("test_quote_tosca_methods_in.yaml");
//    String yamlA4c =
//        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
//    url =
//        BuilderServiceTest.class.getClassLoader().getResource("test_quote_tosca_methods_out.yaml");
//    String yamlIndigoDC =
//        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
//    Assertions.assertEquals(yamlIndigoDC, BuilderService.encodeToscaMethods(yamlA4c));
//  }

  @Test
  public void getEditionContextManagerCsarNullWhenNotExecutedByA4c() {
	  BuilderService bs = new BuilderService();
	  Assertions.assertThrows(NullPointerException.class, () ->{
              bs.getEditionContextManagerCsar();});
  }

  @Test
  public void getEditionContextManagerTopologyNullWhenNotExecutedByA4c() {
    BuilderService bs = new BuilderService();
    Assertions.assertThrows(NullPointerException.class, () ->{bs.getEditionContextManagerTopology();});
  }

  @Test
  public void buildAppOneComputeNode() throws IOException, IllegalAccessException {
    BuilderService bs = new BuilderService(){
      @Override
      protected Csar getEditionContextManagerCsar() {
        return Mockito.mock(Csar.class);
      }

      @Override
      protected Topology getEditionContextManagerTopology() {
        return Mockito.mock(Topology.class);
      }
    };
    URL url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_a4c.yaml");
    String yamlA4c =
            new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_indigodc_orchestrator.yaml");
    String yamlIndigoDC =
            new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
    DeploymentTopology deploymentTopology = Mockito.mock(DeploymentTopology.class);
    Mockito.when(deploymentTopology.getId()).thenReturn(A4C_DEPLOYMENT_TOPOLOGY_ID);
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentTopology()).thenReturn(deploymentTopology);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn( A4C_LOCATIONS_IDS);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getOrchestratorDeploymentId()).thenReturn(A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Map<String, AbstractPropertyValue> inputs = new HashMap<>();
    inputs.put(INPUT_NAME, new ScalarPropertyValue(INPUT_VALUE));
    Mockito.when(deploymentTopology.getAllInputProperties()).thenReturn(inputs);

    EditionContextManager editionContextManager = Mockito.mock(EditionContextManager.class);
    ArchiveExportService exportService = Mockito.mock(ArchiveExportService.class);
    Mockito.when(exportService.getYaml(Mockito.any(), Mockito.any())).thenReturn(yamlA4c);

    String clasz = bs.getClass().getCanonicalName();
    TestUtil.setPrivateFieldSuperClass(bs, "editionContextManager", editionContextManager);
    TestUtil.setPrivateFieldSuperClass(bs, "exportService", exportService);
    Assertions.assertEquals(yamlIndigoDC, bs.buildApp(ptdc, IMPORT_TOSCA_INDIGODC, CALLBACK));
  }
  
}
