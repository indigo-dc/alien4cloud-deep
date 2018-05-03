FROM ubuntu:16.04

RUN apt-get update && apt-get -y install zip curl openjdk-8-jdk-headless git maven

# These are development only libs, speeds up testing
#RUN apt-get -y install build-essential vim htop python3 sudo bash

ARG user_uid=1000
ARG user_gid=1000
ARG a4c_ver=2.0.0
ENV A4C_PORT 8088
ENV A4C_DEBUG_PORT 5005
ENV A4C_INSTALL_PATH /opt
ENV A4C_INSTALL_DIR a4c
ENV USER a4c

# get fastconnect certificate for maven
ARG CERT_FILE=/tmp/fastconnect.org.cert
ARG HOST=fastconnect.org
ARG PORT=443
ARG KEYSTOREFILE=/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts
ARG KEYSTOREPASS=changeit

# get the SSL certificate
RUN openssl s_client -connect ${HOST}:${PORT} </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ${CERT_FILE}

# create a keystore and import certificate
RUN keytool -import -noprompt -trustcacerts -alias ${HOST} -file ${CERT_FILE} -keystore ${KEYSTOREFILE} -storepass ${KEYSTOREPASS}

# verify we've got it.
# RUN keytool -list -v -keystore ${KEYSTOREFILE} -storepass ${KEYSTOREPASS} -alias ${HOST}

RUN addgroup --gid ${user_gid} ${USER} \
  && adduser --disabled-password --gecos "" --uid ${user_uid} --gid ${user_gid} ${USER}

ADD indigodc-orchestrator-plugin "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"

# Change perms from root to restricted user
RUN chown -R ${USER}:${USER} "${A4C_INSTALL_PATH}"
RUN chown -R ${USER}:${USER} "/home/${USER}"

USER ${USER}

RUN git clone -b indigo https://github.com/grycap/tosca "${A4C_INSTALL_PATH}/indigo-dc-tosca-types"

WORKDIR "${A4C_INSTALL_PATH}"
RUN curl --cacert ${CERT_FILE} -O https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-dist/${a4c_ver}/alien4cloud-dist-${a4c_ver}-dist.tar.gz
RUN tar xvf alien4cloud-dist-${a4c_ver}-dist.tar.gz
RUN mv alien4cloud "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}"

WORKDIR "${A4C_INSTALL_PATH}/indigo-dc-tosca-types"
# RUN zip -r "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/indigo-dc-tosca-types.zip" custom_types.yml images/

WORKDIR "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"
RUN mvn clean package
RUN cp ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin/target/alien4cloud-indigodc-provider-*.zip "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/plugins/"

EXPOSE ${A4C_PORT}
EXPOSE ${A4C_DEBUG_PORT}

ENTRYPOINT cd ${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR} && ./alien4cloud.sh
