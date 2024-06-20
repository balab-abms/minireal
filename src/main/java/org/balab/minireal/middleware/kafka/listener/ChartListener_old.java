package org.balab.minireal.middleware.kafka.listener;

import com.storedobject.chart.SOChart;
import com.vaadin.flow.component.UI;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ChartListener_old implements Runnable {
    private final KafkaConsumer<String, Object> consumer;
    private volatile boolean running = true;
    private String sim_session_token;
    private UI parent_ui;
    private ChartJs chartJs;
    private LineChartConfig config;
    private UIRelatedHelpers ui_helper_service;

    public ChartListener_old(
            UIRelatedHelpers ui_helper_service,
            String brokers,
            String sim_session_token,
            UI parent_ui,
            ChartJs chartJs
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
        this.chartJs = chartJs;
        this.config = (LineChartConfig) chartJs.getConfig();
    }

    @Override
    public void run() {
        try {
            String topic = "chart" + sim_session_token;
            consumer.subscribe(Collections.singletonList(topic));
            while (!Thread.currentThread().isInterrupted()  && running) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));
                // update the data value
                List<Dataset<?, ?>> datasets_list = config.data().getDatasets();
                List<String> labels_list = new ArrayList<>(config.data().getLabels());
                int labels_sz = labels_list.size();
                for (ConsumerRecord<String, Object> record : records) {
                    // get the data
                    Object chart_data = record.value();
                    String chart_name = record.key().toString();

                    boolean is_data_saved = false;
                    // add to existing dataset
                    if(datasets_list != null){
                        for (Dataset<?, ?> temp_ds : datasets_list) {
                            LineDataset temp_line_ds = (LineDataset) temp_ds;
                            String ds_label = temp_line_ds.buildJson().get("label").asString();
                            if (ds_label.equals(chart_name)) {
                                temp_line_ds.addData(Double.parseDouble(String.valueOf(chart_data)));
                                is_data_saved = true;
                                break;
                            }
                        }
                    }

                    // if dataset not present ... create a new one
                    if(!is_data_saved) {
                        config.data()
                                .labels()
                                .addDataset(new LineDataset().type().label(chart_name)
                                        .backgroundColor("rgba(255,255,255,0)")
                                        .borderColor(ui_helper_service.stringToRGB(chart_name))
                                        .borderWidth(2)
                                        .pointRadius(0)
                                        .addData(Double.parseDouble(String.valueOf(chart_data)))
                                )
                                .and();

                        // update the list of datasets and labels
                        datasets_list = config.data().getDatasets();
                    }

                }
                int dataset_sz = records.count();
                if(datasets_list != null && !datasets_list.isEmpty())
                    dataset_sz = records.count() / datasets_list.size();

                for(int k=0; k<dataset_sz; k++)
                {
                    labels_list.add(String.valueOf(k + labels_sz));
                }
                config.data().labelsAsList(labels_list);
                parent_ui.access(() -> {
                    chartJs.update();
                });

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            Thread.currentThread().interrupt(); // Preserve the interrupt
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
