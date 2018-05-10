FROM alpine:3.7

RUN apk add --no-cache openjdk8-jre-base bash

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
ARG KEYSTOREFILE=/usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts
ARG KEYSTOREPASS=changeit

# ENV _JAVA_OPTIONS "-Djavax.net.ssl.trustStorePassword=${KEYSTOREPASS}"
# ENV JAVA_HOME "/usr/lib/jvm/java-8-openjdk-amd64"

# get the SSL certificate
RUN apk add --no-cache openssl \
  && openssl s_client -connect ${HOST}:${PORT} </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ${CERT_FILE} \
  && apk del openssl \
  && rm -rf /var/cache/apk/*

# create a keystore and import certificate
RUN keytool -import -noprompt -trustcacerts -alias ${HOST} -file ${CERT_FILE} -keystore ${KEYSTOREFILE} -storepass ${KEYSTOREPASS}

# verify we've got it.
# RUN keytool -list -v -keystore ${KEYSTOREFILE} -storepass ${KEYSTOREPASS} -alias ${HOST}

RUN addgroup -g ${user_gid} -S ${USER} \
  && adduser -D -g "" -u ${user_uid} -G ${USER} ${USER}

ADD indigodc-orchestrator-plugin "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"

WORKDIR "${A4C_INSTALL_PATH}"
RUN apk add --no-cache curl \
  && curl -k -O https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-dist/${a4c_ver}/alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && tar xvf alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && mv alien4cloud "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}" \
  && rm alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && apk del curl \
  && rm -rf /var/cache/apk/*
  
RUN apk add --no-cache git zip \
  && git clone -b indigo2 https://github.com/grycap/tosca "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  && cd "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  && zip -r "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/indigo-dc-tosca-types.zip" custom_types.yml images/ \
  && rm -R "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  && apk del git zip \
  && rm -rf /var/cache/apk/*

WORKDIR "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"
RUN  apk add --no-cache openjdk8 maven \
  && mvn -e clean package \
  && cp ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin/target/alien4cloud-indigodc-provider-*.zip "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/plugins/" \
  && rm -R $HOME/.m2 \
  && rm -R ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin \
  && apk del openjdk8 maven \
  && rm -rf /var/cache/apk/*

EXPOSE ${A4C_PORT}
EXPOSE ${A4C_DEBUG_PORT}

# Change perms from root to restricted user
RUN chown -R ${USER}:${USER} "${A4C_INSTALL_PATH}"
RUN chown -R ${USER}:${USER} "/home/${USER}"

USER ${USER}

ENTRYPOINT cd ${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR} && ./alien4cloud.sh
