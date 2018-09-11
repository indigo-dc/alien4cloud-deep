package es.upv.indigodc.service;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.alien4cloud.tosca.editor.EditionContextManager;
import org.alien4cloud.tosca.exporter.ArchiveExportService;
import org.alien4cloud.tosca.model.definitions.AbstractPropertyValue;
import org.alien4cloud.tosca.model.definitions.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the creation of the generation of the tosca topology in a format that is accepted by the Orchestrator.
 * It uses the TOSCA document as found in the TOSCA topology text editor.
 * @author asalic
 *
 */
@Service("builder-service")
@Slf4j
public class BuilderService {

  /**
   * Gets the TOSCA topology in text format from the A4C topology editor
   */
  @Inject private ArchiveExportService exportService;
  /**
   * Initializes the the manager of the TOSCA editor for a certain deployment
   */
  @Inject private EditionContextManager editionContextManager;
  /**
   * Retrieves the configuration for the plugin
   */
  @Autowired @Qualifier("cloud-configuration-manager")
  private CloudConfigurationManager cloudConfigurationHolder;
  /**
   * The Orchestrator's accepted TOSCA YAML definition declaration
   */
  public static final String TOSCA_DEFINITIONS_VERSION = "tosca_simple_yaml_1_0";
  /**
   * The options used by the TOSCA YAML writer to generate the text representation
   * of the topology that is sent to the Orchestrator
   */
  private static final DumperOptions dumperOptions;

  static {
    dumperOptions = new DumperOptions();
    dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    dumperOptions.setIndent(4);
    dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    // dumperOptions.setLineBreak(DumperOptions.LineBreak.valueOf("\\n"));
    dumperOptions.setLineBreak(DumperOptions.LineBreak.UNIX);
    dumperOptions.setPrettyFlow(true);
    dumperOptions.setCanonical(false);
  }

  /**
   * Describes the payload sent to the Orchestrator containing the TOSCA topology
   * @author asalic
   *
   */
  @Data @AllArgsConstructor @NoArgsConstructor
  public static class Deployment {

    /**
     * The textual representation of the TOSCA topology that will be sent to the Orchestrator
     */
    private String template;
    /**
     * The inputs from the TOSCA topology
     */
    private Map<String, Object> parameters;
    /**
     * The callback function used by the Orchestrator
     */
    private String callback;
    
    /**
     * Generates the text representation of the deployment of a topology as requested by the Orchestrator
     * @return The payload that will be sent to the Orchestrator
     * @throws JsonProcessingException
     */
    public String toOrchestratorString() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this).replace("\n", "\\n");
    }
  }
  
  /**
   * Takes the A4C textual representation of the TOSCA topology and encodes (comments) the TOSCA methods as strings
   * in order to be processed by YAML parser (which doesn't discern between a TOSCA method and a 
   * a value)
   * @param a4cTopologyYaml The A4C topology that can be seen in the A4C text TOSCA editor
   * @return The A4C topology with the TOSCA methods commented
   */
  public static String encodeTOSCAMethods(String a4cTopologyYaml) {
    StringBuffer newa4cTopologyYaml = new StringBuffer();
      Pattern p =
          Pattern.compile(
              "(:){1}(\\s)*(\\\"){0}(\\s)*(\\{){1}(.?)+(\\}){1}(\\s)*(\\\"){0}");
      Matcher m = p.matcher(a4cTopologyYaml);
      while (m.find()) {
        StringBuilder group = new StringBuilder(m.group());
        int pos = group.lastIndexOf("}");
        if (pos >= 0)
          group.replace(pos, pos + 1, "}\"");
        pos = group.indexOf("{");
        if (pos >= 0)
          group.replace(pos, pos + 1, "\"{");
        m.appendReplacement(
            newa4cTopologyYaml, group.toString());
      }
      m.appendTail(newa4cTopologyYaml);
      log.info("Topo with methods changed: " + newa4cTopologyYaml.toString());
    return newa4cTopologyYaml.toString();
  }

  /**
   * Converts the A4C topology to a modified version that the Orchestrator understands
   * @param a4cTopologyYaml The A4C topology that can be seen in the A4C text TOSCA editor 
   * @param importIndigoCustomTypes The path to the repository (file) containing the TOSCA
   * types used by the Orchestrator
   * @return The textual representation of the topology that will be sent to the Orchestrator
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static String getIndigoDCTopologyYaml(String a4cTopologyYaml, String importIndigoCustomTypes)
      throws JsonProcessingException, IOException {
    ObjectMapper mapper =
        new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)
            .disable(Feature.SPLIT_LINES)
            .disable(Feature.CANONICAL_OUTPUT));

    String a4cTopologyYamlIgnoreMethods = encodeTOSCAMethods(a4cTopologyYaml);
    ObjectNode root = (ObjectNode) mapper.readTree(a4cTopologyYamlIgnoreMethods);
    root.remove("tosca_definitions_version");
    root.put("tosca_definitions_version", TOSCA_DEFINITIONS_VERSION);
    ((ObjectNode) root.get("topology_template")).remove("workflows");
    root.remove("metadata");
    root.remove("imports");
    ObjectNode imports = mapper.createObjectNode();
    imports.put("indigo_custom_types", importIndigoCustomTypes);
    root.putArray("imports").add(imports);
    ObjectNode tmp = (ObjectNode) root.get("topology_template").get("node_templates");
    Iterator<JsonNode> it = tmp.elements();
    while (it.hasNext()) {
      ObjectNode nodeTemplate = (ObjectNode) it.next();
      // Eliminate metadata info
      nodeTemplate.remove("metadata");

      ObjectNode properties = rmNullProps((ObjectNode) nodeTemplate.get("properties"));
      if (properties == null)
        nodeTemplate.remove("properties");

      // Change requirements (no type and the name of the requirement is the type)
      ArrayNode requirements = ((ArrayNode) nodeTemplate.get("requirements"));
      if (requirements != null) {
        Iterator<JsonNode> itRequirements = requirements.elements();
        while (itRequirements.hasNext()) {
          ObjectNode requirement = (ObjectNode) itRequirements.next();
          Entry<String, JsonNode> firstField = requirement.fields().next();
          if (firstField.getValue().has("type_requirement")) {
            requirement.remove(firstField.getKey());
            requirement.set(
                firstField.getValue().get("type_requirement").asText(), firstField.getValue());
            ((ObjectNode) firstField.getValue()).remove("type_requirement");
          }
        }
      }
    }
    return toscaMethodsStrToMethod(mapper.writer().writeValueAsString(root))
        // .replaceAll("(\n){0,1}(\\s)*(-){0,1}(\\s)*(\\\"){1}(\\s)*(\\{){1}(\\s)*(get_attribute:){1}(\\s)*(\\[){1}(.?)+(\\]){1}(\\s)*(\\}){1}(\\s)*(\\\"){1}",
        // .replaceAll("(\\n){0,1}(\\s)*(-){0,1}(\\s)*(\\\"){1}(\\s)*(\\{){1}(\\s)*(get_attribute:){1}(\\s)*([){1}(.?)+(]){1}(\\s)*(\\}){1}(\\s)*(\\\"){1}",
        // "this")
        //.replaceAll("\n", "\\\\n")
        ;
  }

  /**
   * Cleanse the properties of a node_template by removing those nodes with null value. This method
   * modifies the properties that it receives.
   *
   * @param properties The array of properties of a node_template (can include inherited ones). Can
   *     be null, in which case nothing is done, null is returned
   * @return The input parameter after modification, null if input is null
   */
  public static ObjectNode rmNullProps(ObjectNode properties) {
    if (properties != null) {
      Iterator<Map.Entry<String,JsonNode>> itProperties = properties.fields();
      while (itProperties.hasNext()) {
        Map.Entry<String,JsonNode> property = itProperties.next();
        if (property.getValue().isNull()) itProperties.remove();
      }
      return properties.size() > 0 ? properties : null;
    } else
      return null;
  }

  /**
   * Executes the uncomment of the TOSCA methods that where commented using {@link #encodeTOSCAMethods}
   * @param topologyYaml The modified TOSCA topology that is accepted by the Orchestrator
   * @return The textual representation of the TOSCA topology with uncommented TOSCA methods
   */
  public static String toscaMethodsStrToMethod(String topologyYaml) {
    StringBuffer newTopologyYaml = new StringBuffer();

    Pattern p =
        Pattern.compile(
            "(\n){0,1}(\\s)*(-){0,1}(\\s)*(\\\"){1}(\\s)*(\\{){1}(\\s)*[a-zA-Z_\\-0-9]+(\\s)*(:){1}(\\s)*(.?)+(\\s)*(\\}){1}(\\s)*(\\\"){1}");

    Matcher m = p.matcher(topologyYaml);
    while (m.find()) {
      StringBuilder group = new StringBuilder(m.group());
      int pos = group.lastIndexOf("\"");
      if (pos >= 0)
        group.replace(pos, pos + 1, "");
      pos = group.indexOf("\"");
      if (pos >= 0)
        group.replace(pos, pos + 1, "");
      m.appendReplacement(
          newTopologyYaml, group.toString().replaceAll("(\\n){0,1}(\\s)*(-){1}", ""));
    }
    m.appendTail(newTopologyYaml);

    return newTopologyYaml.toString();
  }

  /**
   * Creates the payload that will be sent to the Orchestrator
   * @param deploymentContext The deployment object that represents the whole deployment process, including the TOSCA 
   * topology represented as A4C Java classes 
   * @param importIndigoCustomTypes The path to the repository (file) containing the TOSCA
   * types used by the Orchestrator
   * @return The textual representation of the topology that will be sent to the Orchestrator
   * @throws IOException
   */
  public String buildApp(PaaSTopologyDeploymentContext deploymentContext, String importIndigoCustomTypes)
      throws IOException {
    Deployment d = new Deployment();
    d.setParameters(getParameters(deploymentContext));
    d.setCallback("http://localhost:8080/callback");
    editionContextManager.init(deploymentContext.getDeploymentTopology().getInitialTopologyId());
    String a4cTopologyYaml =
        exportService.getYaml(EditionContextManager.getCsar(), EditionContextManager.getTopology());
    String yamlIndigoDC = getIndigoDCTopologyYaml(a4cTopologyYaml, importIndigoCustomTypes);
    d.setTemplate(yamlIndigoDC);
    return d.toOrchestratorString();
  }

  /**
   * Generates the parameters needed by the Orchestrator from the inputs found in the TOSCA topology generated by A4C
   * @param deploymentContext The deployment object that represents the whole deployment process, including the TOSCA 
   * topology represented as A4C Java classes 
   * @return A map having the keys as the input name and the values as the corresponding A4C objects describing the
   * TOSCA inputs
   */
  public Map<String, Object> getParameters(PaaSTopologyDeploymentContext deploymentContext) {
    final Map<String, Object> params = new HashMap<>();
    Map<String, AbstractPropertyValue> vals =
        deploymentContext.getDeploymentTopology().getAllInputProperties();
    for (Map.Entry<String, AbstractPropertyValue> v : vals.entrySet()) {
      if (v.getValue() instanceof PropertyValue)
        params.put(v.getKey(), ((PropertyValue<?>) v.getValue()).getValue());
      else log.warn(String.format("Can't add property %s", v.getKey()));
    }

    return params;
  }
}
