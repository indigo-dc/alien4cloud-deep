FROM alpine:3.9.4

ARG user_uid=1000
ARG user_gid=1000
ARG a4c_ver=2.1.0
ARG a4c_install_path=/opt
ARG a4c_src_dir=alien4cloud
ARG a4c_install_dir=a4c
ARG a4c_deep_ver=${a4c_ver}-DEEP-1.0.0-SNAPSHOT
ARG a4c_user=a4c
ARG a4c_settings_manager_ver=2.1.0
ARG tosca_normative_url=https://raw.githubusercontent.com/openstack/tosca-parser/master/toscaparser/elements/TOSCA_definition_1_0.yaml
ARG tosca_normative_version=1.0.0
ARG tosca_normative_name=normative-types
ARG tosca_indigo_version=3.0.0
ARG tosca_indigo_types_branch=v4.0.0
ARG a4c_deep_url=https://github.com/indigo-dc/alien4cloud
ARG spring_social_oidc_url=https://github.com/indigo-dc/spring-social-oidc
ARG spring_social_oidc_branch=features/issue-4
ARG a4c_deep_branch=deep-dev-UPV
ARG a4c_binary_dist_url=https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-dist/${a4c_ver}/alien4cloud-dist-${a4c_ver}-dist.tar.gz

ENV A4C_SH_NAME=alien4cloud.sh

ENV A4C_CERTS_ROOT_PATH=/certs

ENV A4C_INSTALL_PATH=${a4c_install_path}
ENV A4C_SRC_DIR=${a4c_src_dir}
ENV A4C_INSTALL_DIR=${a4c_install_dir}
ENV A4C_USER=${a4c_user}
ENV A4C_SETTINGS_MANAGER_VER=${a4c_settings_manager_ver}

ENV A4C_PORT_HTTP=8088
ENV A4C_PORT_HTTPS=8443

ENV A4C_SPRING_OIDC_ISSUER=https://iam.deep-hybrid-datacloud.eu
ENV A4C_SPRING_OIDC_CLIENT_ID=<none>
ENV A4C_SPRING_OIDC_CLIENT_SECRET=<none>
# Use CSV style approach, that is list of elements with single quote, separated by one and only one comma
# eg 'ADMIN','APPLICATION MANAGER','DEV, OPS'
# at least one element is necessary
ENV A4C_SPRING_OIDC_ROLES=<none>

ENV A4C_ORCHESTRATOR_URL=https://deep-paas.cloud.ba.infn.it/orchestrator
ENV A4C_ORCHESTRATOR_ENABLE_KEYSTORE=false
ENV A4C_ORCHESTRATOR_KEYSTORE_PASSWORD=default
ENV A4C_ORCHESTRATOR_KEY_PASSWORD=default
ENV A4C_ORCHESTRATOR_PEM_CERT_FILE=orchestrator_ca.pem
ENV A4C_ORCHESTRATOR_PEM_KEY_FILE=orchestrator_ca-key.pem

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
ENV A4C_PEM_CERT_FILE=ca.pem
ENV A4C_PEM_KEY_FILE=ca.key

ENV A4C_JAVA_XMX_MEMO=4g

ADD indigodc-orchestrator-plugin "${a4c_install_path}/indigodc-orchestrator-plugin"
# ADD a4c "${a4c_install_path}/${a4c_src_dir}"
ADD scripts/custom_types-2-a4c.py "${a4c_install_path}"
ADD scripts/normative_tosca-2-a4c.py "${a4c_install_path}"
ADD alien4cloud-settings-manager "${a4c_install_path}/alien4cloud-settings-manager/"

RUN \
  # Install those dependencies that will be removed afterwards
  apk --no-cache add --virtual build-dependencies \
    bash su-exec libcurl libssh2 curl expat pcre2 git zip libxau libbsd \
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
  # Compile and install locally the Spring Social OIDC plugin
  && git clone --single-branch  --branch ${spring_social_oidc_branch} ${spring_social_oidc_url} "${a4c_install_path}/spring_social_oidc" \
  && cd "${a4c_install_path}/spring_social_oidc" \
  && mvn -U clean install \
  # Compile the A4C source code
  && git clone --single-branch  --branch ${a4c_deep_branch} ${a4c_deep_url} "${a4c_install_path}/${a4c_src_dir}" \
  && cd "${a4c_install_path}/${a4c_src_dir}" \
  && mvn -U clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true \
  # Get the precompiled version to obtain the init and config files
  && cd ${a4c_install_path} \
  && curl -k -O ${a4c_binary_dist_url} \
  && mkdir ${a4c_install_path}/${a4c_install_dir} \
  && tar xvf alien4cloud-dist-${a4c_ver}-dist.tar.gz --strip 1 -C "${a4c_install_path}/${a4c_install_dir}" \
  && rm alien4cloud-dist-${a4c_ver}-dist.tar.gz \
  && rm ${a4c_install_path}/${a4c_install_dir}/init/archives/* ${a4c_install_path}/${a4c_install_dir}/init/plugins/* \
  && mv "${a4c_install_path}/${a4c_src_dir}/alien4cloud-ui/target/alien4cloud-ui-${a4c_deep_ver}-standalone.war" \
    "${a4c_install_path}/${a4c_install_dir}/alien4cloud-standalone.war" \
  && rm -rf "${a4c_install_path}/${a4c_src_dir}" \
  # Get and add the normative Tosca types
  && curl -k -o ${a4c_install_path}/TOSCA_normative_types_orig.yaml ${tosca_normative_url}  \
  && python3 ${a4c_install_path}/normative_tosca-2-a4c.py ${tosca_normative_version} ${tosca_normative_name} \
    < ${a4c_install_path}/TOSCA_normative_types_orig.yaml \
    > ${a4c_install_path}/TOSCA_normative_types_a4c.yaml \
  && cd "${a4c_install_path}" \
  && zip -9 -r "${a4c_install_path}/${a4c_install_dir}/init/archives/TOSCA_normative_types_a4c.zip" TOSCA_normative_types_a4c.yaml \
  && rm TOSCA_normative_types_* \
  # Get and add the IndigoDC Tosca types
  && git clone -b ${tosca_indigo_types_branch} https://github.com/indigo-dc/tosca-types  ${a4c_install_path}/indigo-dc-tosca-types \
  && python3 ${a4c_install_path}/custom_types-2-a4c.py  ${a4c_install_path}/indigo-dc-tosca-types/  ${tosca_indigo_version}  ${tosca_normative_name}:${tosca_normative_version} \
    < ${a4c_install_path}/indigo-dc-tosca-types/custom_types.yaml \
    > ${a4c_install_path}/indigo-dc-tosca-types/tosca_types_alien.yaml \
  && rm -rf ${a4c_install_path}/indigo-dc-tosca-types/custom_types.yaml \
  && cd "${a4c_install_path}/indigo-dc-tosca-types" \
  && zip -9 -r --exclude=custom* "${a4c_install_path}/${a4c_install_dir}/init/archives/indigo-dc-tosca-types.zip" *.* artifacts/ images/ \
  && rm -R "${a4c_install_path}/indigo-dc-tosca-types" \
  # Compile and install the plugin
  && cd "${a4c_install_path}/indigodc-orchestrator-plugin" \
  && mvn -U -e clean package \
  && cp ${a4c_install_path}/indigodc-orchestrator-plugin/target/alien4cloud-indigodc-provider.zip \
    "${a4c_install_path}/${a4c_install_dir}/init/plugins/" \
  # Compile and install the a4c settings manager
  && cd "${a4c_install_path}/alien4cloud-settings-manager/" \
  && mvn clean package \
  && mv "${a4c_install_path}/alien4cloud-settings-manager/target/alien4cloud-settings-manager-${A4C_SETTINGS_MANAGER_VER}-jar-with-dependencies.jar" \
    "${a4c_install_path}/${a4c_install_dir}/" \
  # Create a special user with limited access
  && addgroup -g ${user_gid} -S ${a4c_user} \
  && adduser -D -g "" -u ${user_uid} -G ${a4c_user} ${a4c_user} \
  && chown -R ${a4c_user}:${a4c_user} "${a4c_install_path}" \
  && chown -R ${a4c_user}:${a4c_user} "/home/${a4c_user}" \
  && chmod +x "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/${A4C_SH_NAME}" \
  # Clean up the installed packages, files, everything
  && npm list -g --depth=0. | awk -F ' ' '{print $2}' | awk -F '@' '{print $1}'  | xargs npm remove -g \
  && rm -rf ${a4c_install_path}/indigodc-orchestrator-plugin \
    "${a4c_install_path}/alien4cloud-settings-manager/"\
    /usr/lib/ruby \
    "${a4c_install_path}/indigodc-2-a4c.py" \
  && rm -rf $HOME/..?* $HOME/.[!.]* $HOME/* \
  && apk del build-dependencies  \
  # Install the a4c runtime dependencies
  && apk --no-cache add openjdk8-jre-base bash su-exec nss

EXPOSE ${A4C_PORT_HTTP}
EXPOSE ${A4C_PORT_HTTPS}

ENTRYPOINT \
  # Start a4c as non-root user
  cd "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}" \
  # But first generate the settings and the environment for secure connection
  && java -jar "alien4cloud-settings-manager-${A4C_SETTINGS_MANAGER_VER}-jar-with-dependencies.jar"\
  # And flush the buffers to avoid /usr/bin/env: bad interpreter: Text file busy
  && sync \
  && chmod +x "${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/${A4C_SH_NAME}" \
  && su ${A4C_USER} -s /bin/bash -c '"${A4C_INSTALL_PATH}/${A4C_INSTALL_DIR}/${A4C_SH_NAME}" "${A4C_JAVA_XMX_MEMO}"'
