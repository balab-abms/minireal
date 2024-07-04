package org.balab.minireal.views.helpers;

import lombok.RequiredArgsConstructor;
import org.balab.minireal.middleware.kafka.KafkaTopicDeleter;
import org.balab.minireal.middleware.kafka.KafkaTopicMonitor;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SImRelatedHelpers
{
    private final KafkaTopicDeleter kafka_topic_deleter_service;
    private final KafkaAdmin kafkaAdmin;

    public void deleteThreadsTopics(String sim_session_token){
        KafkaTopicMonitor tick_topic_thread_monitor = new KafkaTopicMonitor(kafkaAdmin, "tick" + sim_session_token);
        tick_topic_thread_monitor.startMonitoring();

        KafkaTopicMonitor chart_topic_thread_monitor = new KafkaTopicMonitor(kafkaAdmin, "chart" + sim_session_token);
        chart_topic_thread_monitor.startMonitoring();

        // delete kafka listener threads
//        Thread tick_thread_del = getThreadByName("tick" + sim_session_token);
//        interruptThread(tick_thread_del);
//
//        Thread chart_thread_del = getThreadByName("chart" + sim_session_token);
//        interruptThread(chart_thread_del);

        // delete kafka topics
//        kafka_topic_deleter_service.deleteTopic("tick" + sim_session_token);
//        kafka_topic_deleter_service.deleteTopic("chart" + sim_session_token);
//        kafka_topic_deleter_service.deleteTopic("db" + sim_session_token);

//        System.out.println("tick = " + kafka_topic_deleter_service.isTopicEmpty("tick" + sim_session_token));
//        System.out.println("chart = " + kafka_topic_deleter_service.isTopicEmpty("chart" + sim_session_token));
    }

}
