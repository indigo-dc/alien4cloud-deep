import yaml
import sys
import collections
import os
from pathlib import Path

if len(sys.argv) != 2:
    raise Exception("You must provide only one parameter, that is the root directory where the dummys are created, should be tha same with the custom_types")

_mapping_tag = yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG

def dict_representer(dumper, data):
  return dumper.represent_dict(data.items())

def dict_constructor(loader, node):
  return collections.OrderedDict(loader.construct_pairs(node))

def get_artifact_name(origName, serverPath):
  return serverPath + "ansible-role-" + origName.split(".")[1] + "/archive/master.zip"

yaml.add_representer(collections.OrderedDict, dict_representer)
yaml.add_constructor(_mapping_tag, dict_constructor)

REPO_INDIGODC = "GitHub-IndigoDC"
REPO_GRYCAP = "GitHub-GRyCAP"
PATH_DUMMY_ARTIFACTS = sys.argv[1]#os.path.join(os.path.sep, sys.argv[1], "dummy_artifacts")

tosca = yaml.load(sys.stdin)
tosca["tosca_definitions_version"] = "alien_dsl_2_0_0"
# tosca["repositories"] = \
#  {REPO_INDIGODC: {"url": "https://github.com/indigo-dc/",
#                       "type": "a4c_ignore"},
#   REPO_GRYCAP: {"url": "https://github.com/grycap/",
#                     "type": "a4c_ignore"}
#  }

if not os.path.exists(PATH_DUMMY_ARTIFACTS):
    os.makedirs(PATH_DUMMY_ARTIFACTS)

for i, (key, value) in enumerate(tosca["node_types"].items()):
 if "artifacts" in value:
   for artifact in value["artifacts"]:
     artifactVal = next(iter(artifact.values()))
     if "indigo-dc" in artifactVal["file"]:
       Path(os.path.join(os.path.sep, PATH_DUMMY_ARTIFACTS, artifactVal["file"])).touch()
       #artifactVal["file"] = get_artifact_name(artifactVal["file"], "https://github.com/indigo-dc/")
       #artifactVal["repository"] = REPO_INDIGODC
     elif "grycap" in artifactVal["file"]:
       Path(os.path.join(os.path.sep, PATH_DUMMY_ARTIFACTS, artifactVal["file"])).touch()
       #artifactVal["file"] = get_artifact_name(artifactVal["file"], "https://github.com/grycap/")
       #artifactVal["repository"] = REPO_GRYCAP
     else:
       raise ValueError("Repository containing roles " + artifactVal["file"] + " not handled")

tosca["metadata"] = {"template_name": "indigo-types",
	"template_version": "1.0.0",
	"template_author": "indigo"}
tosca["imports"] = ["tosca-normative-types:1.0.0-ALIEN20"]
print(yaml.dump(tosca, default_flow_style=False))
