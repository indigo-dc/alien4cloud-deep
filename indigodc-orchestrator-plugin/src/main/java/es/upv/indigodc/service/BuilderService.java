package es.upv.indigodc.service;

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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

  public static final String TOSCA_DEFINITIONS_VERSION = "tosca_simple_yaml_1_0";

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

  @Data
  public static class Template {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Metadata {
      protected String template_name;
      protected String template_version;
      protected String template_author;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopologyTemplate {

      @Data
      @AllArgsConstructor
      public static class NodeTemplateDef {

        protected String type;
        protected Map<String, Capability> capabilities;
      }

      protected Map<String, PropertyDefinition> inputs;
      protected Map<String, NodeTemplate> node_templates;
      protected Map<String, OutputDef> outputs;
    }

    @Data
    @AllArgsConstructor
    public static class OutputDef {

      protected String value;
    }

    protected String tosca_definitions_version;
    protected Metadata metadata;
    protected String description;
    protected List<Map<String, String>> imports;
    protected TopologyTemplate topology_template;

    public Template() {
      tosca_definitions_version = TOSCA_DEFINITIONS_VERSION;
      imports = new ArrayList<>(); // new HashMap<>();
      Map<String, String> imp = new HashMap<>();
      imp.put(
          "indigo_custom_types",
          "https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml");
      imports.add(imp);
    }
  }

  public static String getIndigoDCTopologyYaml(String a4cTopologyYaml)
      throws JsonProcessingException, IOException {
    ObjectMapper mapper =
        new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));

    ObjectNode root = (ObjectNode) mapper.readTree(a4cTopologyYaml);
    root.remove("tosca_definitions_version");
    root.put("tosca_definitions_version", "tosca_simple_yaml_1_0");
    ((ObjectNode) root.get("topology_template")).remove("workflows");
    root.remove("metadata");
    root.remove("imports");
    ObjectNode imports = mapper.createObjectNode();
    imports.put(
        "indigo_custom_types",
        "https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml");
    root.putArray("imports").add(imports);
    ObjectNode tmp = (ObjectNode) root.get("topology_template").get("node_templates");
    Iterator<JsonNode> it = tmp.elements();
    while (it.hasNext()) {
      ObjectNode nodeTemplate = (ObjectNode) it.next();
      // Eliminate metadata info
      nodeTemplate.remove("metadata");

      //
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
        .replaceAll("\n", "\\\\n");
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
            "(\n){0,1}(\\s)*(-){0,1}(\\s)*(\\\"){1}(\\s)*(\\{){1}(\\s)*(get_attribute:){1}(\\s)*(\\[){1}(.?)+(\\]){1}(\\s)*(\\}){1}(\\s)*(\\\"){1}");

    Matcher m = p.matcher(a4cTopologyYaml);
    while (m.find()) {
      m.appendReplacement(
          newa4cTopologyYaml, m.group().replaceAll("(\\\"){1}|(\\n){0,1}(\\\\s)*(-){0,1}", ""));
    }
    m.appendTail(newa4cTopologyYaml);

    return newa4cTopologyYaml.toString();
  }

  public String buildApp(PaaSTopologyDeploymentContext deploymentContext, int numCPUs)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Deployment d = new Deployment();
    d.setParameters(getParameters(deploymentContext));
    d.setCallback("http://localhost:8080/callback");
    String yamlIndigoDC = buildTemplate(deploymentContext);
    d.setTemplate(yamlIndigoDC);

    String yamlApp = mapper.writeValueAsString(d).replace("\\\\n", "\\n");
    // .replace("\\\\\\", "\\");//.replaceAll("\"", "\\\"");
    // String.format("{\"template\":\"%s\",\"parameters\":{\"cpus\":1},\"callback\":\"http://localhost:8080/callback\"}", yamlIndigoDC);
    // log.info("Yaml to be sent to the orchestrator: \n" + yamlApp);
    return yamlApp;
  }

  public String buildTemplate(PaaSTopologyDeploymentContext deploymentContext)
      throws JsonProcessingException, IOException {
    editionContextManager.init(deploymentContext.getDeploymentTopology().getInitialTopologyId());
    String a4cTopologyYaml =
        exportService.getYaml(EditionContextManager.getCsar(), EditionContextManager.getTopology());
    //

    log.info("+++++++++++++" + a4cTopologyYaml);

    return getIndigoDCTopologyYaml(a4cTopologyYaml); // yaml.dump(t);
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
