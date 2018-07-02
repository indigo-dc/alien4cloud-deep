# Alien4Cloud DEEP Docker

This repository contains the necessary parts needed to create a Docker image holding the Alien4cloud application, along with the DEEP - Indigo Data Cloud TOSCA types, and the plugin which connects Alien4Cloud to the orchestrator used in DEEP. Each time the Docker image is created, the latest version of the normative TOSCA used by IndigoDataCloud and the latest version of the plugin are integrated in this image.

## Getting Started

Right now, we use version 2.0 of Alien4Cloud.

The default port for Alien4Cloud is 8088.

The default username / password are admin/admin. Please change them!

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

Once Alien4Cloud started, you can connect to it using a modern browser (HTML5 support).
Before anything else, one should take a look at the [Alien4Cloud documentation](https://alien4cloud.github.io/#/documentation/2.0.0/).

First and foremost, one should create and instance of the DEEP - IndigoDataCloud Orchestrator plugin. Next, the values for the parameters should be set (explanation for each in the *Plugin Parameters* subsection. Finally, on the same page, one has to create a location. We support only one location, that is *indigodc*.

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
cat crt_solo.tmp | tr -d '\n'
```

5. This BASE64 string can now be used with the app

### Plugin Parameters

* **user** - The user used to obtain the authorization token to deploy topologies on the DEEP orchestrator. One can register on the page defined by the **iamHost** variable.
* **password** - The password used in conjuction with **user** to obtain the authorization token to deploy topologies on the DEEP orchestrator. One can register on the page defined by the **iamHost** variable.
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

## Known Issues

* The deployment status is not updated automatically. 
* *get_attribute* is not supported by Alien4Cloud. For now, we advise the users to set the value of the attributes that should get the values through *get_attribute* as strings containing the actual method call. Using the text editor is highly advisable. Example (normal TOSCA to Alien4Cloud version_): ```dns: { get_attribute: [ HOST, dns ] }``` becomes ```dns: "{ get_attribute: [ HOST, dns ] }"``` and ```IP_list:\n - { get_attribute: [ HOST, ip ] }``` becomes ```IP_list:\n - "{ get_attribute: [ HOST, ip ] }"```

## Authors

* **Andy S Alic** - *Main Dev* - [asalic](https://github.com/asalic)

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

Thanks for help go to:

* **Miguel Caballer** - [micafer](https://github.com/micafer)

* **Germán Moltó** - [gmolto](https://github.com/gmolto)