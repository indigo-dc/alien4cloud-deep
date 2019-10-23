package es.upv.indigodc;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** Plugin spring context configuration. */
@Configuration
@ComponentScan(basePackages = "es.upv.indigodc")
@EnableAsync
@EnableScheduling
public class PluginConfiguration {
    
//    @Bean(name = "statusObtainerScheduler")
//    @Scope("prototype")
//    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
//        ThreadPoolTaskScheduler sched = new ThreadPoolTaskScheduler();
//        sched.setPoolSize(1);
//        sched.initialize();
//        return sched;
//    }
    
//    @Bean(name = "statusObtainerScheduler")
//   // @Scope("prototype")
//    public Executor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(2);
//        executor.setMaxPoolSize(2);
//        executor.setQueueCapacity(500);
//        executor.setThreadNamePrefix("IndigoDCOrchestrator-");
//        executor.initialize();
//        return executor;
//    }
}
