package es.upv.indigodc.service;

import alien4cloud.paas.model.AbstractMonitorEvent;
import es.upv.indigodc.configuration.CloudConfiguration;

import java.util.ArrayList;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventService {

  public static final int EVENT_QUEUE_MAX_SIZE = 1000;


  @Autowired
  @Qualifier("orchestrator-connector")
  private OrchestratorConnector orchestratorConnector;

  @Autowired
  @Qualifier("mapping-service")
  private MappingService mappingService;

  private final Buffer eventQueue;
  //private ScheduledExecutorService executor;

  public EventService() {
    eventQueue = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(EVENT_QUEUE_MAX_SIZE));
  }

  /**
   * Subscribe to the event service that loads the events from the orchestrator by polling it every
   * X seconds.
   *
   * @param cc the cloud configuration used for the orchestrator instance
   * @param orchestratorId The ID of the orchestrator that will be traced
   */
  public void subscribe(CloudConfiguration cc, String orchestratorId) {
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

  /**
   * Stop adding events froman orchestrator instance
   * @param orchestratorId The ID of the orchestrator that won't be traced anymore
   */
  public void unsubscribe(String orchestratorId) {
    //executor.shutdown();
    //eventQueue.clear();
  }

  /**
   * Add a monitor event to the list of existing events
   * @param monitorEvent The event that should be added in the queue
   */
  public void addEvent(AbstractMonitorEvent monitorEvent) {
    eventQueue.add(monitorEvent);
  }

  /**
   * Poll all events from the IndigoDC orchestrator then flush the Queue.
   *
   * @return All events in the Queue.
   */
  public AbstractMonitorEvent[] flushEvents(Date date, int maxEvents) {
    ArrayList<AbstractMonitorEvent> events = new ArrayList<>(maxEvents);
    AbstractMonitorEvent[] ameArr = new AbstractMonitorEvent[eventQueue.size()];
    eventQueue.toArray(ameArr);
    if (ameArr.length > 0) {
      int count = 0;
      while (count < eventQueue.size() && count < maxEvents) {
          AbstractMonitorEvent ame = ameArr[count];
          if (ame.getDate() > date.getTime())
        	  events.add(ame);
        ++count;
      }
    }
    //// It doesn matter if we lose some stats
    // eventQueue.clear();
    return events.toArray(new AbstractMonitorEvent[events.size()]);
  }

//  protected static class OrchestratorPollRunnable implements Runnable {
//
//    private OrchestratorConnector orchestratorConnector;
//
//    private final CloudConfiguration cc;
//
//    private Queue<AbstractMonitorEvent> eventQueue;
//
//    private MappingService mappingService;
//
//    private User user;
//
//    public OrchestratorPollRunnable(OrchestratorConnector orchestratorConnector,
//        final CloudConfiguration cc, Queue<AbstractMonitorEvent> eventQueue, User user) {
//      this.orchestratorConnector = orchestratorConnector;
//      this.cc = cc;
//      this.eventQueue = eventQueue;
//      this.user = user;
//    }
//
//    @Override
//    public void run() {
////      log.info("Event Service get deployments");
////      try {
////        OrchestratorResponse response = orchestratorConnector.callGetDeployments(cc,
////            user.getUsername(), user.getPlainPassword());
////        if (response.isCodeOk()) {
////          ObjectMapper objectMapper = new ObjectMapper();
////          objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
////          JsonNode root = objectMapper.readTree(response.getResponse().toString());
////
////          List<JsonNode> deployments = root.findValues("content");
////          // For each deployment, add its status to the list
////          for (JsonNode jsonNode : deployments) {
////            PaaSDeploymentStatusMonitorEvent statusEv = new PaaSDeploymentStatusMonitorEvent();
////            statusEv.setDeploymentStatus(Util
////                .indigoDcStatusToDeploymentStatus(jsonNode.findValue("status").get(0).asText()));
////            String orchestratorDeploymentUuid = jsonNode.findValue("uuid").get(0).asText();
////            AlienDeploymentMapping alienDeploymentMapping =
////                mappingService.getByOrchestratorUuidDeployment(orchestratorDeploymentUuid);
////            if (alienDeploymentMapping != null) {
////              statusEv.setDeploymentId(alienDeploymentMapping.getDeploymentId());
////              statusEv.setOrchestratorId(alienDeploymentMapping.getOrchetratorId());
////              // Get date/time from ISO to long since 1.1.1970
////              statusEv.setDate(LocalDateTime.parse(jsonNode.findValue("updateTime").get(0).asText())
////                  .toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli());
////              if (eventQueue.size() < EVENT_QUEUE_MAX_SIZE) {
////                eventQueue.add(statusEv);
////              }
////            } else {
////              log.warn(String.format("Deployment with ID %s not found in the A4C DB",
////                  orchestratorDeploymentUuid));
////            }
////          }
////        } else {
////          log.error("Error calling deployments");
////        }
////        // if (vals.size() > 0)
////        // vals.get(0).asText();
////        // else
////        // throw new NoSuchElementException("The response for deployment doesn't contain an
////        // uuid field");
////
////        log.info("Event Service get deployments 2");
////      } catch (NoSuchFieldException ex) {
////        ex.printStackTrace();
////      } catch (IOException ex) {
////        ex.printStackTrace();
////      } catch (OrchestratorIamException ex) {
////        ex.printStackTrace();
////      } catch (Error | Exception ex) {
////        ex.printStackTrace();
////      }
//    }
//  }
}
