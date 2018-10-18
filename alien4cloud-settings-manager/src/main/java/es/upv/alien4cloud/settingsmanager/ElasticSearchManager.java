package es.upv.alien4cloud.settingsmanager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchManager {
	
	public static final String CONFIG_TEMPLATE_PATH = "config/elasticsearch.yml";

	  private ObjectNode root;
	  private ObjectMapper mapper;
	  
	public ElasticSearchManager() throws JsonProcessingException, IOException {
	    InputStream inConfigTemplate = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_TEMPLATE_PATH);

	    mapper = new ObjectMapper(
	            new YAMLFactory()
	                .disable(Feature.WRITE_DOC_START_MARKER)
	                .disable(Feature.SPLIT_LINES)
	                .disable(Feature.CANONICAL_OUTPUT));
	    root = (ObjectNode) mapper.readTree(inConfigTemplate);
		
	}
	
	public void setPath(String fullPath) throws ValueNotFoundException {
		ObjectNode path = (ObjectNode) root.findValue("path");
	    if (path != null) {	
	    	if (!fullPath.endsWith("/"))      
	    		fullPath = fullPath + "/";
	    	setPathChild(path,"data", fullPath);
	    	setPathChild(path,"work", fullPath);
	    	setPathChild(path,"logs", fullPath);
	    } else
	        throw new ValueNotFoundException(
	                String.format("Can't find the path node in the %s template", CONFIG_TEMPLATE_PATH));
	    	
	}
	
	private void setPathChild(ObjectNode parent, String childKey, String fullPath) throws ValueNotFoundException {
		TextNode node = (TextNode) parent.findValue(childKey);
	      if (node != null) {
	    	  parent.put(childKey, fullPath + node.textValue());
	  		//node.(childKey, fullPath + node.asText());
	      } else
		        throw new ValueNotFoundException(
		                String.format("Can't find the %s node in the %s template", childKey, CONFIG_TEMPLATE_PATH));
	}
	
	  public void writeConfig(String outFileFullPath) 
		      throws JsonGenerationException, JsonMappingException, IOException {
		    ObjectWriter ow = mapper.writer();
		    ow.writeValue(new File(outFileFullPath), this.root);
		  }

}
