package org.balab.minireal.middleware.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaAdmin;

public class KafkaTopicMonitor {

    private final KafkaAdmin kafkaAdmin;
    private final String topicName;
    private final ScheduledExecutorService executorService;

    public KafkaTopicMonitor(KafkaAdmin kafkaAdmin, String topicName) {
        this.kafkaAdmin = kafkaAdmin;
        this.topicName = topicName;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startMonitoring() {
        Runnable task = () -> {
            System.out.println("Running monitor service - " + topicName);
            if (isTopicEmpty(topicName)) {
                // stop the listner thread
                Thread tick_thread_del = getThreadByName(topicName);
                interruptThread(tick_thread_del);
                // delete the topic
                deleteTopic(topicName);
                // Stop the executor service when the topic is empty
                executorService.shutdown();
                System.out.println("Finished monitor service - " + topicName);
            }
        };

        executorService.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
    }

    public boolean isTopicEmpty(String topicName) {
        Map<String, Object> props = new HashMap<>(kafkaAdmin.getConfigurationProperties());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            TopicPartition partition0 = new TopicPartition(topicName, 0);
            consumer.assign(Collections.singletonList(partition0));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));
            return records.count() == 0;
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

    public Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName))
                return t;
        }
        return null;
    }

    public void interruptThread(Thread thread)
    {
        if(thread != null){
            thread.interrupt();
            System.out.println("Thread deleted");
        }
    }
}

