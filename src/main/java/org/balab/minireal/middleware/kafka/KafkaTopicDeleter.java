package org.balab.minireal.middleware.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaTopicDeleter {

    private final KafkaAdmin kafkaAdmin;

    public KafkaTopicDeleter(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    public boolean isTopicEmpty(String topicName)
    {
        Map<String, Object> props = new HashMap<>(kafkaAdmin.getConfigurationProperties());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props))
        {
            TopicPartition partition0 = new TopicPartition(topicName, 0);
            consumer.assign(Collections.singletonList(partition0));
            consumer.seekToEnd(Collections.singletonList(partition0));
            long endPosition = consumer.position(partition0);
            consumer.seekToBeginning(Collections.singletonList(partition0));
            long startPosition = consumer.position(partition0);
            return endPosition == startPosition;
        }
    }


    public void deleteTopic(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singletonList(topicName));
            deleteTopicsResult.all().get();
        } catch (InterruptedException | ExecutionException e) {
            // handle exception
            System.out.println(e.getMessage());
        }
    }
}

