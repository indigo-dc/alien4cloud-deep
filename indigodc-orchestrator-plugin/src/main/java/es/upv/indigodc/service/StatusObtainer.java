package es.upv.indigodc.service;

import alien4cloud.paas.IPaaSCallback;
import alien4cloud.paas.model.DeploymentStatus;
import es.upv.indigodc.Util;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.service.model.DeploymentInfo;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import es.upv.indigodc.service.model.StatusNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 * @author work
 */
@Component
@Scope("prototype")
@Slf4j
public class StatusObtainer implements Runnable {
  
//  @Autowired
//  @Qualifier("statusObtainerScheduler")
//  private ThreadPoolTaskScheduler executor;
  
  @Autowired
  protected CloudConfigurationManager cloudConfigurationManager;

  /** The service that executes the HTTP(S) calls to the Orchestrator. */
  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;
  
  
// // @Autowired
//  protected StatusManagerStorage statusManagerStorage;
    
  protected IPaaSCallback<DeploymentStatus> callback;
  
  protected DeploymentInfo deploymentInfo;
  
  public StatusObtainer(IPaaSCallback<DeploymentStatus> callback, final DeploymentInfo deploymentInfo) {
      this.callback = callback;

      this.deploymentInfo = deploymentInfo;
  }

//  @Async("statusObtainerScheduler")
//  @Scheduled(fixedDelay = 5000)
  public void run() {
         log.info("status obtainer");
          OrchestratorResponse response;
          try {
            response = orchestratorConnector.callDeploymentStatus(
                deploymentInfo.getOrchestratorDeploymentId());
            log.info(response.getResponse().toString());
            String statusTopologyDeployment = response.getStatusTopologyDeployment();
            callback.onSuccess(Util.indigoDcStatusToDeploymentStatus(statusTopologyDeployment));
          } catch (NoSuchFieldException e) {
            e.printStackTrace();
            this.callback.onFailure(e);
          } catch (IOException e) {
            this.callback.onFailure(e);
            e.printStackTrace();
          } catch (OrchestratorIamException e) {
              int code = e.getHttpCode();
              switch (code) {
                  case 401:
              }
            this.callback.onFailure(e);
            e.printStackTrace();
          } catch (StatusNotFoundException e) {
            this.callback.onFailure(e);
            e.printStackTrace();
          }



  }


}
