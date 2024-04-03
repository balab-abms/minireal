package org.simreal.abm.middleware.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig
{
    @Bean
    public NewTopic filesTopic()
    {
        return TopicBuilder.name("files").build();
    }

    @Bean
    public NewTopic chartTopic()
    {
        return TopicBuilder.name("chart").build();
    }

    @Bean
    public NewTopic visualsTopic()
    {
        return TopicBuilder.name("visuals").build();
    }

    @Bean
    public NewTopic tickTopic()
    {
        return TopicBuilder.name("tick").build();
    }

    public NewTopic dbResultsTopic()
    {
        return TopicBuilder.name("db_results").build();
    }

    @Bean
    public NewTopic dbSignalsTopic(){
        return TopicBuilder.name("db_signals").build();
    }
}
