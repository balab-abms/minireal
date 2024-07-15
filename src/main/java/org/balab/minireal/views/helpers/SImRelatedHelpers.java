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
    }

}
