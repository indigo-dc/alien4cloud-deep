FROM ubuntu:16.04

RUN apt-get update && apt-get -y install build-essential curl python3 openjdk-8-jdk ruby  git ruby-dev maven sudo bash apt-transport-https ca-certificates software-properties-common openssl
RUN curl -sL https://deb.nodesource.com/setup_9.x | bash -
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
RUN apt-get update && apt-get -y install nodejs docker-ce
#RUN ln -s /usr/bin/nodejs /usr/bin/node
RUN npm install -g bower
RUN npm -g install grunt-cli
RUN gem install compass
RUN npm install grunt-contrib-compass --save-dev

# These are development only libs, speeds up testing
RUN apt-get -y install vim htop

ARG user_uid=1000
ARG user_gid=1000
ENV A4C_PORT 8089
ENV A4C_VER 2.0.0-SNAPSHOT
ENV A4C_INSTALL_PATH /opt
ENV A4C_INSTALL_DIR a4c
ENV A4C_SRC_DIR a4c-src
ENV USER a4c

# Get IAM's certificate
#openssl s_client -connect iam.recas.ba.infn.it:443 -showcerts </dev/null 2>/dev/null|openssl x509 -outform PEM > ${A4C_INSTALL_PATH}/iamrecasbainfnit.pem

# Add certificate to java keystore
#sudo keytool -import -trustcacerts -file ${A4C_INSTALL_PATH}/iamrecasbainfnit.pem -alias iamrecasbainfnit -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts -storepass changeit

RUN addgroup --gid ${user_gid} ${USER} \
  && adduser --disabled-password --gecos "" --uid ${user_uid} --gid ${user_gid} ${USER}
RUN mkdir -p "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}"
#RUN mkdir -p "/home/${USER}"
ADD alien4cloud.sh "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}"
RUN chmod +x "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/alien4cloud.sh"

ADD config "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/config"
ADD init "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init"

# DEBUG - copy repos to avoid costly downloads
ADD a4c-tmp "/home/${USER}"

# Change perms from root to restricted user
RUN chown -R ${USER}:${USER} "${A4C_INSTALL_PATH}"
RUN chown -R ${USER}:${USER} "/home/${USER}"

USER ${USER}

# Allow bower to be executed as root
#RUN echo '{ "allow_root": true }' > /root/.bowerrc

RUN git clone https://github.com/alien4cloud/alien4cloud "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}"
RUN git clone -b a4c https://github.com/indigo-dc/tosca-types "${A4C_INSTALL_PATH}/indigo-dc-tosca-types"
RUN zip -j "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/indigo-dc-tosca-types.zip" "${A4C_INSTALL_PATH}/indigo-dc-tosca-types/custom_types.yaml"

WORKDIR "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}"

RUN mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

RUN cp "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-ui/target/alien4cloud-ui-${A4C_VER}-standalone.war" "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/alien4cloud-ui-standalone.war"

EXPOSE ${A4C_PORT}

ENTRYPOINT cd ${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR} && ./alien4cloud.sh
