package es.upv.indigodc.service;


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


@Slf4j
public class BuilderServiceTest {

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
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(yamlA4c, cc.getImportIndigoCustomTypes()));
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
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(yamlA4c, cc.getImportIndigoCustomTypes()));
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
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(yamlA4c, cc.getImportIndigoCustomTypes()));
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
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(yamlA4c, cc.getImportIndigoCustomTypes()));
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
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(yamlA4c, cc.getImportIndigoCustomTypes()));
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
    Assertions.assertEquals(
        yamlIndigoDC,
        BuilderService.getIndigoDcTopologyYaml(yamlA4c, cc.getImportIndigoCustomTypes()));
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
  
}
