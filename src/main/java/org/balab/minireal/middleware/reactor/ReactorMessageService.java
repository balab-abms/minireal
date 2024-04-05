package org.balab.minireal.middleware.reactor;

import com.example.application.sim_ui.SimUI;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import java.util.ArrayList;
import java.util.Map;

@Component
public class ReactorMessageService
{
    // define publisher Beans
    @Bean
    Many<Object> chart_publisher()
    {
        Many<Object> chatSink = Sinks.many().multicast().directBestEffort();
        return chatSink;
    }

    @Bean
    Many<Long> tick_publisher()
    {
        Many<Long> chatSink = Sinks.many().multicast().directBestEffort();
        return chatSink;
    }



    // define subscriber Beans
    @Bean
    Flux<Object> chart_subscriber(Many<Object> chatSink)
    {
        return chatSink.asFlux();
    }

    @Bean
    Flux<Long> tick_subscriber(Many<Long> chatSink)
    {
        return chatSink.asFlux();
    }


}
