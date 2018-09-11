Everything was created using the GUI, unless specified otherwise.

**General** (what I got out so far, true for all builds, please excuse any repetition):

- cannot set description from the GUI
- cannot eliminate constraint from inputs from GUI; Edit it from text mode and it is possible
- Warning! range must be set as two numbers separated by a comma, no white space allowed
- cannot set default values for inputs from GUI (accepted from text)
- Policy name editor is really stubborn and hardly accepts the name change

* ambertools.yaml

- cannot add output *ambertools_server*; Adding it using the text editor, the *properties* field is deleted when saving froom the GUI
- cannot set *ambertools_server->capabilities->endpoint->ports-><port _name>->source* from GUI;  Works from text editor only

* cellar_webapp_mem_aws.yaml

- cannot set *web_server->capabilities->endpoint->ports-><port _name>->source* from GUI; Works from text
- cannot set policy **deploy_on_aws** properties from GUI; If added from text editor, deleted from GUI
- cannot add output *server_url*; No support for concat with get_attribute
- Cannot define output *instance_creds*; Adding it using the text editor, the output is deleted when saving froom the GUI
- no inferfaces support for node **apache** from the GUI

* disvis.yaml

- Cannot define *instance_creds->value: { get_attribute: [ disvis_server, endpoint, credential, 0 ] }*; Using the text editor the output is deleted
- cannot set *web_server->capabilities->endpoint->ports-><port _name>->source* from GUI; Works from text

* docker_galaxy.yaml

- cannot set *web_server->capabilities->endpoint->ports-><port _name>->source* from GUI; Works from text
- cannot add output *galaxy_url*; No support for concat with get_attribute

* elastic_cluster.yaml

- cannot set *front_end_ip* of **wn_node**; no support for  *get_attribute* in GUI 
- cannot set *public_front_end_ip* of **wn_node**; no support for  *get_attribute* in GUI 
- cannot set *wn_ips* of **lrms_front_end**; no support for  *get_attribute* in GUI 
- GUI doesn't generate *marathon_credentials->token_type*, *chronos_credentials->token_type*, *mesos_credentials->token_type* for **elastic_cluster_front_end** and then when trying to save from the text view, it complains; Can set the missing fields from text editor
- cannot add output *cluster_creds->value: { get_attribute: [ lrms_server, endpoint, credential, 0 ] }*; No support for concat with get_attribute

* eubiosteo.yaml

- cannot set default values for inputs from GUI (accepted from text)
- cannot add output *instance_creds->value: { get_attribute: [ eubiosteo_server, endpoint, credential, 0 ] }*; No support for concat with get_attribute

* eubiosteo_client.yml

- cannot specify **my_onedata_storage** *credential->token* as TOSCA method *get_input*; When field filled, the property becomes a string e.g. "{ get_input: token }"
- cannot define requirement *local_storage*; Errors even when copy pasting from original file
- cannot add output *instance_creds*; No support for *concat* with *get_attribute*
- cannot detail requirement *local_storage* of **onedata_client_node** to include *properties* and *interfaces*

* eubiosteo_server.yaml

- cannot add output *cluster_creds*; No support for *concat* with *get_attribute*
- cannot set default values for inputs from GUI (accepted from text)
- cannot set **slurm_wn** *ports* *source* from GUI; Works from text editor only
- cannot set **slurm_server** *ports* *source* from GUI; Works from text editor only
- cannot set **slurm_front_end** *wn_ips* b/c *get_attribute* is not supported
- cannot detail requirement *local_storage* of **slurm_server** to include *properties* and *interfaces*
- cannot set *front_end_ip* of **wn_node** b/c *get_attribute* is not supported
- GUI doesn't generate *marathon_credentials->token_type*, *chronos_credentials->token_type*, *mesos_credentials->token_type* for **elastic_cluster_front_end** and then when trying to save from the text view, it complains; Can set the missing fields from text editor
- cannot add *interfaces* to **slurm_wn** via GUI editor
- cannot specify **my_onedata_storage** *credential->token* as TOSCA method *get_input*; When field filled, the property becomes a string e.g. "{ get_input: token }"
- cannot create requirement *local_storage* for **slurm_wn**
- **my_onedata_storage** requires *attachment* to **slurm_wn**

* galaxy_cluster.yaml

- cannot add output *galaxy_url*; No support for *concat* with *get_attribute*
- Cannot define output *cluster_creds* from the GUI; Using the text editor the output is deleted
- cannot set *front_end_ip* of **galaxy_wn** b/c *get_attribute* is not supported
- cannot specify *ports*->*source* for **lrms_server**; Works from text editor only
- cannot set *front_end_ip*, *public_front_end_ip* of **wn_node** b/c *get_attribute* is not supported
- cannot set *wn_ips* of **lrms_front_end** b/c *get_attribute* is not supported

* hadoop_cluster.yaml

- cannot set **hadoop_server** *ports* *source* from GUI; Works from text editor only
- cannot define output *cluster_creds* from the GUI; Adding it using the text editor, the output is deleted when saving froom the GUI
- cannot add output *dfs_url*; No support for *concat* with *get_attribute*
- cannot add output *yarn_url*; No support for *concat* with *get_attribute*
- cannot set *master_ip* of **hadoop_slave** b/c *get_attribute* is not supported

* indigo_grid_job.yaml

* indigo_job_onedata.yaml

- cannot define *environment_variable* *INPUT_ONEDATA_TOKEN* *OUTPUT_ONEDATA_TOKEN*, *INPUT_ONEDATA_SPACE*, *INPUT_PATH*, *OUTPUT_ONEDATA_SPACE*, *OUTPUT_PATH*, *OUTPUT_FILENAMES* of **chronos_job** b/c there is no way to specify a TOSCA method (in our case *get_input*)
- cannot add *artifacts* to **node_templates**
- cannot set requirement to *docker_runtime1*
- missing prerequisite *docker_runtime1 (tosca.nodes.indigo.Container.Runtime.Docker)* - *host (tosca.capabilities.Container)*
- missing prerequisite *chronos_job (tosca.nodes.indigo.Container.Application.Docker.Chronos)* -	*host (tosca.capabilities.Container.Docker)*

* indigo_marathon_app.yaml

- cannot add output *endpoint*; No support for *concat* with *get_attribute*
- cannot set *publish_ports*->*source* for **docker_runtime** b/c *get_input* method cannot be set from the GUI - if set it is considered as string
- cannot set *volumes* for **docker_runtime** b/c *concat* method cannot be set from the GUI - if set it is considered as string
- cannot create HostedOn relationship between tosca.nodes.indigo.Container.Application.Docker.Marathon and tosca.nodes.indigo.Container.Runtime.Docker
- invalid name for **marathon-app**; hyphens not allowed
- cannot set artifacts for **marathon-app** 

* jupyter_kube_cluster.yaml

- cannot add outputs *jupyterhub_url*, *dashboard_url*; No support for *concat* with *get_attribute*
- cannot set **lrms_server** *ports*->*source* from GUI; Works from text editor only
- cannot set *front_end_ip* of **wn_node** b/c *get_attribute* is not supported

* mesos_cluster.yaml

- cannot add outputs *marathon_endpoint*, *chronos_endpoint*; No support for *concat* with *get_attribute*
- cannot specify *ports*->*source* for **mesos_master_server**; Works from text editor only
- cannot set *mesos_masters_list* for **mesos_master** b/c *get_attribute* is not supported
- cannot set *master_ips*, *front_end_ip* for **mesos_slave** b/c *get_attribute* is not supported
- cannot set *master_ips* for **mesos_load_balancer** b/c *get_attribute* is not supported

* monitoring_simple.yaml

- cannot add output *instance_creds*; Adding it using the text editor, this output is deleted when saving froom the GUI
- cannot specify *ports*->*source* for **simple_server**; Works from text

* node_with_availability_zone.yaml

- cannot add output *node_creds*; Adding it using the text editor, this output is deleted when saving froom the GUI
- cannot specify *ports*->*source* for **simple_node**; Works from text editor only
- cannot add Policy *properties* from GUI; Adding it using the text editor, the *properties* field is deleted when saving froom the GUI

* onedata_client.yaml

- cannot specify *ports*->*source* for **simple_node**; Works from text editor only
- cannot create Requirement *local_storage* of **onedata_client_node**; Can only create from GUI from **my_onedata_storage** towards **onedata_client_node**, but cannot edit properties from GUI

* ophidia.yaml

- cannot set default values for inputs from GUI (accepted from text)
- cannot set *server_ip* for **ophidia_io** b/c *get_attribute* is not supported
- cannot specify *ports*->*source* for **simple_node**; Works from text editor only
- cannot create Requirement *local_storage* of **ophidiaserver**; Can only create from GUI from **ophidia_server_block_storage** towards **ophidiaserver**, but cannot edit properties from GUI
- cannot set *io_ips* for **ophidiafe** b/c *get_attribute* is not supported; Workaround in plugin set it as string and the plugin will detect and change it

* powerfit.yaml

- cannot add output *instance_creds*; Adding it using the text editor, this output is deleted when saving froom the GUI
- cannot specify *ports*->*source* for **powerfit_server**; Works from text editor only

* spark_mesos_cluster.yaml

- cannot specify *ports*->*source* for **mesos_master_server**, **mesos_slave_server**; Works from text editor only
- cannot set default values for inputs from GUI (accepted from text)
- cannot set *mesos_masters_list* for **mesos_master** b/c *get_attribute* is not supported
- cannot set *master_ips*, *front_end_ip* for **mesos_slave** b/c *get_attribute* is not supported
- cannot set *master_ips* for **mesos_load_balancer** b/c *get_attribute* is not supported
- cannot set *zookeeper_peers* for **spark_application** b/c *get_attribute* is not supported

* tensorflow_on_mesos_gpu.yaml

- cannot add outputs *marathon_endpoint*, *jupyter_endpoint*; No support for *concat* with *get_attribute*
- cannot set *master_ips* for **mesos_load_balancer** b/c *get_attribute* is not supported
- cannot set *master_ips*, *front_end_ip* for **mesos_slave** b/c *get_attribute* is not supported
- cannot set *mesos_masters_list* for **mesos_master** b/c *get_attribute* is not supported
- cannot set *marathon_host* for **tensorflow** b/c *get_attribute* is not supported

* web_mysql_tosca.yaml

- cannot create Requirement *local_storage* of **db_server**; Can only create from GUI from **my_block_storage** towards **db_server**, but cannot edit properties from GUI
- cannot set Artifacts for **test_db** from the GUI
- cannot create Interfaces for **test_db** from the GUI

* web_mysql_tosca_across_clouds.yaml

- same as for web_mysql_tosca.yaml
- cannot add Policies *properties* from GUI; Adding it using the text editor, the *properties* field is deleted when saving froom the GUI


