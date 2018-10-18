FROM alpine:3.8

ARG user_uid=1000
ARG user_gid=1000
ARG a4c_ver=2.0.0
ARG a4c_install_path=/opt
ARG a4c_src_dir=alien4cloud
ARG a4c_install_dir=a4c
ARG a4c_upv_ver=${a4c_ver}-UPV-1.0.0
ARG a4c_user=a4c
ARG a4c_settings_manager_ver=0.0.1-SNAPSHOT

ENV A4C_INSTALL_PATH=${a4c_install_path}
ENV A4C_SRC_DIR=${a4c_src_dir}
ENV A4C_INSTALL_DIR=${a4c_install_dir}
ENV A4C_USER=${a4c_user}
ENV A4C_SETTINGS_MANAGER_VER=${a4c_settings_manager_ver}

ENV A4C_PORT_HTTP=8088
ENV A4C_PORT_HTTPS=8443

ENV A4C_RUNTIME_DIR=/mnt/a4c_runtime_data
# This variable triggers the wipping of the old A4C
# runtime data! be sure you know what you are doing!
ENV A4C_RESET_CONFIG=false
ENV A4C_ADMIN_USERNAME=admin
ENV A4C_ADMIN_PASSWORD=admin
# A4C_ENABLE_SSL can be either "true" or "false"
# When it is true the non-secure link will be disabled
ENV A4C_ENABLE_SSL=true
ENV A4C_KEY_STORE_PASSWORD=default
ENV A4C_KEY_PASSWORD=default
ENV A4C_PEM_CA_CERT_FILE=ca.pem
ENV A4C_PEM_CA_KEY_FILE=ca-key.pem
ENV A4C_CERTS_ROOT_PATH=/certs

ADD indigodc-orchestrator-plugin "${a4c_install_path}/indigodc-orchestrator-plugin"
ADD a4c "${a4c_install_path}/${a4c_src_dir}"
ADD indigodc-2-a4c.py "${a4c_install_path}"
ADD alien4cloud-settings-manager "${a4c_install_path}/alien4cloud-settings-manager/"
  
RUN \
  # Install those dependencies that will be removed afterwards  
  apk --no-cache add --virtual build-dependencies \
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
  # Compile the A4C source code
  && cd "${a4c_install_path}/${a4c_src_dir}" \
  && mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true \
  # Get the precompiled version to obtain the init and config files
  && mkdir -p "${a4c_install_path}/${a4c_install_dir}"\
  && cd ${a4c_install_path} \
  && curl -k -O https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-dist/${a4c_ver}/alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && tar xvf alien4cloud-dist-${a4c_ver}-dist.tar.gz --strip 1 -C "${a4c_install_path}/${a4c_install_dir}" \
  && mv "${a4c_install_path}/${a4c_src_dir}/alien4cloud-ui/target/alien4cloud-ui-${a4c_upv_ver}-standalone.war" "${a4c_install_path}/${a4c_install_dir}/alien4cloud-ui-${a4c_ver}.war" \
  # Get and add the IndigoDC Tosca types
  && git clone -b devel_deep https://github.com/indigo-dc/tosca-types  ${a4c_install_path}/indigo-dc-tosca-types \
  && ls "${a4c_install_path}/a4c/" \
  && python3 ${a4c_install_path}/indigodc-2-a4c.py ${a4c_install_path}/indigo-dc-tosca-types/ < ${a4c_install_path}/indigo-dc-tosca-types/custom_types.yaml > ${a4c_install_path}/indigo-dc-tosca-types/tosca_types_alien.yaml \
  && cd "${a4c_install_path}/indigo-dc-tosca-types" \
  && zip -9 -r --exclude=custom* "${a4c_install_path}/${a4c_install_dir}/init/archives/indigo-dc-tosca-types.zip" *.* artifacts/ images/ \
  && rm -R "${a4c_install_path}/indigo-dc-tosca-types" \
  # Compile and install the plugin
  && cd "${a4c_install_path}/indigodc-orchestrator-plugin" \
  && mvn -e clean package \
  && cp ${a4c_install_path}/indigodc-orchestrator-plugin/target/alien4cloud-indigodc-provider-*.zip "${a4c_install_path}/${a4c_install_dir}/init/plugins/" \
  # Compile and install the a4c settings manager
  && cd "${a4c_install_path}/alien4cloud-settings-manager/" \
  && mvn clean package \
  && mv "${a4c_install_path}/alien4cloud-settings-manager/target/alien4cloud-settings-manager-${A4C_SETTINGS_MANAGER_VER}-jar-with-dependencies.jar" "${a4c_install_path}/${a4c_install_dir}/"\
  # Create a special user with limited access
  && addgroup -g ${user_gid} -S ${a4c_user} \
  && adduser -D -g "" -u ${user_uid} -G ${a4c_user} ${a4c_user} \
  && chown -R ${a4c_user}:${a4c_user} "${a4c_install_path}" \
  && chown -R ${a4c_user}:${a4c_user} "/home/${a4c_user}" \
  # Clean up the installed packages, files, everything
#  && npm list -g --depth=0. | awk -F ' ' '{print $2}' | awk -F '@' '{print $1}'  | xargs npm remove -g\
#  && rm -rf "${a4c_install_path}/alien4cloud-dist-${a4c_ver}-dist.tar.gz" \
#    "${a4c_install_path}/${a4c_src_dir}" ${a4c_install_path}/indigodc-orchestrator-plugin\
#    "${a4c_install_path}/alien4cloud-settings-manager/"\
#    /usr/lib/ruby \
#    "${a4c_install_path}/indigodc-2-a4c.py" \
#  && rm -rf $HOME/..?* $HOME/.[!.]* $HOME/*\
#  && apk del build-dependencies \
  # Install the a4c runtime dependencies
  && apk --no-cache add openjdk8-jre-base bash su-exec

EXPOSE ${A4C_PORT_HTTP}
EXPOSE ${A4C_PORT_HTTPS}

ENTRYPOINT \
  # Start a4c as non-root user
  cd "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}" \
  # But first generate the settings and the environment for secure connection
  && java -jar "alien4cloud-settings-manager-${A4C_SETTINGS_MANAGER_VER}-jar-with-dependencies.jar"\
  # And flush the buffers to avoid /usr/bin/env: bad interpreter: Text file busy
  && sync \
  && su ${A4C_USER} -s /bin/bash -c '"${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/alien4cloud.sh"'
