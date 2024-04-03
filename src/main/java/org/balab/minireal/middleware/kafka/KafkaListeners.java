package org.simreal.abm.middleware.kafka;

import com.example.application.sim_ui.SimUI;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import java.util.ArrayList;
import java.util.Map;

@Component
public class KafkaListeners
{
    private final Many<Object> publisher;
    private final Many<Long> tick_publisher;
    private final Many<ArrayList<SimUI>> visuals_publisher;
    private final Many<Map<String, Object>> db_results_publisher;
    @Autowired
    private Many<String> db_signals_publisher;

    private ArrayList<SimUI> simUI_array;

    public KafkaListeners(Many<Object> publisher,
                          Many<Long> tick_publisher,
                          Many<ArrayList<SimUI>> visuals_publisher,
                          Many<Map<String, Object>> db_results_publisher
    ){
        this.publisher = publisher;
        this.tick_publisher = tick_publisher;
        this.visuals_publisher = visuals_publisher;
        this.db_results_publisher = db_results_publisher;
        simUI_array = new ArrayList<>();
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

//    @KafkaListener(topics = "visuals", groupId = "defaultGroup")
//    void visualsListener(Object data)
//    {
//        ConsumerRecord recieved_data = (ConsumerRecord) data;
//        if(simUI_array.size() >= Integer.parseInt(recieved_data.key().toString()))
//        {
//            visuals_publisher.emitNext(simUI_array, (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
//            simUI_array = new ArrayList<>();
//        }
//        simUI_array.add((SimUI) recieved_data.value());
//    }
//
//    @KafkaListener(topics = "db_results", groupId = "defaultGroup")
//    void dbResultsListener(Object data)
//    {
//        ConsumerRecord recieved_data = (ConsumerRecord) data;
//        if (recieved_data.key().equals("data")) {
//            db_results_publisher.emitNext(
//                    (Map<String, Object>) recieved_data.value(),
//                    (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED
//            );
//        } else if (recieved_data.key().equals("signal")) {
//            db_signals_publisher.emitNext(
//                (String) recieved_data.value(),
//                (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED
//            );
//        }
//
//    }

//    @KafkaListener(topics = "db_signals", groupId = "defaultGroup")
//    void dbSignalsListener(Object data)
//    {
//        ConsumerRecord recieved_data = (ConsumerRecord) data;
//        db_signals_publisher.emitNext(
//                (String) recieved_data.value(),
//                (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED
//        );
//
//    }
}
