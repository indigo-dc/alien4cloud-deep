import yaml
import sys
import collections

_mapping_tag = yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG

def dict_representer(dumper, data):
  return dumper.represent_dict(data.items())

def dict_constructor(loader, node):
  return collections.OrderedDict(loader.construct_pairs(node))

def get_artifact_name(origName):
  return "ansible-role-" + origName.split(".")[1] + "/archive/master.zip"

yaml.add_representer(collections.OrderedDict, dict_representer)
yaml.add_constructor(_mapping_tag, dict_constructor)

REPO_INDIGODC = "GitHub-IndigoDC"
REPO_GRYCAP = "GitHub-GRyCAP"

tosca = yaml.load(sys.stdin)
tosca["tosca_definitions_version"] = "alien_dsl_2_0_0"
tosca["repositories"] = \
  {REPO_INDIGODC: {"url": "https://github.com/indigo-dc/",
                       "type": "http"},
   REPO_GRYCAP: {"url": "https://github.com/grycap/",
                     "type": "http"} 
  }

nodeTypes = collections.OrderedDict()
for i, (key, value) in enumerate(tosca["node_types"].items()):
  if "artifacts" in value:
    for artifact in value["artifacts"]:
      artifactVal = next(iter(artifact.values()))
      if "indigo-dc" in artifactVal["file"]:
        artifactVal["file"] = get_artifact_name(artifactVal["file"])
        artifactVal["repository"] = REPO_INDIGODC
      elif "grycap" in artifactVal["file"]:
        artifactVal["file"] = get_artifact_name(artifactVal["file"])
        artifactVal["repository"] = REPO_GRYCAP
      else:
        raise ValueError("Repository containing roles " + artifactVal["file"] + " not handled")

tosca["metadata"] = {"template_name": "indigo-types",
	"template_version": "1.0.0",
	"template_author": "indigo"}
tosca["imports"] = ["tosca-normative-types:1.0.0-ALIEN20"]
print(yaml.dump(tosca, default_flow_style=False))
