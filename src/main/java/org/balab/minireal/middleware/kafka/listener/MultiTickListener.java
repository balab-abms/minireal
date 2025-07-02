package org.balab.minireal.middleware.kafka.listener;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MultiTickListener implements Runnable {
    private final KafkaConsumer<String, Object> consumer;
    private volatile boolean running = true;
    private String sim_session_token;
    private UI parent_ui;
    private TextField tick_tf;

    public MultiTickListener(
            String brokers,
            String sim_session_token,
            UI parent_ui,
            TextField tick_tf
    ) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "tick" + sim_session_token);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(props);
        this.sim_session_token = sim_session_token;
        this.parent_ui = parent_ui;
        this.tick_tf = tick_tf;
    }

    @Override
    public void run() {
        try {
            String topic = "tick" + sim_session_token;
            consumer.subscribe(Collections.singletonList(topic));
            while (!Thread.currentThread().isInterrupted() && running) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, Object> record : records) {
                    parent_ui.access(() -> {
                        String tick_value = String.valueOf(record.value());
                        String temp_prev_tick_tf_value = tick_tf.getValue();
                        if(!tick_value.equals(temp_prev_tick_tf_value)){
                            tick_tf.setValue(tick_value);
                        } else {
                            System.out.println("Tick value is the same. No need to update TextBox.");
                        }
                    });
                }
            }
        } catch (InterruptException e) {
            System.out.println("Thread was interrupted, closing consumer.");
            Thread.currentThread().interrupt(); // Preserve the interrupt
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            running = false;
            closeConsumer();
            System.out.println("Resource Cleaned");
        }
    }

    public void stop() {
        running = false;
        closeConsumer();
    }

    private void closeConsumer() {
        try {
            consumer.unsubscribe();
            consumer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
