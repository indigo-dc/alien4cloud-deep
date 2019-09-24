package es.upv.indigodc.service;

import alien4cloud.paas.model.AbstractMonitorEvent;
import es.upv.indigodc.configuration.CloudConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventServiceTest {

    public static final String ORCHESTRATOR_ID = "orchestrator_id";

    @Test
    public void addEvent_bellow_limit_check_order() throws ParseException {
        EventService es = new EventService();
        es.subscribe(Mockito.mock(CloudConfiguration.class), ORCHESTRATOR_ID);
        DateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH");
        List<AbstractMonitorEvent> ameArr = createTestEvents(EventService.EVENT_QUEUE_MAX_SIZE - 2,
        		inputFormat.parse("1969.07.04 12"),
                ORCHESTRATOR_ID);
        for (int cnt = 0; cnt<ameArr.size(); ++cnt) {
            es.addEvent(ameArr.get(cnt));
        }

        AbstractMonitorEvent[] flushedEvents = es.flushEvents(
        		inputFormat.parse("1000.07.04 12"),
                EventService.EVENT_QUEUE_MAX_SIZE - 2);

        for (int cnt = 0; cnt<ameArr.size(); ++cnt) {
            Assertions.assertEquals(flushedEvents[cnt].getDeploymentId(),
                    ameArr.get(cnt).getDeploymentId());
            Assertions.assertEquals(flushedEvents[cnt].getOrchestratorId(),
                    ameArr.get(cnt).getOrchestratorId());
        }

        es.unsubscribe(ORCHESTRATOR_ID);

    }

    @Disabled
    protected List<AbstractMonitorEvent> createTestEvents(int numElems, Date d, String orchestratorId) {
        List<AbstractMonitorEvent> ameArr = new ArrayList<>();
        for (int cnt = 0; cnt<numElems; ++cnt) {
            AbstractMonitorEvent ame = Mockito.mock(AbstractMonitorEvent.class);
            ame.setDeploymentId(Integer.toString(cnt));
            ame.setOrchestratorId(orchestratorId);
            ame.setDate(d.getTime());
            ameArr.add(ame);
        }
        return ameArr;
    }

}
