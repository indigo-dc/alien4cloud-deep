import yaml
import sys
import collections
import os
from pathlib import Path

if len(sys.argv) != 4:
    raise Exception("Required parameters:\n \
    (1) the path to the folder the the dummy artifact files are created\n \
    followed by \n \
    (2) this TOSCA template version\n \
    followed by\n \
    (3) the TOSCA normative types name and version imported by these custom types, separated by colon")

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
pathDummyArtifacts = sys.argv[1]#os.path.join(os.path.sep, sys.argv[1], "dummy_artifacts")
toscaTemplateVer = sys.argv[2]#os.path.join(os.path.sep, sys.argv[1], "dummy_artifacts")
importNormativeNameVer = sys.argv[3]

tosca = yaml.load(sys.stdin)
#tosca["tosca_definitions_version"] = "alien_dsl_2_0_0"
# tosca["repositories"] = \
#  {REPO_INDIGODC: {"url": "https://github.com/indigo-dc/",
#                       "type": "a4c_ignore"},
#   REPO_GRYCAP: {"url": "https://github.com/grycap/",
#                     "type": "a4c_ignore"}
#  }

if not os.path.exists(pathDummyArtifacts):
    os.makedirs(pathDummyArtifacts)

for i, (key, value) in enumerate(tosca["node_types"].items()):
  if "artifacts" in value:
    artifacts = value["artifacts"]
    #artifactsArray = []
    for artifactName, artifactVal in artifacts.items():
      #artifactsArray.append({artifactName: artifactVal});
      #print(artifactName, file=sys.stderr,  flush=True)
      #artifactVal = next(iter(artifact.values()))
      if "indigo-dc" in artifactVal["file"]:
        Path(os.path.join(os.path.sep, pathDummyArtifacts, artifactVal["file"])).touch()
        #artifactVal["file"] = get_artifact_name(artifactVal["file"], "https://github.com/indigo-dc/")
        #artifactVal["repository"] = REPO_INDIGODC
      elif "grycap" in artifactVal["file"]:
        Path(os.path.join(os.path.sep, pathDummyArtifacts, artifactVal["file"])).touch()
        #artifactVal["file"] = get_artifact_name(artifactVal["file"], "https://github.com/grycap/")
        #artifactVal["repository"] = REPO_GRYCAP
      else:
        raise ValueError("Repository containing roles " + artifactVal["file"] + " not handled")
    #value["artifacts"] = artifactsArray

tosca["metadata"] = {"template_name": "indigo_custom_types",
	"template_version": toscaTemplateVer,
	"template_author": "Indigo"}
tosca["description"] = "Contains the types definition as currently supported by the IndigoDC Orchestrator"
importNormativeNameVerLst = importNormativeNameVer.split(":")
tosca["imports"] = [{importNormativeNameVerLst[0].strip(" "):
  importNormativeNameVerLst[1].strip(" ")}]

#print(importNormativeNameVer, file=sys.stderr,  flush=True)
print(yaml.dump(tosca, default_flow_style=False))
