package es.upv.indigodc.service;


import alien4cloud.model.deployment.Deployment;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
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
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;


@Slf4j
public class BuilderServiceTest {

  public static final String A4C_PAAS_ID = "a4c-paas-id";
  public static final String A4C_ID = "a4c-id";
  public static final String A4C_ORCHESTRATOR_ID = "a4c-orchestrator-id";
  public static final String A4C_VERSION_ID = "a4c-version-id";

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
      Deployment depl = Mockito.mock(Deployment.class);
      Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
      Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
      Mockito.when(ptdc.getDeployment()).thenReturn(depl);
      Mockito.when(depl.getLocationIds()).thenReturn(new String[0]);
      Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
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
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn(new String[0]);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
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
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn(new String[0]);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

  @Test
  public void getAttributeFromStrToMethod() throws URISyntaxException, IOException {
    URL url = BuilderServiceTest.class.getClassLoader().getResource("test_get_attribute_a4c.yaml");
    String yamlA4c =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url = BuilderServiceTest.class.getClassLoader().getResource("test_get_attribute_indigodc.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    CloudConfiguration cc = TestUtil.getTestConfiguration("cloud_conf_import_master_test.json");
    PaaSTopologyDeploymentContext ptdc = Mockito.mock(PaaSTopologyDeploymentContext.class);
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn(new String[0]);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

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
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn(new String[0]);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
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
    Deployment depl = Mockito.mock(Deployment.class);
    Mockito.when(ptdc.getDeploymentPaaSId()).thenReturn(A4C_PAAS_ID);
    Mockito.when(ptdc.getDeploymentId()).thenReturn(A4C_ID);
    Mockito.when(ptdc.getDeployment()).thenReturn(depl);
    Mockito.when(depl.getLocationIds()).thenReturn(new String[0]);
    Mockito.when(depl.getOrchestratorId()).thenReturn(A4C_ORCHESTRATOR_ID);
    Mockito.when(depl.getVersionId()).thenReturn(A4C_VERSION_ID);
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(ptdc, yamlA4c, cc.getImportIndigoCustomTypes()));
  }

  @Test
  public void quoteAllToscaMethods() throws URISyntaxException, IOException {
    URL url =
        BuilderServiceTest.class.getClassLoader().getResource("test_quote_tosca_methods_in.yaml");
    String yamlA4c =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    url =
        BuilderServiceTest.class.getClassLoader().getResource("test_quote_tosca_methods_out.yaml");
    String yamlIndigoDC =
        new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
    Assertions.assertEquals(yamlIndigoDC, BuilderService.encodeToscaMethods(yamlA4c));
  }

  @Test
  public void getEditionContextManagerCsar_null_when_not_executed_by_a4c() {
	  BuilderService bs = new BuilderService();
	  Assertions.assertThrows(NullPointerException.class, () ->{
              bs.getEditionContextManagerCsar();});
  }

  @Test
  public void getEditionContextManagerTopology_null_when_not_executed_by_a4c() {
    BuilderService bs = new BuilderService();
    Assertions.assertThrows(NullPointerException.class, () ->{bs.getEditionContextManagerTopology();});
  }
  
}
