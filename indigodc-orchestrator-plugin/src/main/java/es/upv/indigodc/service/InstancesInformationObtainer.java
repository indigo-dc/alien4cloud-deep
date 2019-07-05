package es.upv.indigodc.service;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
@EnableScheduling
public class InstancesInformationObtainer implements Runnable {

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

}
