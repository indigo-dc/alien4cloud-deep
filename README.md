# Alien4Cloud DEEP Docker


This repository contains the necessary parts needed to create a Docker image holding the Alien4cloud application, along with the DEEP - Indigo Data Cloud TOSCA types, and the plugin which connects Alien4Cloud (A4C) to the orchestrator used in DEEP.
Each time the Docker image is created, the latest version of the normative TOSCA used by IndigoDataCloud and the latest version of the plugin are integrated in this image.

## Getting Started

Right now, we use a customized version of A4C 2.0.
The code for our version is in the *a4c* directory in the root of this repository.
The plugin code is in the *indigodc-orchestrator-plugin* directory.

The default port for A4C is 8088.

The default username / password are *admin*/*admin*. Please change them!

## Deployment

You can get the docker image from [DockerHub IndigoDC repo](https://hub.docker.com/r/indigodatacloud/alien4cloud-deep/).


Get the Docker container by running:

```
docker pull indigodatacloud/alien4cloud-deep
```

Create the deployment using:

```
docker run -d --name alien4cloud-deep  -p 8088:8088 indigodatacloud/alien4cloud-deep
```


Follow the logs (and wait for the web app to start up):

```
docker logs -f alien4cloud-deep
```

## Usage

Once Alien4Cloud started, you can connect to it using a [supported browser] (https://alien4cloud.github.io/#/documentation/2.0.0/admin_guide/supported_platforms.html).
Before anything else, one should take a look at the [Alien4Cloud documentation](https://alien4cloud.github.io/#/documentation/2.0.0/).

### Plugin activation

The IndigoDataCloud Orchestrator plugin comes preinstalled in the Docker container. It should also be activated by default. Please check the list from **Administration** -> **Plugins**. The switch on the right should be green.

### Instance Creation

First and foremost, one should create and instance of the DEEP - IndigoDataCloud Orchestrator plugin.
Next, the values for the parameters should be set (explanation for each in the *Plugin Parameters* subsection.
Finally, on the same page, one has to create a location.
We support only one location, that is *Deep Orchestrator Location*.

### Users

For the moment, the plugin uses the username and password utilized during the A4C login.
As a result, once you have A4C up and running, please create a new user having the same username and password as in IAM.
A user can be added using the  **Administration** -> **Users** -> **New User** functionality.
The web application stores the information locally.
If you change it in the IAM, you have to manually update it for each A4C instance.

The A4C user roles are not taken in consideration by the plugin.
The admin should consider the proper role(s) for a user only in the context of the A4C running instance.

### Obtain Certificate

1. Install openssl

```
apt-get install openssl
```

2. Get the key from the server (example for the development server managing the orchestrator).
Use echo to terminate the connection

```
echo "Q" | openssl s_client -showcerts -servername deep-paas.cloud.ba.infn.it -connect deep-paas.cloud.ba.infn.it:443 > crt.tmp
```

3. Copy a certificate from *crt.tmp*.
This file may contain more than one, any is valid.
You need the characters between *-----BEGIN CERTIFICATE-----* and *-----END CERTIFICATE-----*, for example to get all certificates separated by new lines:

```
grep -m 1 -ozP '(?<=-----BEGIN\ CERTIFICATE-----)(\n|.)+?(?=-----END\ CERTIFICATE-----)' crt.tmp > crt_solos.tmp
```

4. Remove the new lines from one certificate with your prefered approach, e.g.:

```
cat crt_solos.tmp | tr -d '\n'
```

5. This BASE64 string can now be used with the app

### Plugin Parameters

<!---* **user** - The user used to obtain the authorization token to deploy topologies on the DEEP orchestrator. One can register on the page defined by the **iamHost** variable.
* **password** - The password used in conjuction with **user** to obtain the authorization token to deploy topologies on the DEEP orchestrator. One can register on the page defined by the **iamHost** variable.-->
* **clientId** - Once one has an account on the **iamHost**, an application can be registered. After registration, the IAM server generates a unique pair of a **clientId** and a **clientSecret** that is needed along with the **user** and **password** to get a token.
* **clientSecret** - Once one has an account on the **iamHost**, an application can be registered. After registration, the IAM server generates a unique pair of a **clientId** and a **clientSecret** that is needed along with the **user** and **password** to get a token.
* **tokenEndpoint** - Once one has a **user**, **password**, **clientId**, and **clientSecret**, a token can be obtained using the endpoint defined by this variable.
* **tokenEndpointCert** - The certificate used by the **tokenEndpoint**, if the server uses an encrypted connection (HTTPS). Take a look at *Obtain Certificate* subsection to learn how to obtain the certificate.
* **clientScopes** - When calling the token generator endpoint, one has to supply a list of scopes for the token. This list has the elements separated by a space e.g. *openid profile email offline_access*
* **orchestratorEndpoint** - The endpoint of the orchestrator used to deploy the topologies.
* **orchestratorEndpointCert** - The certificate of the **orchestratorEndpoint** server. Take a look at *Obtain Certificate* subsection to learn how to obtain the certificate.
* **iamHost** - The host that allows one to register an account, and get the **clientId** and the **clientSecret**.
* **iamHostCert** - The certificate of the **iamHost** server. Take a look at *Obtain Certificate* subsection to learn how to obtain the certificate.
* **orchestratorPollInterval** - Alien4Cloud tries to obtain the history of events every number of seconds. This parameter sets that number.
* **importIndigoCustomTypes** - Depending on the status of the work, there can be a different location of the indigo types definition file that is sent to the orchestrator. This field allows the admin to specify which TOSCA types file is used for all deployments that go through the IndigoDCOrchestrator plugin. Please keep in mind that this field affects only the future deployments. This happens because it is read when a topology is deployed.

### Launching a Topology (Fast Lane)

Once you have logged in, go to the **Applications** tab.
Open the *New application* window by clicking the button with the same name.
Give a name to your app and, optionally, a description.
A4C automatically generates the *Archive Id*.
After you press the *Create* button, a new screen with the application details greets you.
In the *Work on an environment* paragraph, you can click on the element on the row of the table listing all the environments.
A screen with the steps to follow to launch your topology should be displayed to you.
The **Topology** step should be selected, and an *Edit* button available.
You shouldn't be able to navigate further until you create a topology, and it is valid.
Once you do that, you can set the inputs, then select the location created as specified earlier in this chapter and finally deploy your application.
The **Matching** step should run without any problems, as we support all nodes from TOSCA standard and IndigoDataCloud.
Once you launched your topology, you have to wait until it is deployed.
Please refresh your browser if nothing happens for a while.
The **Manage current deployment** tab contains the details about your deployment.
We currently do not support showing information about individual nodes of the deployment.

## Testing

We implemented a series of tests that should be executed during the maven building step.
You can find them in the **indigodc-orchestrator-plugin/src/test/** directory.

## Known Issues

Please take a look at the issues list on Github.

## Authors

* **Andy S Alic** - *Main Dev* - [asalic](https://github.com/asalic)

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

Thanks for help go to:

* **Miguel Caballer** - [micafer](https://github.com/micafer)

* **Germán Moltó** - [gmolto](https://github.com/gmolto)
