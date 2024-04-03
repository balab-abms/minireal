package org.balab.minireal.data.service;

import com.example.application.kafkaserialize.KafkaTemplateSerializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Component
public class StartupRunner implements CommandLineRunner {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public StartupRunner(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        String kafka_serilizer_path = "sim-service" + File.separator + "dependencies" + File.separator + "kafka_template.ser";
        writeKafkaTemplateToFile(kafkaTemplate, kafka_serilizer_path);
        System.out.println("Kafka template serialized.");
    }

    public void writeKafkaTemplateToFile(KafkaTemplate<String, Object> kafkaTemplate, String filePath) {
        KafkaTemplateSerializer kafkaTemplateSerializer = new KafkaTemplateSerializer(kafkaTemplate);
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(kafkaTemplateSerializer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
