package es.upv.indigodc.service;

import alien4cloud.paas.model.AbstractMonitorEvent;
import alien4cloud.paas.model.PaaSDeploymentStatusMonitorEvent;
import alien4cloud.security.model.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.upv.indigodc.Util;
import es.upv.indigodc.configuration.CloudConfiguration;
import es.upv.indigodc.service.model.AlienDeploymentMapping;
import es.upv.indigodc.service.model.OrchestratorIamException;
import es.upv.indigodc.service.model.OrchestratorResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventService {

  public static final int EVENT_QUEUE_MAX_SIZE = 1000;

  @Autowired
  private UserService userService;

  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;

  @Autowired
  @Qualifier("mapping-service")
  private MappingService mappingService;

  private final Queue<AbstractMonitorEvent> eventQueue = new ConcurrentLinkedQueue<>();
  private ScheduledExecutorService executor;

  /**
   * Subscribe to the event service that loads the events from the orchestrator by polling it every
   * X seconds.
   *
   * @param cc the cloud configuration used for the orchestrator instance
   */
  public void subscribe(CloudConfiguration cc) {
    // if (executor != null)
    // executor.shutdown();
    // executor = Executors.newScheduledThreadPool(1);
    //
    // executor.scheduleWithFixedDelay(
    // new OrchestratorPollRunnable(orchestratorConnector, cc, eventQueue,
    // userService.getCurrentUser()),
    // 0,
    // cc.getOrchestratorPollInterval(),
    // TimeUnit.SECONDS);
  }

  public void unsubscribe() {
    executor.shutdown();
    eventQueue.clear();
  }

  /**
   * Poll all events from the IndigoDC orchestrator then flush the Queue.
   *
   * @return All events in the Queue.
   */
  public AbstractMonitorEvent[] flushEvents(Date date, int maxEvents) {
    ArrayList<AbstractMonitorEvent> events = new ArrayList<>(eventQueue.size());
    AbstractMonitorEvent ame = eventQueue.poll();
    int count = 0;
    while (ame != null && ame.getDate() > date.getTime() && count < maxEvents) {
      events.add(ame);
      ++count;
      ame = eventQueue.poll();
    }
    //// It doesn matter if we lose some stats
    // eventQueue.clear();
    return events.toArray(new AbstractMonitorEvent[events.size()]);
  }

  protected static class OrchestratorPollRunnable implements Runnable {

    private OrchestratorConnector orchestratorConnector;

    private final CloudConfiguration cc;

    private Queue<AbstractMonitorEvent> eventQueue;

    private MappingService mappingService;

    private User user;

    public OrchestratorPollRunnable(OrchestratorConnector orchestratorConnector,
        final CloudConfiguration cc, Queue<AbstractMonitorEvent> eventQueue, User user) {
      this.orchestratorConnector = orchestratorConnector;
      this.cc = cc;
      this.eventQueue = eventQueue;
      this.user = user;
    }

    @Override
    public void run() {
      log.info("Event Service get deployments");
      try {
        OrchestratorResponse response = orchestratorConnector.callGetDeployments(cc,
            user.getUsername(), user.getPlainPassword());
        if (response.isCodeOk()) {
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
          JsonNode root = objectMapper.readTree(response.getResponse().toString());

          List<JsonNode> deployments = root.findValues("content");
          // For each deployment, add its status to the list
          for (JsonNode jsonNode : deployments) {
            PaaSDeploymentStatusMonitorEvent statusEv = new PaaSDeploymentStatusMonitorEvent();
            statusEv.setDeploymentStatus(Util
                .indigoDcStatusToDeploymentStatus(jsonNode.findValue("status").get(0).asText()));
            String orchestratorDeploymentUuid = jsonNode.findValue("uuid").get(0).asText();
            AlienDeploymentMapping alienDeploymentMapping =
                mappingService.getByOrchestratorUuidDeployment(orchestratorDeploymentUuid);
            if (alienDeploymentMapping != null) {
              statusEv.setDeploymentId(alienDeploymentMapping.getDeploymentId());
              statusEv.setOrchestratorId(alienDeploymentMapping.getOrchetratorId());
              // Get date/time from ISO to long since 1.1.1970
              statusEv.setDate(LocalDateTime.parse(jsonNode.findValue("updateTime").get(0).asText())
                  .toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli());
              if (eventQueue.size() < EVENT_QUEUE_MAX_SIZE) {
                eventQueue.add(statusEv);
              }
            } else {
              log.warn(String.format("Deployment with ID %s not found in the A4C DB",
                  orchestratorDeploymentUuid));
            }
          }
        } else {
          log.error("Error calling deployments");
        }
        // if (vals.size() > 0)
        // vals.get(0).asText();
        // else
        // throw new NoSuchElementException("The response for deployment doesn't contain an
        // uuid field");

        log.info("Event Service get deployments 2");
      } catch (NoSuchFieldException ex) {
        ex.printStackTrace();
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (OrchestratorIamException ex) {
        ex.printStackTrace();
      } catch (Error | Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
