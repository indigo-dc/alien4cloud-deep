FROM alpine:3.8

ARG user_uid=1000
ARG user_gid=1000
ARG a4c_ver=2.0.0
ARG a4c_install_path=/opt
ARG a4c_src_dir=alien4cloud
ARG a4c_install_dir=a4c
ARG a4c_upv_ver=${a4c_ver}-UPV-1.0.0
ARG a4c_user=a4c

ENV A4C_PORT 8088
ENV A4C_VOLUME_DIR /mnt/a4c_instance_data

ENV A4C_INSTALL_PATH=${a4c_install_path}
ENV A4C_SRC_DIR=${a4c_src_dir}
ENV A4C_INSTALL_DIR=${a4c_install_dir}
ENV A4C_USER=${a4c_user}

ADD indigodc-orchestrator-plugin "${a4c_install_path}/indigodc-orchestrator-plugin"
ADD a4c "${a4c_install_path}/${a4c_src_dir}"
ADD indigodc-2-a4c.py "${a4c_install_path}"
  
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
  # Create a special user with limited access
  && addgroup -g ${user_gid} -S ${a4c_user} \
  && adduser -D -g "" -u ${user_uid} -G ${a4c_user} ${a4c_user} \
  && chown -R ${a4c_user}:${a4c_user} "${a4c_install_path}" \
  && chown -R ${a4c_user}:${a4c_user} "/home/${a4c_user}" \
  # Clean up the installed packages, files, everything
  && npm list -g --depth=0. | awk -F ' ' '{print $2}' | awk -F '@' '{print $1}'  | xargs npm remove -g\
  && rm -rf "${a4c_install_path}/alien4cloud-dist-${a4c_ver}-dist.tar.gz" \
    "${a4c_install_path}/${a4c_src_dir}" ${a4c_install_path}/indigodc-orchestrator-plugin\
    /usr/lib/ruby \
    "${a4c_install_path}/indigodc-2-a4c.py" \
  && rm -rf $HOME/..?* $HOME/.[!.]* $HOME/*\
  && apk del build-dependencies \
  # Install the a4c runtime dependencies
  && apk --no-cache add openjdk8-jre-base bash su-exec

EXPOSE ${A4C_PORT}

ENTRYPOINT mkdir -p ${A4C_VOLUME_DIR} \
  && chown -R ${A4C_USER}:${A4C_USER} "${A4C_VOLUME_DIR}" \
  # Replace the paths for the runtime folders so they point to ${A4C_VOLUME_DIR}
  && sed -i -e "s|alien: runtime|alien: ${A4C_VOLUME_DIR}/runtime|" "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/config/alien4cloud-config.yml" \  
  # Replace the paths for the logs (deployment and log4j) folders so they point to ${A4C_VOLUME_DIR}  
  && sed -i -e "s|<Property name=\"deployment_path\">deployment_logs</Property>|<Property name=\"deployment_path\">${A4C_VOLUME_DIR}/deployment_logs</Property>|" "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/config/log4j2.xml" \
  && sed -i -e "s|<File name=\"FILE\" fileName=\"logs/alien4cloud.log\">|<File name=\"FILE\" fileName=\"${A4C_VOLUME_DIR}/logs/alien4cloud.log\">|" "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/config/log4j2.xml" \
  # Replace the paths for the elastic search folders so they point to ${A4C_VOLUME_DIR}  
  && sed -i -e "s|runtime/elasticsearch/|${A4C_VOLUME_DIR}/runtime/elasticsearch/|g" "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/config/elasticsearch.yml" \
  && chown -R ${A4C_USER}:${A4C_USER} "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}" \
  # Start a4c as user not root
  && cd "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}" \
  # But first flush the buffers to avoid /usr/bin/env: bad interpreter: Text file busy
  && sync \
  && su ${A4C_USER} -s /bin/bash -c '"${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/alien4cloud.sh"'
