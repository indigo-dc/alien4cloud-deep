package es.upv.indigodc.service.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;


/**
 * Holds the response received after a call to the Orchestrator
 * @author asalic
 *
 */
@Getter
public class OrchestratorResponse {
	
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ObjectMapper objectMapper;
	

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private JsonNode rootResponse;
	
	public OrchestratorResponse(int code, HttpMethod callMethod, StringBuilder response) throws JsonProcessingException, IOException {
		this.code = code;
		this.callMethod = callMethod;
		this.response = response;
		objectMapper = new ObjectMapper();
		objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
		if (response.length() > 0)
			rootResponse = objectMapper.readTree(response.toString());
		else
			rootResponse = objectMapper.createObjectNode();
	}

  /**
   * The return code of the call, e.g. 200 for OK
   */
  private int code;
  /**
   * One of the calls methods for HTTP, e.g. GET, POST etc. used to get this response
   */
  private HttpMethod callMethod;
  /**
   * The response itself with all its fields
   */
  private StringBuilder response;
  
  public boolean isCodeOK() {
    return code >= 200 && code <= 299;
  }
  
  public Map<String, String> getOutputs() throws JsonProcessingException, IOException {
	  Map<String, String> result = new HashMap<>();
	  List<JsonNode> outputs = getNodesByKey("outputs");
	  if (outputs != null && outputs.size() > 0) {
		  return objectMapper.convertValue(outputs.get(0), new TypeReference<Map<String, String>>(){});
	  } 
	  return result;
  }
  
  protected List<JsonNode> getNodesByKey(String key) throws JsonProcessingException, IOException {
    return rootResponse.findValues(key);
  }
  
}
