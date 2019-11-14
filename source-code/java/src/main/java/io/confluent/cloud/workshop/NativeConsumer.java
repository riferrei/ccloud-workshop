package io.confluent.cloud.workshop;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.confluent.cloud.workshop.model.Claim;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import static io.confluent.cloud.workshop.Utils.*;

public class NativeConsumer {

  public void run(Properties properties) {

    createTopic(properties);
    consumer = new KafkaConsumer<String, Claim>(properties);
    consumer.subscribe(Arrays.asList(CLAIMS));
    ConsumerRecords<String, Claim> records = null;
    
    while (true) {
      records = consumer.poll(Duration.ofMillis(500));
      for (ConsumerRecord<String, Claim> record : records) {
        System.out.println(record.value());
      }
    }

  }

  private static KafkaConsumer<String, Claim> consumer;

  static {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      consumer.close();
    }));
  }

  public static void main(String args[]) throws Exception {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-native-consumer");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
    properties.load(NativeConsumer.class.getResourceAsStream("/ccloud.properties"));
    new NativeConsumer().run(properties);
  }

}