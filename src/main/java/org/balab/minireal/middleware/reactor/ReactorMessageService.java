package org.balab.minireal.middleware.reactor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Component
public class ReactorMessageService
{
    // define publisher Beans
    @Bean
    Many<String> sim_session_del_publisher()
    {
        Many<String> chatSink = Sinks.many().multicast().directBestEffort();
        return chatSink;
    }




    // define subscriber Beans
    @Bean
    Flux<String> chart_subscriber(Many<String> chatSink)
    {
        return chatSink.asFlux();
    }



}
