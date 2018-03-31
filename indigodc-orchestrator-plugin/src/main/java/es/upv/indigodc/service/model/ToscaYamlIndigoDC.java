package es.upv.indigodc.service.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ToscaYamlIndigoDC {
	
	protected String indigoDCTopologyYaml;
	
	public ToscaYamlIndigoDC(String a4cTopologyYaml) {
		Pattern pattern = Pattern.compile("(imports:)(\\s)*(\n)(\\s)*(-)");
	    Matcher matcher = pattern.matcher(a4cTopologyYaml);
	    int numSpaces = 0;
	    if (matcher.find()) {
	    	String sub = a4cTopologyYaml.substring(matcher.start(), matcher.end());
	    	//log.info();
	    	int pos = sub.lastIndexOf(' ');
	    	while (pos>=0 && sub.charAt(pos) == ' ') {
	    		++numSpaces;
	    		--pos;
	    	}
	    	log.info("Tosca indent size: " + numSpaces);
	    } else {
	    	log.error("Can't find imports");
	    }
		indigoDCTopologyYaml =  a4cTopologyYaml
				.replaceFirst("(tosca_definitions_version:)(\\s*)(.+)(\n)", "tosca_definitions_version: tosca_simple_yaml_1_0\n")
				.replaceFirst("(metadata:)(?s:.)+(\ndescription:)", "\ndescription:")
				.replaceFirst("(imports:)(\\s)*(\n)(?s:.)+(\ntopology_template:)", 
						String.format("imports:\n%" + numSpaces + "s- indigo_custom_types: https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml\n\ntopology_template:", ""))
				.replaceFirst("(\n)(\\s)+(workflows:)(\\s)*(\n)(?s:.)+", "")
				.replaceAll("\"", "\\\\\"")
				.replaceAll("\n", "\\\\n");
	}
	
	public String getIndigoDCTopologyYaml() { return indigoDCTopologyYaml;}

}
