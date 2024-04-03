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
    Many<Object> display_publisher()
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

    @Bean
    Many<ArrayList<SimUI>> visuals_publisher()
    {
        Many<ArrayList<SimUI>> chatSink = Sinks.many().multicast().directBestEffort();
        return chatSink;
    }

    @Bean
    Many<Map<String, Object>> db_results_publisher()
    {
        Many<Map<String, Object>> chatSink = Sinks.many().multicast().directBestEffort();
        return chatSink;
    }

    @Bean
    Many<String> db_signals_publisher()
    {
        Many<String> chatSink = Sinks.many().multicast().directBestEffort();
        return chatSink;
    }

    // define subscriber Beans
    @Bean
    Flux<Object> display_subscriber(Many<Object> chatSink)
    {
        return chatSink.asFlux();
    }

    @Bean
    Flux<Long> tick_subscriber(Many<Long> chatSink)
    {
        return chatSink.asFlux();
    }

    @Bean
    Flux<ArrayList<SimUI>> visuals_subscriber(Many<ArrayList<SimUI>> chatSink)
    {
        return chatSink.asFlux();
    }

    @Bean
    Flux<Map<String, Object>> db_results_subscriber(Many<Map<String, Object>> chatSink)
    {
        return chatSink.asFlux();
    }

    @Bean
    Flux<String> db_signals_subscriber(Many<String> chatSink)
    {
        return chatSink.asFlux();
    }

}
