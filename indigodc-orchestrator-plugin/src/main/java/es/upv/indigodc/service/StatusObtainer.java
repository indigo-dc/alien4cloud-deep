package es.upv.indigodc.service;

import alien4cloud.paas.IPaaSCallback;
import alien4cloud.paas.model.DeploymentStatus;
import alien4cloud.paas.model.PaaSDeploymentContext;
import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.indigodc.SpringContext;
import es.upv.indigodc.Util;
import es.upv.indigodc.configuration.CloudConfigurationManager;
import es.upv.indigodc.service.model.DeploymentInfo;
import es.upv.indigodc.service.model.DeploymentInfoPair;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import es.upv.indigodc.service.model.StatusNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.oidc.api.Oidc;
import org.springframework.stereotype.Component;

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
  
  
 // @Autowired
  protected StatusManagerStorage statusManagerStorage;
  
  protected String token;
  
  protected IPaaSCallback<DeploymentStatus> callback;
  
  protected DeploymentInfo deploymentInfo;
  
  public StatusObtainer(String token, IPaaSCallback<DeploymentStatus> callback, final DeploymentInfo deploymentInfo) {
      this.callback = callback;
      this.token = token;
      this.deploymentInfo = deploymentInfo;
  }

//  @Async("statusObtainerScheduler")
//  @Scheduled(fixedDelay = 5000)
  public void run() {
         
          OrchestratorResponse response;
          try {
            response = orchestratorConnector.callDeploymentStatus(token, 
                cloudConfigurationManager.getCloudConfiguration(deploymentInfo.getOrchestratorId()),
                deploymentInfo.getOrchestratorDeploymentId());
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
