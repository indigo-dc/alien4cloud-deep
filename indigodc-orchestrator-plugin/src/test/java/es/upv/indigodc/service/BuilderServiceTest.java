package es.upv.indigodc.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.junit.Ignore;
import org.junit.Test;

import es.upv.indigodc.IndigoDCOrchestrator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuilderServiceTest {
	
  @Ignore
	@Test
	public void convertYamlTopoEditorToOrchestratorFormatJupyterKubernetesDemo() throws URISyntaxException, IOException {
		URL url = BuilderServiceTest.class.getClassLoader().getResource("test_jupyter_kube_cluster_a4c.yaml");
		String yamlA4c = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		url = BuilderServiceTest.class.getClassLoader().getResource("test_jupyter_kube_cluster_indigodc.yaml");
		String yamlIndigoDC = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		assertEquals(yamlIndigoDC, BuilderService.getIndigoDCTopologyYaml(yamlA4c));
	}
	
  
	@Test
	public void convertYamlTopoEditorToOrchestratorFormatIndigoTypeCompute() throws URISyntaxException, IOException {
		URL url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_a4c.yaml");
		String yamlA4c = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_indigodc.yaml");
		String yamlIndigoDC = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		assertEquals(yamlIndigoDC, BuilderService.getIndigoDCTopologyYaml(yamlA4c));
	}
  
  
	 @Test
	  public void convertYamlTopoEditorToOrchestratorFormatIndigoTypeComputeKepler() throws URISyntaxException, IOException {
	    URL url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_kepler_a4c.yaml");
	    String yamlA4c = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
	    url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_kepler_indigodc.yaml");
	    String yamlIndigoDC = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
	    assertEquals(yamlIndigoDC, BuilderService.getIndigoDCTopologyYaml(yamlA4c));
	  }
	 
	 @Ignore
   @Test
   public void getAttributeFromStrToMethod() throws URISyntaxException, IOException {
     URL url = BuilderServiceTest.class.getClassLoader().getResource("test_get_attribute_a4c.yaml");
     String yamlA4c = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
     url = BuilderServiceTest.class.getClassLoader().getResource("test_get_attribute_indigodc.yaml");
     String yamlIndigoDC = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
     assertEquals(yamlIndigoDC, BuilderService.getIndigoDCTopologyYaml(yamlA4c));
   }
	
	
}
