package org.balab.minireal.middleware.kafka;

import com.example.application.sim_ui.SimUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks.Many;

import java.util.ArrayList;
import java.util.Map;

@Component
public class KafkaListeners
{
    private final Many<Object> publisher;
    private final Many<Long> tick_publisher;

    public KafkaListeners(Many<Object> publisher,
                          Many<Long> tick_publisher
    ){
        this.publisher = publisher;
        this.tick_publisher = tick_publisher;
    }

//    @KafkaListener(topics = "chart", groupId = "defaultGroup")
//    void chartListener(Object data)
//    {
//        ConsumerRecord recieved_data = (ConsumerRecord) data;
//        publisher.emitNext(recieved_data, (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
//    }

//    @KafkaListener(topics = "tick", groupId = "defaultGroup")
//    void tickListener(Object data)
//    {
//        ConsumerRecord recieved_data = (ConsumerRecord) data;
//        tick_publisher.emitNext((Long) recieved_data.value(), (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
//    }


}
