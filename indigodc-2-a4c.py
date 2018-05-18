import yaml
import sys
import collections

_mapping_tag = yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG

def dict_representer(dumper, data):
    return dumper.represent_dict(data.items())

def dict_constructor(loader, node):
    return collections.OrderedDict(loader.construct_pairs(node))

yaml.add_representer(collections.OrderedDict, dict_representer)
yaml.add_constructor(_mapping_tag, dict_constructor)

tosca = yaml.load(sys.stdin)
tosca["tosca_definitions_version"] = "alien_dsl_2_0_0"
tosca["metadata"] = {"template_name": "indigo-types",
	"template_version": "1.0.0",
	"template_author": "indigo"}
tosca["imports"] = ["tosca-normative-types:1.0.0-ALIEN20"]
print(yaml.dump(tosca, default_flow_style=False))
