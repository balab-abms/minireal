package org.simreal.abm.middleware.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ManualKafkaListener implements Runnable {
    private final KafkaConsumer<String, Object> consumer;
    private final String topic;
    private String groupid;
    private volatile boolean running = true;
    private final Sinks.Many<Long> tick_publisher;

    public ManualKafkaListener(
            String brokers,
            String topic,
            Sinks.Many<Long> tick_publisher
    ) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "defaultGroup");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
        this.tick_publisher = tick_publisher;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(topic));
            while (running) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, Object> record : records) {
                    System.out.printf("%s - offset = %d, key = %s, value = %s%n", groupid, record.offset(), record.key(), record.value());
                    tick_publisher.emitNext((Long) record.value(), (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
                }
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        running = false;
    }
}
