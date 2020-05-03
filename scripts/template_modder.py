import yaml
import sys
import collections
from collections import OrderedDict
import os
from pathlib import Path
import zipfile
import urllib.request
from urllib.parse import urlparse
import logging
import math
from itertools import chain

if len(sys.argv) != 5:
    raise Exception("This script takes the normatives, the IndigoCustom types and the DEEP templates and zips them for use in A4C.\n \
    Required parameters:\n \
    (1) the original TOSCA normative types file full path\n \
    (2) the original Indigo DC types root folder (with the yml, images and other files to be included in the A4C CSAR)\n \
    (3) the full path of the directory containing the templates\n \
    (4) the full output path of the directory where the zips will be stored")


TOSCA_NORMATIVES_NAME = "normative-types"
TOSCA_NORMATIVES_VERSION = "1.0.0"
TOSCA_TEMPLATE_DEFAULT_VER = "4.0.1"
TOSCA_FILES_MAX_COUNT = 999999
TOSCA_FILES_MAX_COUNT_DIGITS = str(int(math.log10(TOSCA_FILES_MAX_COUNT))+1)

LOG = logging.getLogger("my-logger")


def addFolderToZip(zipFile, basePath, relativeZipPath):
    currentPath = os.path.join(basePath, relativeZipPath)
    for file in os.listdir(currentPath):
        fPath = os.path.join(currentPath, file)
        if os.path.isfile(fPath) and not os.fsdecode(fPath).lower().endswith( ('.yml', '.yaml') ):
            zipFile.write(fPath, os.path.join(relativeZipPath, file))
        elif os.path.isdir(fPath):
            addFolderToZip(zipFile, basePath, os.path.join(relativeZipPath, file))

def dict_representer(dumper, data):
  return dumper.represent_dict(data.items())

def dict_constructor(loader, node):
  return collections.OrderedDict(loader.construct_pairs(node))

def createImportsForTemplates(toscaObjs):
    '''
    Create the name:version approach of A4C imports from all the types groups
    already parsed, and in the toscaObjs ordered dict. Any grouped types without
    a metadata->template_name and metadata->template_version are ignored. The types are added
    in the order found in the argument ordered dict
    @param toscaObjs: the ordered dict of parsed YAML representation of the base types.
        the keys are the full paths of the files with the groups, values are the parsed YAMLs
    @return: the list of imports base on the input in the A4C form
    '''
    imports = []
    for [name, toscaO] in toscaObjs.items():
        if "template_version" in toscaO["metadata"] and "template_name" in toscaO["metadata"]:
            imports.append({toscaO["metadata"]["template_name"]: toscaO["metadata"]["template_version"]});
        else:
            LOG.warning("Unable to find metadata template_name and/or template version for types in " + name)
    return imports


def processToscaNormatives(count, filePath, zipOutputFolder):
    with open(filePath, 'r') as streamN:
        normativesObj = yaml.load(streamN, Loader=yaml.FullLoader)
        count += 1
        templateName = TOSCA_NORMATIVES_NAME
        templateVersion = TOSCA_NORMATIVES_VERSION
        if not "metadata" in normativesObj:
            normativesObj["metadata"] = {}
        if not "template_name" in normativesObj["metadata"]:
            normativesObj["metadata"]["template_name"] = TOSCA_NORMATIVES_NAME
        else:
            templateName = normativesObj["metadata"]["template_name"]
        if not "template_version" in normativesObj["metadata"]:
            normativesObj["metadata"]["template_version"] = TOSCA_NORMATIVES_VERSION
        else:
            templateVersion = normativesObj["metadata"]["template_version"]
            #if not "template_author" in normativesObj["metadata"]:
            #    normativesObj["metadata"]["template_author"] = "OpenStack"
        if not "description" in normativesObj:
            normativesObj["description"] = "Contains the normative types definition as defined by the OpenStack distro"
        z = zipfile.ZipFile(os.path.join(zipOutputFolder,
            #('{:0' + TOSCA_FILES_MAX_COUNT_DIGITS + 'd}').format(count) + "_" +
            TOSCA_NORMATIVES_NAME + ".zip"), 'w', zipfile.ZIP_DEFLATED)
        z.writestr(TOSCA_NORMATIVES_NAME + ".yaml", yaml.dump(normativesObj, default_flow_style=False))
        z.close()
        result = collections.OrderedDict()
        result[filePath] = normativesObj
        return result, count, templateName, templateVersion
    raise Exception("Cannot open " + filePath)

def processIndigoCustomTypes(count, rootPath, zipOutputFolder, normativesName, normativesVersion):
    list = os.listdir(rootPath)
    customTypesYamlLst = []
    resourcesLst = []
    customTypesObjs = collections.OrderedDict()
    for file in list:
        if os.fsdecode(file).lower().endswith( ('.yml', '.yaml') ):
            customTypesYamlLst.append(file)
        else:
            resourcesLst.append(file)
    for file in customTypesYamlLst:
        customsP = os.fsdecode(file)
        with open(os.path.join(rootPath, customsP), 'r') as streamC:
            customsPBase = os.path.splitext(os.path.basename(customsP))[0]
            count += 1
            customsObj = yaml.load(streamC, Loader=yaml.FullLoader)
            if "imports" in customsObj:
                customsObj["imports"].append({normativesName: normativesVersion})
            else:
                customsObj["imports"] = [{normativesName: normativesVersion}]
            customTypesObjs[customsP] =  customsObj
            z = zipfile.ZipFile(os.path.join(zipOutputFolder,
                #('{:0' + TOSCA_FILES_MAX_COUNT_DIGITS + 'd}').format(count) + "_" +
                customsPBase + ".zip"), 'w', zipfile.ZIP_DEFLATED)
            z.writestr(customsP, yaml.dump(customsObj, default_flow_style=False))
            addFolderToZip(z, rootPath, "")
            z.close()
    return customTypesObjs, count



_mapping_tag = yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG
yaml.add_representer(collections.OrderedDict, dict_representer)
yaml.add_constructor(_mapping_tag, dict_constructor)

def processTemplates(rootTemplateDir, zipOutputFolder, importTypes, count):
        list = os.listdir(rootTemplateDir)
        for file in list:
            filename = os.fsdecode(file)
            if filename.lower().endswith( ('.yml', '.yaml') ): # whatever file types you're using...
                with open(os.path.join(templatesP, filename), 'r') as stream:
                    templateObj = yaml.load(stream, Loader=yaml.FullLoader)
                    filenameBase = os.path.splitext(os.path.basename(filename))[0]
                    templateObj.move_to_end("tosca_definitions_version", last=False)
                    #bak = templateObj["tosca_definitions_version"]
                    #del templateObj["tosca_definitions_version"]
                    #templateObj["tosca_definitions_version"] = bak
                    # Add imports a4c style
                    templateObj["imports"] = []

                    for importType in importTypes:
                        templateObj["imports"].append(importType)
                    icon = None
                    iconName = None
                    # Create/enhance metadate
                    if not "metadata" in templateObj:
                        templateObj["metadata"] = {}
                    if not "template_name" in templateObj["metadata"]:
                        templateObj["metadata"]["template_name"] = filenameBase
                    if not "template_version" in templateObj["metadata"]:
                        templateObj["metadata"]["template_version"] = TOSCA_TEMPLATE_DEFAULT_VER
                    if not "template_author" in templateObj["metadata"]:
                        templateObj["metadata"]["template_author"] = "DEEP team"
                    # Download icon and pack it
                    if "icon" in templateObj["metadata"]:
                        response = urllib.request.urlopen(templateObj["metadata"]["icon"])
                        icon = response.read()
                        iconName = os.path.basename(urlparse(templateObj["metadata"]["icon"]).path)
                        templateObj["metadata"]["icon"] = os.path.join("images", iconName)

                    if not "description" in templateObj:
                        templateObj["description"] = "DEEP template"

                    # Zip the template
                    z = zipfile.ZipFile(os.path.join(outP,
                        #('{:0' + TOSCA_FILES_MAX_COUNT_DIGITS + 'd}').format(count) + "_" +
                        filenameBase + ".zip"), 'w', zipfile.ZIP_DEFLATED)
                    z.writestr(filename, yaml.dump(templateObj, default_flow_style=False))
                    if not icon == None:
                        z.writestr(os.path.join("images", iconName), icon)
                    z.close()
                    count += 1

if __name__ == "__main__":
    normativesP = sys.argv[1]
    customsP = sys.argv[2]
    templatesP = sys.argv[3]
    outP = sys.argv[4]

    count = 1
    normativesObjs, count, normativesName, normativesVersion = processToscaNormatives(count, normativesP, outP)
    customTypesObjs, count = processIndigoCustomTypes(count, customsP, outP, normativesName, normativesVersion)
    concaten = collections.OrderedDict(list(normativesObjs.items()) + list(customTypesObjs.items()))
    # /print(concaten)
    importTypes = createImportsForTemplates(concaten)
    processTemplates(templatesP, outP, importTypes, count)

    if count > TOSCA_FILES_MAX_COUNT:
        raise Exception("Max num of supported tosca files reached, please increase TOSCA_FILES_MAX_COUNT")
