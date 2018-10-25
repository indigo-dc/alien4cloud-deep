package es.upv.indigodc.service.model;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrchestratorResponseTest {
	
	public final static String ORCHESTRATOR_RESPONSE_WITH_OUTPUT = " {\"uuid\":\"\",\"creationTime\":\"2018-10-03T15:09+0000\",\"updateTime\":\"2018-10-03T15:09+0000\",\"physicalId\":\"e77e56c6-c71b-11e8-8b1b-300000000002\",\"status\":\"CREATE_COMPLETE\",\"outputs\":{\"mesos_master_server_public_address\":[\"10.10.10.10\"]},\"task\":\"NONE\",\"callback\":\"http://localhost:8080/callback\",\"cloudProviderName\":\"provider-IFCA-LCG2\",\"createdBy\":{\"issuer\":\"https:///\",\"subject\":\"efd877c8-c9fe-4544-8f0c-6a0fac1f75ca\"},\"links\":[{\"rel\":\"self\",\"href\":\"http:///deployments/\"},{\"rel\":\"resources\",\"href\":\"http:///deployments//resources\"},{\"rel\":\"template\",\"href\":\"http:///deployments//template\"}]}";
	public final static String ORCHESTRATOR_RESPONSE_UUID = " {\"uuid\":\"1234\"}";
	public final static String ORCHESTRTOR_RESPONS_MALFORMED = "...";
	
	@Test
	public void parseOutputsSingleKeyListValue() throws JsonProcessingException, IOException {
		String ORCHESTRATOR_RESPONSE_WITH_OUTPUT = " {\"uuid\":\"\",\"creationTime\":\"2018-10-03T15:09+0000\",\"updateTime\":\"2018-10-03T15:09+0000\",\"physicalId\":\"e77e56c6-c71b-11e8-8b1b-300000000002\",\"status\":\"CREATE_COMPLETE\",\"outputs\":{\"mesos_master_server_public_address\":[\"10.10.10.10\"]},\"task\":\"NONE\",\"callback\":\"http://localhost:8080/callback\",\"cloudProviderName\":\"provider-IFCA-LCG2\",\"createdBy\":{\"issuer\":\"https:///\",\"subject\":\"efd877c8-c9fe-4544-8f0c-6a0fac1f75ca\"},\"links\":[{\"rel\":\"self\",\"href\":\"http:///deployments/\"},{\"rel\":\"resources\",\"href\":\"http:///deployments//resources\"},{\"rel\":\"template\",\"href\":\"http:///deployments//template\"}]}";

		StringBuilder response = new StringBuilder(ORCHESTRATOR_RESPONSE_WITH_OUTPUT);
		OrchestratorResponse or = new OrchestratorResponse(200, HttpMethod.GET, response);
		Map<String, String> outputs = or.getOutputs();
		
		String output = outputs.get("mesos_master_server_public_address");
		assertEquals(output, "[ \"10.10.10.10\" ]");
	}
	
	
	/**
	 * Check behaviour when response is empty
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	@Test
	public void emptyResponse() throws JsonProcessingException, IOException {
		StringBuilder response = new StringBuilder("");
		OrchestratorResponse or = new OrchestratorResponse(200, HttpMethod.GET, response);
		Map<String, String> outputs = or.getOutputs();
		assertEquals(outputs.isEmpty(), true);
	}
	
	@Test
	public void getOrchestratorUuidDeployment() throws JsonProcessingException, IOException {
		StringBuilder response = new StringBuilder(ORCHESTRATOR_RESPONSE_UUID);
		OrchestratorResponse or = new OrchestratorResponse(200, HttpMethod.GET, response);

		assertEquals(or.getOrchestratorUuidDeployment(), "1234");
	}
	
	@Test
	public void errorOnMalformedOrchestratorResponseJson() throws JsonProcessingException, IOException {
		StringBuilder response = new StringBuilder(ORCHESTRTOR_RESPONS_MALFORMED);
		Assertions.assertThrows(JsonProcessingException.class,
				() -> {OrchestratorResponse or = new OrchestratorResponse(200, HttpMethod.GET, response);});
	}
	
	@Test
	public void codeNotOkInResponse() throws JsonProcessingException, IOException {
		OrchestratorResponse or = new OrchestratorResponse(199, HttpMethod.GET, new StringBuilder(""));
		assertTrue(!or.isCodeOk());
		or = new OrchestratorResponse(200, HttpMethod.GET, new StringBuilder(""));
		assertTrue(or.isCodeOk());
		or = new OrchestratorResponse(299, HttpMethod.GET, new StringBuilder(""));
		assertTrue(or.isCodeOk());
		or = new OrchestratorResponse(300, HttpMethod.GET, new StringBuilder(""));
		assertTrue(!or.isCodeOk());
	}
	
}
