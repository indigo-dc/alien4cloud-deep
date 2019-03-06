import yaml
import json
import sys
import collections
import os
from pathlib import Path
import argparse
import requests
from typing import Type, List

parser = argparse.ArgumentParser()
parser.add_argument("-z", help="The path of the zipped plugin")
parser.add_argument("-w", help="The web address of A4C")
parser.add_argument("-u", help="The user name of an admin of A4C")
parser.add_argument("-p", help="The password of an admin of A4C")
args = parser.parse_args()

pathPlugin = args.z
url = args.w
user = args.u
passw = args.p

PLUGIN_ID = "alien4cloud-deep-provider"
NAME_NEW_ORCHESTRATOR = "Deep Orchestrator"

def login(url: str, user: str, passw: str):
  headers: dict = {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
  payload: dict = {'username': user,'password': passw, "submit": "Login"}
  session = requests.Session()
  session.get(url)
  res = session.post(url + "/login", headers=headers, data=payload, verify=False)
  return session

def getOrchestrators(url: str, session):
  result = session.get(url + "/rest/v1/orchestrators")
  data = json.loads(result.text)
  idsToBeDel = []
  if len(data["data"]) > 0:
    orchestrators = data["data"]["data"]
    for o in orchestrators:
      if o["pluginId"] == PLUGIN_ID:
        idsToBeDel.append(o["id"])
  return idsToBeDel

def disableOrchestrators(idsToBeDel: List, url: str, session):
  for oId  in idsToBeDel:
      result = session.delete(url + "/rest/v1/orchestrators/{}/instance?force=true&clearDeployments=true".format(oId))

def delOrchestrators(idsToBeDel: List, url: str, session):
  for oId  in idsToBeDel:
      result = session.delete(url + "/rest/v1/orchestrators/{}".format(oId))

def delPlugin( url: str, session):
  result = session.delete(url + "/rest/v1/plugins/{}".format(PLUGIN_ID))

def putPluginNewVer(pathPlugin: str, url: str, session):
  filehandle = open(pathPlugin, "rb")
  payload: dict = {'file': filehandle}
  result = session.post(url + "/rest/v1/plugins", data={}, files=payload)
  print(result.text)

def createNewOrchestrator(url: str, session):
  orchestratorRequest = {"name": NAME_NEW_ORCHESTRATOR,
      "plugin": PLUGIN_ID
    }
  headers: dict = {
        'Content-Type': 'application/json'
    }
  result = session.post(url + "/rest/v1/orchestrators", headers=headers,
    data=orchestratorRequest)
  print(result.text)

s = login(url, user, passw)
idsToBeDel = getOrchestrators(url, s)
disableOrchestrators(idsToBeDel, url, s)
delOrchestrators(idsToBeDel, url, s)
delPlugin(url, s)
putPluginNewVer(pathPlugin, url, s);
#createNewOrchestrator(url, s)
