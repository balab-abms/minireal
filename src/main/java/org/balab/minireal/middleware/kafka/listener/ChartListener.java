package org.balab.minireal.middleware.kafka.listener;

import com.storedobject.chart.ChartException;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataChannel;
import com.storedobject.chart.SOChart;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.balab.minireal.views.helpers.UIRelatedHelpers;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.vaadin.addons.chartjs.ChartJs;
import org.vaadin.addons.chartjs.config.LineChartConfig;
import org.vaadin.addons.chartjs.data.Dataset;
import org.vaadin.addons.chartjs.data.LineDataset;
import oshi.util.tuples.Pair;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class ChartListener implements Runnable {
    private final KafkaConsumer<String, Object> consumer;
    private volatile boolean running = true;
    private String sim_session_token;
    private UI parent_ui;
    private HashMap<String, Pair<Data, Data>> chart_datachannel_list;
    private UIRelatedHelpers ui_helper_service;
    private SOChart so_chart;

    public ChartListener(
            UIRelatedHelpers ui_helper_service,
            String brokers,
            String sim_session_token,
            UI parent_ui,
            SOChart so_chart,
            HashMap<String, Pair<Data, Data>> chart_data_list
    ) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chart" + sim_session_token);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(props);
        this.ui_helper_service = ui_helper_service;
        this.sim_session_token = sim_session_token;
        this.parent_ui = parent_ui;
        this.so_chart = so_chart;
        this.chart_datachannel_list = chart_data_list;
    }

    @Override
    public void run() {
        try {
            String topic = "chart" + sim_session_token;
            consumer.subscribe(Collections.singletonList(topic));
            int chart_update_interval = 10;
            int total_data_size = 0;
            while (!Thread.currentThread().isInterrupted()  && running) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(5000));
                for (ConsumerRecord<String, Object> record : records) {
                    // get the data
                    Object chart_data = record.value();
                    String chart_name = record.key().toString();
                    Number temp_tick_val = null;

                    // get the headers
                    Headers headers = record.headers();
                    Header tickHeader = headers.lastHeader("tick");
                    if (tickHeader != null) {
                        byte[] tickHeaderValue = tickHeader.value();
                        temp_tick_val = Long.parseLong(new String(tickHeaderValue, StandardCharsets.UTF_8));
                    }

                    Pair<Data, Data> temp_data_channel = chart_datachannel_list.get(chart_name);
                    if (temp_data_channel != null) {
                        Data x_data = temp_data_channel.getA();
                        Data y_data = temp_data_channel.getB();
                        if (y_data != null && x_data != null) {
                            y_data.add((Number) chart_data);
                            // if tick header is null ... then set the x-axis value to the size of y data
                            temp_tick_val = temp_tick_val != null  ? temp_tick_val : y_data.size();
                            x_data.add(temp_tick_val);
                            total_data_size++;
                        }
                    }
                }
                if(chart_update_interval <= 0){
                    parent_ui.access(() -> {
                        try {
                            for(Pair<Data, Data> temp_chart_datas: chart_datachannel_list.values()){
                                so_chart.updateData(temp_chart_datas.getA(), temp_chart_datas.getB());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    chart_update_interval = (int)(Math.log10(total_data_size + 1)*10);
                }
                chart_update_interval--;
            }
        } catch (InterruptException e) {
            System.out.println("Thread was interrupted, closing consumer.");
            Thread.currentThread().interrupt(); // Preserve the interrupt
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            running = false;
            closeConsumer();
            System.out.println("Chart Listner Resource Cleaned");
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
