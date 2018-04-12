FROM ubuntu:16.04

RUN apt-get update && apt-get -y install curl openjdk-8-jdk-headless git maven

# These are development only libs, speeds up testing
RUN apt-get -y install build-essential vim htop python3 sudo bash

ARG user_uid=1000
ARG user_gid=1000
ENV A4C_PORT 8088
ENV A4C_DEBUG_PORT 5005
ENV A4C_VER 2.0.0-SNAPSHOT
ENV A4C_INSTALL_PATH /opt
ENV A4C_INSTALL_DIR a4c
ENV A4C_SRC_DIR a4c-src
ENV USER a4c

RUN addgroup --gid ${user_gid} ${USER} \
  && adduser --disabled-password --gecos "" --uid ${user_uid} --gid ${user_gid} ${USER}
#RUN mkdir -p "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}"
#RUN mkdir -p "/home/${USER}"
#ADD alien4cloud.sh "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}"
#RUN chmod +x "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/alien4cloud.sh"

#ADD config "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/config"
#ADD init "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init"
RUN mkdir -p "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/"
RUN mkdir -p "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/plugins/"
ADD init/archives/indigo-types_1.1.0.zip "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/"
ADD indigodc-orchestrator-plugin "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"

# DEBUG - copy repos to avoid costly downloads
#ADD a4c-tmp "/home/${USER}"

# Change perms from root to restricted user
RUN chown -R ${USER}:${USER} "${A4C_INSTALL_PATH}"
RUN chown -R ${USER}:${USER} "/home/${USER}"

USER ${USER}

# Allow bower to be executed as root
#RUN echo '{ "allow_root": true }' > /root/.bowerrc

#RUN git clone https://github.com/alien4cloud/alien4cloud "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}"
#RUN git clone -b a4c https://github.com/indigo-dc/tosca-types "${A4C_INSTALL_PATH}/indigo-dc-tosca-types"
RUN git clone -b indigo https://github.com/grycap/tosca "${A4C_INSTALL_PATH}/indigo-dc-tosca-types"

#WORKDIR "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}"

#RUN mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true
#RUN cp "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-ui/target/alien4cloud-ui-${A4C_VER}-standalone.war" "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/alien4cloud-ui-standalone.war"

WORKDIR "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"
RUN mvn clean package
RUN cp ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin/target/alien4cloud-indigodc-provider-*.zip "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/plugins/"

WORKDIR "${A4C_INSTALL_PATH}"
RUN curl -O https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-dist/1.4.3.2/alien4cloud-dist-1.4.3.2-dist.tar.gz
RUN tar xvf alien4cloud-dist-1.4.3.2-dist.tar.gz

RUN mv alien4cloud "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}"
#RUN zip -j "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/indigo-dc-tosca-types.zip" "${A4C_INSTALL_PATH}/indigo-dc-tosca-types/custom_types.yaml" "${A4C_INSTALL_PATH}/indigo-dc-tosca-types/images"

EXPOSE ${A4C_PORT}
EXPOSE ${A4C_DEBUG_PORT}

ENTRYPOINT cd ${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR} && ./alien4cloud.sh
