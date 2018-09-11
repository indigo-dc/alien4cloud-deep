FROM alpine:3.8

ARG user_uid=1000
ARG user_gid=1000
ARG a4c_ver=2.0.0
ENV A4C_PORT 8088
ENV A4C_INSTALL_PATH /opt
ENV A4C_INSTALL_DIR a4c
ENV A4C_SRC_DIR alien4cloud-2.0.0
ENV A4C_UPV_VER 2.0.0-UPV-1.0.0
ENV USER a4c

ADD indigodc-orchestrator-plugin "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin"
ADD a4c "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}"
ADD indigodc-2-a4c.py "${A4C_INSTALL_PATH}"

RUN apk add --no-cache openjdk8-jre-base bash \
  # Install those dependencies that will be removed afterwards
  && apk --no-cache add --virtual build-dependencies \
    libcurl libssh2 curl expat pcre2 git zip libxau libbsd \
    libxdmcp libxcb libx11 libxcomposite libxext libxi \
    libxrender libxtst alsa-lib libbz2 libpng freetype \
    giflib openjdk8-jre openjdk8 maven gdbm xz-libs python3 \
    yaml py3-yaml ruby-dev nodejs git npm gcc make libffi-dev \
    build-base ruby-rdoc python \
  # Install dependencies used for A4C compilation process
  && npm install -g bower \
  && npm -g install grunt-cli \
  && gem install compass \
  && npm install grunt-contrib-compass --save-dev\
  && echo '{ "allow_root": true }' > /root/.bowerrc\
  && cat /root/.bowerrc \
#  # Get A4C source code
#  && cd "${A4C_INSTALL_PATH}"\
#  && curl -sL https://github.com/alien4cloud/alien4cloud/archive/2.0.0.tar.gz  | tar xvz \
#  # Add mods to the A4C source code
#  && sed -i '110iuser.setPlainPassword(password);' "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-security/src/main/java/alien4cloud/security/users/UserService.java"\
#  && sed -i '129iuser.setPlainPassword(userUpdateRequest.getPassword());' "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-security/src/main/java/alien4cloud/security/users/UserService.java"\
#  && sed -i '34iprivate String plainPassword;' "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-security/src/main/java/alien4cloud/security/model/User.java" \
  # Compile the A4C source code
  && cd "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}"\
  && mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true\
  # Get the precompiled version to obtain the init and config files
  && mkdir -p "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}"\
  && cd ${A4C_INSTALL_PATH} \
  && curl -k -O https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-dist/${a4c_ver}/alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && tar xvf alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && mv alien4cloud/* "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}" \
  && cp "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-ui/target/alien4cloud-ui-${A4C_UPV_VER}-standalone.war" "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/alien4cloud-ui-2.0.0.war" \
  # Get and add the IndigoDC Tosca types
  && git clone -b devel_deep https://github.com/indigo-dc/tosca-types  ${A4C_INSTALL_PATH}/indigo-dc-tosca-types \
  && ls "${A4C_INSTALL_PATH}/a4c/" \
  && python3 ${A4C_INSTALL_PATH}/indigodc-2-a4c.py ${A4C_INSTALL_PATH}/indigo-dc-tosca-types/ < ${A4C_INSTALL_PATH}/indigo-dc-tosca-types/custom_types.yaml > ${A4C_INSTALL_PATH}/indigo-dc-tosca-types/tosca_types_alien.yaml \
  && cd "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  && zip -9 -r --exclude=custom* "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/archives/indigo-dc-tosca-types.zip" *.* artifacts/ images/ \
  && rm -R "${A4C_INSTALL_PATH}/indigo-dc-tosca-types" \
  # Compile and install the plugin
  && cd "${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin" \
  && mvn -e clean package \
  && cp ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin/target/alien4cloud-indigodc-provider-*.zip "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/init/plugins/" \
  # Create a special user with limited access
  && addgroup -g ${user_gid} -S ${USER} \
  && adduser -D -g "" -u ${user_uid} -G ${USER} ${USER} \
  && chown -R ${USER}:${USER} "${A4C_INSTALL_PATH}" \
  && chown -R ${USER}:${USER} "/home/${USER}" \
#RUN  cat "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-security/src/main/java/alien4cloud/security/users/UserService.java"\
#  && cat "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}/alien4cloud-security/src/main/java/alien4cloud/security/model/User.java"
  # Clean up the installed packages, files, everything
  && npm list -g --depth=0. | awk -F ' ' '{print $2}' | awk -F '@' '{print $1}'  | xargs npm remove -g\
  && rm -rf "${A4C_INSTALL_PATH}/alien4cloud-dist-${a4c_ver}-dist.tar.gz" \
    "${A4C_INSTALL_PATH}/${A4C_SRC_DIR}" ${A4C_INSTALL_PATH}/indigodc-orchestrator-plugin\
    /usr/lib/ruby "${A4C_INSTALL_PATH}/alien4cloud"\
    "${A4C_INSTALL_PATH}/indigodc-2-a4c.py" \
  && rm -rf $HOME/..?* $HOME/.[!.]* $HOME/*\
  && apk del build-dependencies

EXPOSE ${A4C_PORT}

USER ${USER}

ENTRYPOINT cd ${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR} && ./alien4cloud.sh
