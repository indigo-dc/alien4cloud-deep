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

RUN addgroup -g ${user_gid} -S ${USER} \
  && adduser -D -g "" -u ${user_uid} -G ${USER} ${USER}

ADD indigodc-orchestrator-plugin "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"

WORKDIR "${A4C_INSTALL_PATH}"
RUN apk add --no-cache curl git zip  openjdk8 maven \
  && curl -k -O https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-dist/${a4c_ver}/alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && tar xvf alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && mv alien4cloud "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}" \
  && rm alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && git clone -b indigo2 https://github.com/grycap/tosca "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  && cd "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  && zip -r "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/indigo-dc-tosca-types.zip" custom_types.yml images/ \
  && rm -R "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  && cd "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin" \
  && mvn -e clean package \
  && cp ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin/target/alien4cloud-indigodc-provider-*.zip "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/plugins/" \
  && mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false \
  && rm -R $HOME/.m2 \
  && rm -R ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin \
  && apk del curl git zip  openjdk8 maven \
  && rm -rf /var/cache/apk/*

EXPOSE ${A4C_PORT}
EXPOSE ${A4C_DEBUG_PORT}

# Change perms from root to restricted user
RUN chown -R ${USER}:${USER} "${A4C_INSTALL_PATH}"
RUN chown -R ${USER}:${USER} "/home/${USER}"

USER ${USER}

ENTRYPOINT cd ${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR} && ./alien4cloud.sh
