package org.balab.minireal.views.helpers;

import lombok.RequiredArgsConstructor;
import org.balab.minireal.middleware.kafka.KafkaTopicDeleter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SImRelatedHelpers
{
    private final KafkaTopicDeleter kafka_topic_deleter_service;
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

    public void deleteThreadsTopics(String sim_session_token){
        // delete kafka listener threads
        Thread tick_thread_del = getThreadByName("tick" + sim_session_token);
        interruptThread(tick_thread_del);

        Thread chart_thread_del = getThreadByName("chart" + sim_session_token);
        interruptThread(chart_thread_del);

        // delete kafka topics
        kafka_topic_deleter_service.deleteTopic("tick" + sim_session_token);
        kafka_topic_deleter_service.deleteTopic("chart" + sim_session_token);
        kafka_topic_deleter_service.deleteTopic("db" + sim_session_token);
    }

}
