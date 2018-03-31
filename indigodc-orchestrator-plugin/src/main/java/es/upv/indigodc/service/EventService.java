package es.upv.indigodc.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import alien4cloud.paas.model.AbstractMonitorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventService {

  private final Queue<AbstractMonitorEvent> eventQueue = new LinkedList<>();;
  
  public void subscribe(String apiURL) {
    
  }
  
  /**
   * Poll all events from the IndigoDC orchestrator then flush the Queue.
   * @return All events in the Queue.
   */
  public AbstractMonitorEvent[] flushEvents() {
      ArrayList<AbstractMonitorEvent> events = new ArrayList<>(eventQueue.size());
      AbstractMonitorEvent e;
      while (( e = eventQueue.poll()) != null) {
          events.add(e);
      }
      return events.toArray(new AbstractMonitorEvent[events.size()]);
  }
  
}
