package io.confluent.cloud.workshop;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

public class Utils {

    public static final String CLAIMS = "claims";

    public static void createTopic(Properties properties) {
        try (AdminClient adminClient = AdminClient.create(properties)) {
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> topicNames = topics.names().get();
            if (!topicNames.contains(CLAIMS)) {
                NewTopic newTopic = new NewTopic(CLAIMS, 4, (short) 3);
                adminClient.createTopics(Collections.singletonList(newTopic));
            }
        } catch (InterruptedException | ExecutionException ex) {}
    }

}