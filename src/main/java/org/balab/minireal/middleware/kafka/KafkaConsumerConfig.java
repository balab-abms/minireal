package org.balab.minireal.middleware.kafka;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

public class KafkaConsumerConfig
{
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> consumerConfig()
    {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, byte[]> consumerFactory()
    {
        return new DefaultKafkaConsumerFactory<>(consumerConfig());
    }

    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> factory(
            ConsumerFactory<String, byte[]> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> kafka_factory = new ConcurrentKafkaListenerContainerFactory<>();
        kafka_factory.setConsumerFactory(consumerFactory);
        return kafka_factory;
    }

}
