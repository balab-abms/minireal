package org.balab.minireal.middleware.kafka.listener;

import com.storedobject.chart.Data;
import com.storedobject.chart.DataChannel;
import com.storedobject.chart.SOChart;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.balab.minireal.views.helpers.UIRelatedHelpers;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.LineChartConfig;
import org.vaadin.addons.chartjs.data.Dataset;
import org.vaadin.addons.chartjs.data.LineDataset;
import oshi.util.tuples.Pair;

import java.time.Duration;
import java.util.*;

public class ChartListener implements Runnable {
    private final KafkaConsumer<String, Object> consumer;
    private volatile boolean running = true;
    private String sim_session_token;
    private UI parent_ui;
    private HashMap<String, Pair<Data, DataChannel>> chart_datachannel_list;
    private UIRelatedHelpers ui_helper_service;

    public ChartListener(
            UIRelatedHelpers ui_helper_service,
            String brokers,
            String sim_session_token,
            UI parent_ui,
            HashMap<String, Pair<Data, DataChannel>> chart_datachannel_list
    ) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chart" + sim_session_token);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put("group.instance.id", "chart" + sim_session_token);
        this.consumer = new KafkaConsumer<>(props);
        this.ui_helper_service = ui_helper_service;
        this.sim_session_token = sim_session_token;
        this.parent_ui = parent_ui;
        this.chart_datachannel_list = chart_datachannel_list;
    }

    @Override
    public void run() {
        try {
            String topic = "chart" + sim_session_token;
            consumer.subscribe(Collections.singletonList(topic));
            while (!Thread.currentThread().isInterrupted()  && running) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, Object> record : records) {
                    // get the data
                    Object chart_data = record.value();
                    String chart_name = record.key().toString();

                    Pair<Data, DataChannel> temp_data_channel = chart_datachannel_list.get(chart_name);
                    Data y_data = temp_data_channel.getA();
                    DataChannel data_channel = temp_data_channel.getB();
                    y_data.add((Number) chart_data);
                    data_channel.append(y_data.size(), (Number) chart_data);
                    System.out.println("Added data to chart");
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Thread was interrupted, closing consumer.");

        } finally {
            running = false;
            consumer.unsubscribe();
            System.out.println("Resource Cleaned");
        }
    }

    public void stop() {
        running = false;
        consumer.unsubscribe();
    }
}
