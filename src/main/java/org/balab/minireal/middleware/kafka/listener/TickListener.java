package org.balab.minireal.middleware.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import reactor.core.publisher.Sinks;

public class TickListener {
    private String myGroupId;
    private String myUserId;
    private final Sinks.Many<Long> tick_publisher;


    public TickListener(
            Sinks.Many<Long> tick_publisher,
            String myGroupId,
            String myUserId
    ) {
        this.tick_publisher = tick_publisher;
        this.myGroupId = myGroupId;
        this.myUserId = myUserId;
        System.out.println("Tick Listener created.");
    }

    @KafkaListener(topics = "chart", groupId = "#{myGroupId}", containerFactory = "filteringKafkaListenerContainerFactory")
    void chartListener(Object data) {
        System.out.println("Tick Listner data recieved.");
        ConsumerRecord received_data = (ConsumerRecord) data;
        tick_publisher.emitNext((Long) received_data.value(), (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Integer> filteringKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Integer> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setRecordFilterStrategy(record -> {
            String userId = record.headers().lastHeader("user_id").value().toString();
            return !userId.equals(myUserId);
        });
        return factory;
    }
}
