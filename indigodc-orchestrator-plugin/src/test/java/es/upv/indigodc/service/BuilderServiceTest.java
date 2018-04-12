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

import org.junit.Test;

import es.upv.indigodc.IndigoDCOrchestrator;
import es.upv.indigodc.service.model.ToscaYamlIndigoDC;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuilderServiceTest {
	
	@Test
	public void convertYamlTopoEditorToOrchestratorFormatJupyterKubernetesDemo() throws URISyntaxException, IOException {
		URL url = BuilderServiceTest.class.getClassLoader().getResource("test_jupyter_kube_cluster_a4c.yaml");
		String yamlA4c = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		url = BuilderServiceTest.class.getClassLoader().getResource("test_jupyter_kube_cluster_indigodc.yaml");
		String yamlIndigoDC = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		ToscaYamlIndigoDC toscaYamlIndigoDC = new ToscaYamlIndigoDC(yamlA4c);
		//log.info(toscaYamlIndigoDC.getIndigoDCTopologyYaml());
		assertEquals(yamlIndigoDC, toscaYamlIndigoDC.getIndigoDCTopologyYaml());
	}
	
	@Test
	public void convertYamlTopoEditorToOrchestratorFormatIndigoTypeCompute() throws URISyntaxException, IOException {
		URL url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_a4c.yaml");
		String yamlA4c = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		url = BuilderServiceTest.class.getClassLoader().getResource("test_compute_indigodc.yaml");
		String yamlIndigoDC = new String(Files.readAllBytes(Paths.get(url.getPath())), StandardCharsets.UTF_8);
		ToscaYamlIndigoDC toscaYamlIndigoDC = new ToscaYamlIndigoDC(yamlA4c);
		//log.info(toscaYamlIndigoDC.getIndigoDCTopologyYaml());
		assertEquals(yamlIndigoDC, toscaYamlIndigoDC.getIndigoDCTopologyYaml());
	}
	
	
}
