package io.confluent.cloud.workshop;

import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import io.confluent.cloud.workshop.model.Claim;
import static io.confluent.cloud.workshop.Utils.*;

public class NativeProducer {

  private void run(Properties properties) {

    createTopic(properties);
    producer = new KafkaProducer<String, Claim>(properties);
    ProducerRecord<String, Claim> record = null;

    for (;;) {

      String generatedKey = UUID.randomUUID().toString();
      record = new ProducerRecord<String, Claim>(CLAIMS,
        generatedKey, createClaim(generatedKey));

      producer.send(record, new Callback() {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
          System.out.println("Claim '" + generatedKey + "' created successfully!");
        }
      });

      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {}

    }

  }

  private Claim createClaim(String generatedKey) {
    Claim claim = new Claim();
    claim.setId(generatedKey);
    claim.setDate(new Date().getTime());
    claim.setState(STATES[random.nextInt(STATES.length-1)]);
    claim.setAmount(Double.valueOf(random.nextInt(1000)));
    return claim;
  }

  private static final String[] STATES = {
    "MA", "OR", "CA", "NC", "FL", "TN", "OH",
    "CO", "GA", "IL", "KS", "MD", "MO", "NJ",
    "NY", "SC", "WA", "VA", "CT", "AL", "AZ"
  };
  private static final Random random = new Random();
  private static KafkaProducer<String, Claim> producer;

  static {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      producer.close();
    }));
  }

  public static void main(String args[]) throws Exception {
    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
    properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    properties.load(NativeProducer.class.getResourceAsStream("/ccloud.properties"));
    new NativeProducer().run(properties);
  }

}