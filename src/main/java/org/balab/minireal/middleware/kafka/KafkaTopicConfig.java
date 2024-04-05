package org.balab.minireal.middleware.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig
{


    @Bean
    public NewTopic chartTopic()
    {
        return TopicBuilder.name("chart").build();
    }

    @Bean
    public NewTopic tickTopic()
    {
        return TopicBuilder.name("tick").build();
    }


}
