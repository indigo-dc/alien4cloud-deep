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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.alien4cloud.tosca.editor.EditionContextManager;
import org.alien4cloud.tosca.exporter.ArchiveExportService;
import org.alien4cloud.tosca.model.Csar;
import org.alien4cloud.tosca.model.definitions.AbstractPropertyValue;
import org.alien4cloud.tosca.model.definitions.PropertyDefinition;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import alien4cloud.paas.model.PaaSNodeTemplate;
import alien4cloud.paas.model.PaaSTopology;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.service.model.ToscaYamlIndigoDC;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service("builder-service")
@Slf4j
public class BuilderService {
  

  @Inject
  private ArchiveExportService exportService;

  @Inject
  private EditionContextManager editionContextManager;
  
  public static final String TOSCA_DEFINITIONS_VERSION = "tosca_simple_yaml_1_0";
  
  private static final DumperOptions dumperOptions;
  
  private static final String TEST_YAML = 
      "\ntosca_definitions_version: tosca_simple_yaml_1_0\n\nimports:\n  - indigo_custom_types: https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml\n\ndescription: Example\n\ntopology_template:\n  node_templates:\n    Compute:\n      type: tosca.nodes.Compute\n      capabilities:\n        scalable:\n          properties:\n            min_instances: 1\n            max_instances: 1\n            default_instances: 1\n          endpoint:\n            properties:\n              secure: true\n              protocol: tcp\n              network_name: PRIVATE\n              initiator: source\n";
  
  private static final String TEST_YAML_JUPYTER =
      "tosca_definitions_version: tosca_simple_yaml_1_0\n\nimports:\n  - indigo_custom_types: https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml\n\ndescription: >\n  TOSCA test for launching a Kubernetes Virtual Cluster.\ntopology_template:\n  inputs:\n    wn_num:\n      type: integer\n      description: Number of WNs in the cluster\n      default: 1\n      required: yes\n    fe_cpus:\n      type: integer\n      description: Numer of CPUs for the front-end node\n      default: 2\n      required: yes\n    fe_mem:\n      type: scalar-unit.size\n      description: Amount of Memory for the front-end node\n      default: 2 GB\n      required: yes\n    wn_cpus:\n      type: integer\n      description: Numer of CPUs for the WNs\n      default: 1\n      required: yes\n    wn_mem:\n      type: scalar-unit.size\n      description: Amount of Memory for the WNs\n      default: 2 GB\n      required: yes\n\n    admin_username:\n      type: string\n      description: Username of the admin user\n      default: kubeuser\n    admin_token:\n      type: string\n      description: Access Token for the admin user\n      default: not_very_secret_token\n\n  node_templates:\n\n    jupyterhub:\n      type: tosca.nodes.indigo.JupyterHub\n      properties:\n        spawner: kubernetes\n      requirements:\n        - host: lrms_server\n        - dependency: lrms_front_end\n\n    lrms_front_end:\n      type: tosca.nodes.indigo.LRMS.FrontEnd.Kubernetes\n      properties:\n        admin_username:  { get_input: admin_username }\n        admin_token: { get_input: admin_token }\n      requirements:\n        - host: lrms_server\n\n    lrms_server:\n      type: tosca.nodes.indigo.Compute\n      capabilities:\n        endpoint:\n          properties:\n            dns_name: kubeserver\n            network_name: PUBLIC\n            ports:\n              https_port:\n                protocol: tcp\n                source: 6443\n        host:\n          properties:\n            num_cpus: { get_input: fe_cpus }\n            mem_size: { get_input: fe_mem }\n        os:\n          properties:\n            image: linux-ubuntu-16.04-vmi\n            #type: linux\n            #distribution: ubuntu\n            #version: 16.04\n\n    wn_node:\n      type: tosca.nodes.indigo.LRMS.WorkerNode.Kubernetes\n      properties:\n        front_end_ip: { get_attribute: [ lrms_server, private_address, 0 ] }\n      requirements:\n        - host: lrms_wn\n\n    lrms_wn:\n      type: tosca.nodes.indigo.Compute\n      capabilities:\n        scalable:\n          properties:\n            count: { get_input: wn_num }\n        host:\n          properties:\n            num_cpus: { get_input: wn_cpus }\n            mem_size: { get_input: wn_mem }\n        os:\n          properties:\n            image: linux-ubuntu-16.04-vmi\n            #type: linux\n            #distribution: ubuntu\n            #version: 16.04\n\n  outputs:\n    jupyterhub_url:\n      value: { concat: [ 'http://', get_attribute: [ lrms_server, public_address, 0 ], ':8000' ] }\n    cluster_ip:\n      value: { get_attribute: [ lrms_server, public_address, 0 ] }\n    cluster_creds:\n      value: { get_attribute: [ lrms_server, endpoint, credential, 0 ] }";
  
  static {
    dumperOptions = new DumperOptions();
    dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    dumperOptions.setIndent(4);
    dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    //dumperOptions.setLineBreak(DumperOptions.LineBreak.valueOf("\\n"));
    dumperOptions.setLineBreak(DumperOptions.LineBreak.UNIX);
    dumperOptions.setPrettyFlow(true);
    dumperOptions.setCanonical(false);
  }
  
 
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Deployment {
    
    @Data @AllArgsConstructor
    public static class Parameters {
      private int cpus;
    }
    
    private String template;
    private Parameters parameters;
    private String callback;
    
  }
  
  @Data
  public static class Template {
    
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Metadata {
      protected String template_name;
      protected String template_version;
      protected String template_author;
    }
    
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class TopologyTemplate {
      
      @Data @AllArgsConstructor
      public static class NodeTemplateDef {
        
        protected String type;
        protected Map<String, Capability> capabilities;
        
      }
      
      protected Map<String, PropertyDefinition> inputs;
      protected Map<String, NodeTemplate> node_templates;
      protected Map<String, OutputDef> outputs;
      
    }
    
    @Data @AllArgsConstructor
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
      imports = new ArrayList<>();//new HashMap<>();
      Map<String, String> imp = new HashMap<>();
      imp.put("indigo_custom_types", "https://raw.githubusercontent.com/indigo-dc/tosca-types/master/custom_types.yaml");
      imports.add(imp);
    }
    
  }

  public String buildApp(PaaSTopologyDeploymentContext deploymentContext, int numCPUs) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Deployment d = new Deployment();
    d.setParameters(new Deployment.Parameters(numCPUs));
    d.setCallback("http://localhost:8080/callback");
    String yamlIndigoDC = TEST_YAML_JUPYTER;//buildTemplate(deploymentContext);
    d.setTemplate(yamlIndigoDC);
    
    String yamlApp = mapper.writeValueAsString(d);//.replace("\\\\n", "\\n")
    		//.replace("\\\\\\", "\\");//.replaceAll("\"", "\\\"");
    		//String.format("{\"template\":\"%s\",\"parameters\":{\"cpus\":1},\"callback\":\"http://localhost:8080/callback\"}", yamlIndigoDC);
    log.info("Yaml to be sent to the orchestrator: \n" + yamlApp);
    return yamlApp;
  }
  
  public String buildTemplate(PaaSTopologyDeploymentContext deploymentContext) {
//    Template t = new Template();
//    t.setDescription("A4C won't allow saving a description from the UI");
//    t.setMetadata(new Template.Metadata(deploymentContext.getDeployment().getSourceId(), 
//        deploymentContext.getDeployment().getVersionId(), "admin"));
//    //t.setTopology_template(new Template.TopologyTemplate(deploymentContext.getPaaSTopology().getAllNodes()));
//    //deploymentContext.getPaaSTopology().get
//    //Representer r = new Representer();
//    //r.setPropertyUtils(new MyPropertyUtils());
//    Yaml yaml = new Yaml(new MyRepresenter(), dumperOptions);
//    
//    //log.info("Deployment is: \n" + yaml.dumpAs(deploymentContext.getDeploymentTopology(), Tag.MAP, DumperOptions.FlowStyle.BLOCK));
//    
//    
//    
//    //deploymentContext.getPaaSTopology().getAllNodes();
//    //PaaSTopology paaSTopology = this.clonePaaSTopology(deploymentContext.getPaaSTopology());
//    Map<String, PaaSNodeTemplate> an = deploymentContext.getPaaSTopology().getAllNodes();
//    //log.info("Num nodes topo: " + an.size());
//    
//
//   // log.info("Full dump topo: " + yaml.dump(deploymentContext.getPaaSTopology()));
//    //log.info("Full dump deployment: " + yaml.dump(deploymentContext.getDeployment()));
//    
//    //Map<String, Template.TopologyTemplate.NodeTemplateDef> nodes = new HashMap<>();
//    Map<String, NodeTemplate> nodes = new HashMap<>();
    editionContextManager.init(deploymentContext.getDeploymentTopology().getInitialTopologyId());
    String a4cTopologyYaml = exportService.getYaml(EditionContextManager.getCsar(), EditionContextManager.getTopology());
    
    ToscaYamlIndigoDC toscaYamlIndigoDC = new ToscaYamlIndigoDC(a4cTopologyYaml);
    
    //log.info("Editor yaml: " + a4cTopologyYaml);
    
    
//    for (Map.Entry<String, PaaSNodeTemplate> e: an.entrySet()) {
//      //log.info(e.getKey() + " - " + e.getValue().getId() + " - " + e.toString());
//      //log.info("Template: " + yaml.dump(e.getValue().getTemplate()));
//      //log.info("Full dump: " + yaml.dump(e.getValue()));
//      Map<String, Capability> capabilities = e.getValue().getTemplate().getCapabilities();
////      for (Map.Entry<String, Capability> capability: capabilities.entrySet()) {
////        Map<String, AbstractPropertyValue> props = capability.getValue().getProperties();
////        for (Map.Entry<String, AbstractPropertyValue> prop: props.entrySet()) {
////          prop.getValue().
////        }
////      }
//      //nodes.put(e.getKey(), new Template.TopologyTemplate.NodeTemplateDef(e.getValue().getTemplate().getType(), capabilities));
//      nodes.put(e.getKey(), e.getValue().getTemplate());
//    }
//    deploymentContext.getDeploymentTopology().getOutputAttributes();
//    
//    
//    Map<String, Template.OutputDef> outputs = new HashMap<>();
//    
//    for (Entry<String, Set<String>> e: deploymentContext.getDeploymentTopology().getOutputAttributes().entrySet()) {
//      for (String pn: e.getValue()) {
//        //log.info(buildOutputValue(e.getKey(), pn));
//        outputs.put(buildOutputKey(e.getKey(), pn), new Template.OutputDef(buildOutputValue(e.getKey(), pn)));
//      }
//    }
//
//    t.setTopology_template(new Template.TopologyTemplate(deploymentContext.getDeploymentTopology().getInputs(), nodes, outputs));
//    
////    log.info(deploymentContext.getPaaSTopology().toString());
//    String yamlRepresentation = yaml.dumpAs(t, Tag.MAP, 
//        DumperOptions.FlowStyle.BLOCK);
    //log.info("Tosca Template to be sent: " + yamlRepresentation);
    return toscaYamlIndigoDC.getIndigoDCTopologyYaml();//yaml.dump(t);
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
//    protected Set<Property> getProperties(Class<? extends Object> type) throws IntrospectionException {
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
//}
//  }

}
