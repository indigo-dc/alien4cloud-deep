# Alien4Cloud DEEP Docker

[![GitHub license](https://img.shields.io/github/license/indigo-dc/alien4cloud-deep.svg?maxAge=2592000&style=flat-square)](https://github.com/indigo-dc/alien4cloud-deep/blob/master/LICENSE)
![Repo size](https://img.shields.io/github/repo-size/indigo-dc/alien4cloud-deep.svg?maxAge=2592000&style=flat-square)
[![Build Status](https://jenkins.indigo-datacloud.eu:8080/buildStatus/icon?job=Pipeline-as-code/alien4cloud-deep/master)](https://jenkins.indigo-datacloud.eu:8080/job/Pipeline-as-code/job/alien4cloud-deep/job/master/)


This repository contains the necessary parts needed to create a Docker image holding the Alien4cloud application, along with the DEEP - Indigo Data Cloud TOSCA types, and the plugin which connects Alien4Cloud (A4C) to the orchestrator used in DEEP.
Each time the Docker image is created, the latest version of the normative TOSCA used by IndigoDataCloud and the latest version of the plugin are integrated in this image.

## Documentation version

2.0.2

## Administration Guide

### Getting Started

Right now, we use a customized version of A4C 2.0.
The code for our version is in the *a4c* directory in the root of this repository.
The plugin code is in the *indigodc-orchestrator-plugin* directory.

The default port for A4C is 8088.

The default username / password are *admin*/*admin*. Please change them!

### Prerequisites

Aside of the official requirements found at [[Res2]](#res2), Alien4Cloud needs at least 3GB of RAM to run and a dual core CPU.

You also need Docker, we tested with the CE version 17.03 and above.
We strongly advise you to install the latest version from the official Docker site.

### Docker Arguments

We defined a number of constants to allow easy parametrisation of the container.

* **user_uid** - the UID of the non-priviledged user that runs the A4C server
* **user_gid** - the group UID of the non-priviledged user that runs the A4C server
* **a4c_ver** - the version of A4C used in the container, e.g. *2.0.0* or *2.1.0-SNAPSHOT*; This is the official version, released by the original developers
* **a4c_install_path** - the path to the instalation root directory of A4C
* **a4c_src_dir** - the relative path used to store the source of A4C
* **a4c_install_dir** - the relative path denoting the root where the A4C war is stored
* **a4c_deep_ver** - the version of A4C modified and released by the DEEP team; It uses the **a4c_ver** as prefix to which an suffix is added, e.g. *-DEEP-1.0.0*
* **a4c_user** - the user name of the non-priviledged user that executes the A4C server
* **a4c_settings_manager_ver** - The version of the settings manager that imports various settings from the environment into the A4C conf
* **tosca_normative_url** - The URL to the TOSCA normative types ram YAML file as implemented by OpenStack; It must be respect the version set by **tosca_normative_version**
* **tosca_normative_version** - The version of TOSCA currently supported by this bundle
* **tosca_normative_name** - The name set in the TOSCA normative types file obtained from **tosca_normative_url**; This name is set in the *metadata*->*template_name* tag
* **tosca_indigo_types_branch** - The branch of the Indigo custom types
* **spring_social_oidc_branch** - The branch of the Spring OIDC Social plugin  used in this build
* **a4c_deep_branch** -  The branch of the DEEP A4C fork used in this build
* **a4c_binary_dist_url** - The binary distribution of the original A4C

### Docker Variables

The docker file contains environment variables.
Some of them can be modified by the user when running a container.
These are denoted by RW (read-write).
The other group exists just to indicate features that were builtin in the container.
This group is marked with RO (read-only).
The RO group can be modified by modifying the args detailed in the previous paragraph.
If the name of a variable is the same as the name of an argument, but with capital letters, then the variable is initialized with the valued of the argument.

* **A4C_SH_NAME** (RO) - the name of the script that executes A4C; Script generated by the Settings Manager in the **a4c_install_dir** folder (it has to be in the same folder with the war)
* **A4C_PORT_HTTP** (RO) - the port on which A4C is exposed
* **A4C_PORT_HTTPS** (RO) - the port on which A4C is exposed
* **A4C_INSTALL_PATH** (RO) - the path to the instalation root directory of A4C
* **A4C_SRC_DIR** (RO) - the relative path used to store the source of A4C
* **A4C_INSTALL_DIR** (RO) - the relative path denoting the root where the A4C war is stored
* **A4C_USER** (RO) - the user name of the non-priviledged user that executes the A4C server

* **A4C_RUNTIME_DIR** (RW) - the container directory that holds the runtime data generated by A4C during runtime  like logs, uploads, and the Elastic Search cluster data
* **A4C_RESET_CONFIG**  (RW) - Caution! Enable the A4C settings manager execution with old data present; This will delete everything from the directory set in the **A4C_RUNTIME_DIR** and will let the settings manager re-create the A4C config files; Either "true" or "false"; Disabled for the time being
* **A4C_ADMIN_USERNAME** (RW) - the username of the default admin user of A4C
* **A4C_ADMIN_PASSWORD** (RW) - the password of the default admin user of A4C
* **A4C_ENABLE_SSL** (RW) - enable SSL mode for A4C; HTTP will be disabled; Either "true" or "false"
* **A4C_KEY_STORE_PASSWORD** (RW) - the password of the keystore used by A4C
* **A4C_KEY_PASSWORD** (RW) - the password of the key that is added using the PEM file refered by the ENV **A4C_PEM_KEY_FILE**
* **A4C_PEM_CERT_FILE** (RW) - the name of the PEM file with the certificate used to secure the A4C instance (just the name, without path)
* **A4C_PEM_KEY_FILE** (RW) - the name of the PEM file with the key used to secure the A4C instance (just the name, without path)
* **A4C_CERTS_ROOT_PATH** (RW) - the full path to the directory containing the files refered by **A4C_PEM_CERT_FILE**, **A4C_PEM_KEY_FILE**, **A4C_ORCHESTRATOR_PEM_CERT_FILE** (if desired), and **A4C_ORCHESTRATOR_PEM_KEY_FILE** (if desired) respectively
* **A4C_ORCHESTRATOR_URL** (RW) - the URL of the orchestrator that allows the deployment of the topologies created in A4C [[https://deep-paas.cloud.ba.infn.it/orchestrator]]
* **A4C_ORCHESTRATOR_ENABLE_KEYSTORE** (RW) - enable the keystore creation and loading for those cases when the orchestrator certificate is not signed by a recognized authority [[false]]
* **A4C_ORCHESTRATOR_KEYSTORE_PASSWORD** (RW) - the default password for the Java keystore containing the orchestrator certificate [[default]]
* **A4C_ORCHESTRATOR_KEY_PASSWORD** (RW) - the password to be used to protected the certificate inside the Java keystore [[default]]
* **A4C_ORCHESTRATOR_PEM_CERT_FILE** (RW) - the file name of the orchestrator certificate file that is available on **A4C_CERTS_ROOT_PATH**  [[orchestrator_ca.pem]]
* **A4C_ORCHESTRATOR_PEM_KEY_FILE** (RW) - the file name of the orchestrator certificate key file that is available on **A4C_CERTS_ROOT_PATH** [[orchestrator_ca-key.pem]]
* **A4C_SPRING_OIDC_ISSUER** (RW) - the URL of the IAM that performs the authentication of the user; For more details, please check the Spring OIDC A4C plugin at [[Res8]](#res8) [[https://iam.deep-hybrid-datacloud.eu]]
* **A4C_SPRING_OIDC_CLIENT_ID** (RW) - the ID of the client registered on the IAM; The client normally represents an A4C instance; For more details, please check the Spring OIDC A4C plugin at [[Res8]](#res8) [[<none>]]
* **A4C_SPRING_OIDC_CLIENT_SECRET** (RW) - the secret of the client registered on the IAM; The client normally represents an A4C instance; For more details, please check the Spring OIDC A4C plugin at [[Res8]](#res8) [[<none>]]
* **A4C_SPRING_OIDC_ROLES** (RW) - the roles given to the users created on the fly on A4C; Use CSV style approach, that is list of elements with single quote, separated by one and only one comma eg 'ADMIN','APPLICATION MANGER','DEV, OPS' at least one element is necessary; For more details, please check the Spring OIDC A4C plugin at [[Res8]](#res8) [[<none>]]
* **A4C_JAVA_XMX_MEMO** (RW) - the maximum size of the Java heap memory allocated to the A4C process; it must respect the Java nomeclature see [[Res9]](#res9) [[<none>]]

### Settings Manager

We included a small Java utility program that runs each time the container is started.
It takes some of the values defined by the Docker ENV variables listed earlier, and modifies the config files of the a4c instance.
The **A4C_ADMIN_USERNAME** defines a custom name for the admin user.
This utility writes it in the **a4c_install_path**/**a4c_install_dir**/config/alien4cloud-config.yml.
The code is in the _alien4cloud-settings-manager_ folder.

We also use this application to convert the certificates provided as detailed in the next sections to a Java storing format.
You can protect this way the a4c communication with SSL.

### Security

Skip this section if you don't want to deploy a secured A4C instance.

If you want to activate the HTTPS protocol for the A4C instance, you must set the **A4C_ENABLE_SSL** ENV to "true", without double quotes. Furthermore, you must mount an external volume containing a ca pem certificate (with the file name controlled by the **A4C_PEM_CERT_FILE** ENV) and a ca pem key (with the file name controlled by the **A4C_PEM_KEY_FILE** ENV). Finally, since the previous two files must be just file names, you can control the path inside the container for the mount using the **A4C_CERTS_ROOT_PATH** ENV. You can control the Java keystore password by the means of the **A4C_KEY_STORE_PASSWORD** ENV, and set the password for your key using the **A4C_KEY_PASSWORD** ENV. Take a look at the examples in the _Deployment_ section.

### Deployment

You can get the docker image from DockerHub IndigoDC repo [[Res5]](#res5).


* Get the Docker container by running:

```
docker pull indigodatacloud/alien4cloud-deep
```

* Create the deployment using:

```
docker run -d --name alien4cloud-deep  -p ${A4C_PORT_HTTP}:${A4C_PORT_HTTP} -e ${A4C_SPRING_OIDC_CLIENT_ID}=<IAM client ID> -e ${A4C_SPRING_OIDC_CLIENT_SECRET}=<IAM client secret> -e ${A4C_SPRING_OIDC_ROLES}=<list of roles>  indigodatacloud/alien4cloud-deep
```

where ${A4C_PORT}, ${A4C_SPRING_OIDC_CLIENT_ID}, ${A4C_SPRING_OIDC_CLIENT_SECRET}, ${A4C_SPRING_OIDC_ROLES} are explained in the *Docker Variables* section of this README.

* Follow the logs (and wait for the web app to start up):

```
docker logs -f alien4cloud-deep
```

* You can also mount the directory containing the runtime data generated by A4C, e.g.:

```
docker run -d --name alien4cloud-deep  -p ${A4C_PORT_HTTP}:${A4C_PORT_HTTP} -e ${A4C_SPRING_OIDC_CLIENT_ID}=<IAM client ID> -e ${A4C_SPRING_OIDC_CLIENT_SECRET}=<IAM client secret> -e ${A4C_SPRING_OIDC_ROLES}=<list of roles> -e A4C_RUNTIME_DIR=${A4C_RUNTIME_DIR_MINE} -v /mnt/a4c_runtime:${A4C_RUNTIME_DIR_MINE} indigodatacloud/alien4cloud-deep
```

where ${A4C_VOLUME_DIR}, ${A4C_PORT}, ${A4C_SPRING_OIDC_CLIENT_ID}, ${A4C_SPRING_OIDC_CLIENT_SECRET}, ${A4C_SPRING_OIDC_ROLES} are explained in the *Docker Variables* section of this README.

* In order to secure the A4C connection you can use the following:

```
docker run -d --name alien4cloud-deep  -p ${A4C_PORT_HTTPS}:${A4C_PORT_HTTPS} -e ${A4C_SPRING_OIDC_CLIENT_ID}=<IAM client ID> -e ${A4C_SPRING_OIDC_CLIENT_SECRET}=<IAM client secret> -e ${A4C_SPRING_OIDC_ROLES}=<list of roles> -e A4C_RUNTIME_DIR=${A4C_RUNTIME_DIR_MINE} -v <path to directory that will hold A4C's runtime data>:${A4C_RUNTIME_DIR_MINE} -e A4C_CERTS_ROOT_PATH=${A4C_CERTS_ROOT_PATH_MINE} -v <path to certificates root>:${A4C_CERTS_ROOT_PATH_MINE} -e A4C_PEM_CERT_FILE=<ca cert file name> -e A4C_PEM_KEY_FILE=<ca private key file name> -e A4C_KEY_PASSWORD=<password used to sign the certificate> -e A4C_ENABLE_SSL=true indigodatacloud/alien4cloud-deep
```

where you have to specify the secure port, the root path where the certificates are on the host and its mapping inside the container, the names of the key and certificates pems and to enable SSL

### REST API

A4C supports a REST API [[Res4]](#res4) that can be used for many operations.
We list some examples in the following paragraphs.

* (MUST BE DONE FIRST) authenticate & set general variables

```
 # Set the A4C server
export ALIEN_URL='http://example.com:8088'
 # Set the cookie file path
export ALIEN_COOKIE='/tmp/a4c cookie.txt'
 # Authenticate, check if the file
 # This call protects the username and password by using the read call
 # When you execute the following line, the shell will wait for two values separated by the ENTER key, before executing the curl call
 # The first value is the username, the second is the password

curl -k -c "${ALIEN_COOKIE}" "$ALIEN_URL/login" --data-urlencode "username=$( read -s U; echo $U )" --data-urlencode "password=$( read -s P; echo $P )" --data-urlencode "submit=Login" -XPOST -H 'Content-Type: application/x-www-form-urlencoded'
```

* create a new user (you can select either combination of the roles; we list all available)

```
 # Create new user; the cookie must be from an ADMIN type user
curl -k -b "${ALIEN_COOKIE}" -XPOST -H 'Content-Type: application/json; charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data '{"email": "a@a", "firstName": "fn", "lastName": "ln", "password": "pass new", "roles": ["ADMIN", "APPLICATIONS_MANAGER", "ARCHITECT", "COMPONENTS_BROWSER",  "COMPONENTS_MANAGER"], "username": "tst2"}' "$ALIEN_URL/rest/v1/users"
```

* update existing user

```
 # Set the username you want to change; you can use the read call as when you authenticated yourself
export ALIEN_USERNAME='tst2'
 # Update user; the cookie must be from an ADMIN type user
curl -k -b "${ALIEN_COOKIE}" -XPUT -H 'Content-Type: application/json; charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data '{"email": "a@a", "firstName": "fn", "lastName": "ln", "password": "pass new", "roles": ["ADMIN", "APPLICATIONS_MANAGER", "ARCHITECT", "COMPONENTS_BROWSER",  "COMPONENTS_MANAGER"], "username": "${ALIEN_USERNAME}"}' "$ALIEN_URL/rest/v1/users/${ALIEN_USERNAME}"
```

### Usage

Once Alien4Cloud started, you can connect to it using a supported browser [[Res3]](#res3).
Before anything else, one should take a look at the [[Res2]](#res2).

#### Plugin activation

The IndigoDataCloud Orchestrator plugin comes preinstalled in the Docker container. It should also be activated by default. Please check the list from **Administration** -> **Plugins**. The switch on the right should be green.

#### Instance Creation

First and foremost, one should create and instance of the DEEP - IndigoDataCloud Orchestrator plugin.
Next, the values for the parameters should be set (explanation for each in the *Plugin Parameters* subsection.
Finally, on the same page, one has to create a location.
We support only one location, that is *Deep Orchestrator Location*.

#### Users

For the moment, the plugin uses the username and password utilized during the A4C login.
As a result, once you have A4C up and running, please create a new user having the same username and password as in IAM.
A user can be added using the  **Administration** -> **Users** -> **New User** functionality.
The web application stores the information locally.
If you change it in the IAM, you have to manually update it for each A4C instance.

The A4C user roles are not taken in consideration by the plugin.
The admin should consider the proper role(s) for a user only in the context of the A4C running instance.

#### Plugin Parameters

<!---* **user** - The user used to obtain the authorization token to deploy topologies on the DEEP orchestrator. One can register on the page defined by the **iamHost** variable.
* **password** - The password used in conjuction with **user** to obtain the authorization token to deploy topologies on the DEEP orchestrator. One can register on the page defined by the **iamHost** variable.
* **clientId** - Once one has an account on the **iamHost**, an application can be registered. After registration, the IAM server generates a unique pair of a **clientId** and a **clientSecret** that is needed to get a token.
* **clientSecret** - Once one has an account on the **iamHost**, an application can be registered. After registration, the IAM server generates a unique pair of a **clientId** and a **clientSecret** that is needed to get a token.
* **tokenEndpoint** - Once one has a **clientId** and a **clientSecret**, a token can be obtained using the endpoint defined by this variable.
* **tokenEndpointCert** - The certificate used by the **tokenEndpoint**, if the server uses an encrypted connection (HTTPS). Take a look at *Obtain Certificate* subsection to learn how to obtain the certificate.
* **clientScopes** - When calling the token generator endpoint, one has to supply a list of scopes for the token. This list has the elements separated by a space e.g. *openid profile email offline_access*
* **orchestratorEndpoint** - The endpoint of the orchestrator used to deploy the topologies.
* **orchestratorEndpointCert** - The certificate of the **orchestratorEndpoint** server. Take a look at *Obtain Certificate* subsection to learn how to obtain the certificate.
* **iamHost** - The host that allows one to register an account, and get the **clientId** and the **clientSecret**.
* **iamHostCert** - The certificate of the **iamHost** server. Take a look at *Obtain Certificate* subsection to learn how to obtain the certificate.-->
* **orchestratorPollInterval** - Alien4Cloud tries to obtain the history of events every number of seconds. This parameter sets that number.
* **importIndigoCustomTypes** - Depending on the status of the work, there can be a different location of the indigo types definition file that is sent to the orchestrator. This field allows the admin to specify which TOSCA types file is used for all deployments that go through the IndigoDCOrchestrator plugin. Please keep in mind that this field affects only the future deployments. This happens because it is read when a topology is deployed.

#### Obtain Certificate

In order to obtain the certificates used by the plugin and stored in the **tokenEndpointCert**, **orchestratorEndpointCert**, and **iamHostCert** respectively, you should follow the steps described in this subsection for each different server.
We refer to servers as in the following unit: _*.domain.extension_.
Two variables may point towards the same server, but with different paths.
_deep-paas.cloud.ba.infn.it/rest/v1_ and _deep-paas.cloud.ba.infn.it/token_ point towards the same domain/subdomains with different paths, therefore you only have to execute the following procedure once, for _deep-paas.cloud.ba.infn.it_.
You also need the port (normally 443 for HTTPS), as you'll see next.


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

### Testing

We implemented a series of tests that should be executed during the maven building step.
You can find them in the **indigodc-orchestrator-plugin/src/test/** directory.

You can run the unit tests by calling _mvn_ with a specific target:

```
mvn clean test
```

#### Coverage

During the plugin building process, we use Jacoco (through the Eclipse plugin for fast coverage execution, or Maven for automatic release).
Jacoco generates the _jacoco-deep.exec_ file in the target/coverage-reports/ directory, after running the maven **test** lifecycle.

If you want to visualize the results in a human friendly format, you can convert the _jacoco-deep.exec_ file to html using the CLI from the package at https://www.eclemma.org/jacoco/, e.g.

```
java -jar jacoco/lib/jacococli.jar report target/coverage-reports/jacoco-deep.exec --classfiles target/classes/ --html plugin-html-coverage-report/
```

We require a minimum of 70% overall coverage by unit tests. When the plugin is built, Maven checks if the minimum threshold has been achieved or exceeded. If it hasn't, then the whole process has failed.

#### Style

The A4C orchestrator plugin's Java code must respect the Google Java formatting style [[Res6]](#res6).

We use checkstyle with Maven, as a plugin. This way we can rest assured that the committed code, that passes the continuous integration testing, respects the required formatting. The building process fails when warnings are encountered. We use the oficial checkstyle repository [[Res7]](#res7) to obtain a stable version of _com.puppycrawl.tools.checkstyle_ dependency of the _org.apache.maven.plugins.maven-checkstyle-plugin_ plugin. The checkstyle team includes a formatter ready to be used that respects Google's rules.

## User Guide

This section contains the information necessary for the user to use an already deployed instance of Alien4Cloud-deep.
We consider that the administrator(s) followed the guidelines from the previous section to set up a Docker container ready to be used.
Please contact your administrator if you encounter any problem.

We also strongly recommend to read the official guide available at [[Res2]](#res2).
We try to summarize the steps in the following sub-sections.

### Registration

Alien4Cloud can only be used by registered users.
Before anything, please be sure that you have an account registered with [[Res1]](#res1).

### Login

You have to use the user name and password as entered during the registration process on [[Res1]](#res1).
Alien4Cloud cannot be used without login.
Once you logged in successfully, you can access different parts of the web platform.
We use rights associated with each user, therefore it may be possible that you can't perform some actions (e.g. add new TOSCA custom types in the catalog).

### Topology creation

Once you have logged in, go to the **Applications** tab.
Open the *New application* window by clicking the button with the same name.
Give a name to your app and, optionally, a description.
A4C automatically generates the *Archive Id*.
After you press the *Create* button, a new screen with the application details greets you.
In the *Work on an environment* paragraph, you can click on the element on the row of the table listing all the environments.
A screen with the steps to follow to launch your topology should be displayed to you.

### Topology editor

The **Topology** step should be selected, and an *Edit* button available.
You shouldn't be able to navigate further until you create a topology, and it is valid.
The **Matching** step should run without any problems, as we support all nodes from TOSCA standard and IndigoDataCloud.

The web platform offers two editors.
To exit the editor, click on the *Environment* or *Topology* arrow-buttons on the top-left.

#### UI editor

The main one is the graphical interface, pre-selected once you start editing the topology.
Using the vertical buttons on right, you can display the list of TOSCA nodes available for deployment.
Clicking on a button opens a window with the nodes.
The search field on the top filters the available options by TOSCA name.
You can add a node by simply drag and drop it on the available central space.
Selecting a node by clicking it brings up a window with the list of properties for that node.
Do not forget to save the topology using the button from the top-left.

#### Text editor

Using the *Archive content* button on the left, you can see the topology in text mode.
Once you open this ditor view, click on the *topology.yml* file to display its content in the editor.

You can manually modify various properties, but please keep in mind that Alien4Cloud was designed with the UI editor in mind.
The text editor can behave unexpectedly, e.g. if something is unsupported (like custom outputs) it either deletes
the changes or shows an error.

Secondly, do not forget that the Alien4Cloud TOSCA has some differences when compared to the normative.
Therefore, you may find some bits that may seem strange if you know the normative TOSCA, like the imports that point towards a version instead of a file.
Our plugin takes care of the necessary conversion, when it is required.

Once you save the topology through this editor view, the UI should be updated.
As explained earlier, some changes may not be reflected in the topology and may automatically be removed.
Always save and then switch to the UI editor to see if the changes were actually applied.
Then switch back to the text editor to see if they are still there.

### Topology inputs

Alien4Cloud has a separate screen that allows you to define the values for the inputs.
It cannot proceed further until you do so.

### Topology deployment

Once you created your topology, selected a  location, set the inputs, set the matching nodes if required, you can proceed to deploy the application.
Once you launched your topology, you have to wait until it is deployed.
Please refresh your browser if nothing happens for a while.
The **Manage current deployment** tab contains the details about your deployment.
We currently do not support showing information about individual nodes of the deployment.

### Topology undeployment

If there has been an error during deployment on the orchestrator responded or the deployment has been successfully created, you can use the *Undeploy* button in order to undeploy the application.
It is accessible in the **Manage current deployment** tab.

## Authors

* **Andy S Alic** - *Main Dev* - [asalic](https://github.com/asalic)

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

Thanks for help go to:

* **Miguel Caballer** - [micafer](https://github.com/micafer)

* **Germán Moltó** - [gmolto](https://github.com/gmolto)

## Resources

#### [Res1]
IAM authentication portal: https://iam.deep-hybrid-datacloud.eu

#### [Res2]
Alien4Cloud Official Documentation: https://alien4cloud.github.io/

#### [Res3]
Alien4Cloud supported platforms: https://alien4cloud.github.io/#/documentation/2.0.0/admin_guide/supported_platforms.html

#### [Res4]
Alien4Cloud REST API: http://alien4cloud.github.io/#/documentation/2.0.0/rest/overview.html

#### [Res5]
Deep Docker container on DockerHub: https://hub.docker.com/r/indigodatacloud/alien4cloud-deep/

#### [Res6]
Google Java style on Github: https://github.com/google/google-java-format

#### [Res7]
Oficial checkstyle repository: https://github.com/checkstyle/checkstyle

#### [Res8]
Spring social OIDC A4C plugin repository for OAuth2 authentication: https://github.com/indigo-dc/spring-social-oidc


#### [Res8]
Java Xmx and Xms nomenclature: https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABDJJFI
