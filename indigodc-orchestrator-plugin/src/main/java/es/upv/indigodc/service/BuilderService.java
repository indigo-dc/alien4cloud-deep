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
import org.alien4cloud.tosca.model.Csar;
import org.alien4cloud.tosca.model.definitions.AbstractPropertyValue;
import org.alien4cloud.tosca.model.definitions.PropertyDefinition;
import org.alien4cloud.tosca.model.definitions.PropertyValue;
import org.alien4cloud.tosca.model.definitions.ScalarPropertyValue;
import org.alien4cloud.tosca.model.templates.Capability;
import org.alien4cloud.tosca.model.templates.NodeTemplate;
import org.alien4cloud.tosca.model.templates.Topology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import alien4cloud.paas.model.PaaSNodeTemplate;
import alien4cloud.paas.model.PaaSTopology;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Service("builder-service")
@Slf4j
public class BuilderService {

  @Inject private ArchiveExportService exportService;

  @Inject private EditionContextManager editionContextManager;
  
  @Autowired
  @Qualifier("cloud-configuration-manager")
  private CloudConfigurationManager cloudConfigurationHolder;

  public static final String TOSCA_DEFINITIONS_VERSION = "tosca_simple_yaml_1_0";
  public static final String TOSCA_METHODS[] = {"get_input", "concat"};

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

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Deployment {

    private String template;
    private Map<String, Object> parameters;
    private String callback;
  }
  
  public static String encodeTOSCAMethods(String a4cTopologyYaml) {
    StringBuffer patternsMethods = new StringBuffer();
    for (int idx=0; idx<TOSCA_METHODS.length; ++idx)
      patternsMethods.append(TOSCA_METHODS[idx]).append("|");
    patternsMethods.deleteCharAt(patternsMethods.length() - 1);

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

  public static String toscaMethodsStrToMethod(String a4cTopologyYaml) {
    StringBuffer newa4cTopologyYaml = new StringBuffer();

    Pattern p =
        Pattern.compile(
            "(\n){0,1}(\\s)*(-){0,1}(\\s)*(\\\"){1}(\\s)*(\\{){1}(\\s)*[a-zA-Z_\\-0-9]+(\\s)*(:){1}(\\s)*(.?)+(\\s)*(\\}){1}(\\s)*(\\\"){1}");

    Matcher m = p.matcher(a4cTopologyYaml);
    while (m.find()) {
      StringBuilder group = new StringBuilder(m.group());
      int pos = group.lastIndexOf("\"");
      if (pos >= 0)
        group.replace(pos, pos + 1, "");
      pos = group.indexOf("\"");
      if (pos >= 0)
        group.replace(pos, pos + 1, "");
      m.appendReplacement(
          newa4cTopologyYaml, group.toString().replaceAll("(\\n){0,1}(\\s)*(-){1}", ""));
    }
    m.appendTail(newa4cTopologyYaml);

    return newa4cTopologyYaml.toString();
  }
  
  public static String deploymentToStr(Deployment d) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(d).replace("\n", "\\n");
  }

  public String buildApp(PaaSTopologyDeploymentContext deploymentContext, String importIndigoCustomTypes)
      throws IOException {
    Deployment d = new Deployment();
    d.setParameters(getParameters(deploymentContext));
    d.setCallback("http://localhost:8080/callback");
    String yamlIndigoDC = buildTemplate(deploymentContext, importIndigoCustomTypes);
    d.setTemplate(yamlIndigoDC);

    // .replace("\\\\\\", "\\");//.replaceAll("\"", "\\\"");
    // String.format("{\"template\":\"%s\",\"parameters\":{\"cpus\":1},\"callback\":\"http://localhost:8080/callback\"}", yamlIndigoDC);
    // log.info("Yaml to be sent to the orchestrator: \n" + yamlApp);
    return deploymentToStr(d);
  }

  public String buildTemplate(PaaSTopologyDeploymentContext deploymentContext, String importIndigoCustomTypes)
      throws JsonProcessingException, IOException {
    editionContextManager.init(deploymentContext.getDeploymentTopology().getInitialTopologyId());
    String a4cTopologyYaml =
        exportService.getYaml(EditionContextManager.getCsar(), EditionContextManager.getTopology());
    //

    //log.info("+++++++++++++" + a4cTopologyYaml);

    return getIndigoDCTopologyYaml(a4cTopologyYaml, importIndigoCustomTypes); // yaml.dump(t);
  }

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

  protected String buildOutputValue(String nodeName, String propertyName) {
    return String.format("{ get_attribute: [ %s, %s ] }", nodeName, propertyName);
  }

  protected String buildOutputKey(String nodeName, String propertyName) {
    String nodeName2 = nodeName.replaceAll(" ", "_");
    String propertyName2 = propertyName.replaceAll(" ", "_");
    return nodeName2 + "_" + propertyName2;
  }

  private static class MyRepresenter extends Representer {
    //    @Override
    //    protected Set<Property> getProperties(Class<? extends Object> type) throws
    // IntrospectionException {
    //        Set<Property> set = super.getProperties(type);
    //        Set<Property> filtered = new LinkedHashSet<Property>();
    //        if (type.equals(ScalarPropertyValue.class)) {
    //            // filter properties
    //            for (Property prop : set) {
    //                String name = prop.getName();
    //                if (!name.equals("value")) {
    //                    filtered.add(prop);
    //                } else {
    //                  prop.set(, value);
    //                }
    //            }
    //        } else {
    //          for (Property prop : set) {
    //            String name = prop.getName();
    //                filtered.add(prop);
    //          }
    //        }
    //        return filtered;
    //    }
  }
  //
  //  private static class MyPropertyUtils extends PropertyUtils {
  //    @Override
  //    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess)
  //        throws IntrospectionException {
  //    Set<Property> properties = new LinkedHashSet<Property>();
  //    Collection<Property> props = getPropertiesMap(type, bAccess).values();
  //    for (Property property : props) {
  //        if (property.isReadable() && property.isWritable()) {
  //            properties.add(property);
  //        }
  //    }
  //    return properties;
  // }
  //  }

}
