package com.anish.apiGateway.service.KafkaServices;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;


@Service
public class RegisterUserKafkaService {

    private static final String TOPIC = "registration-request";

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void sendMessage(String key, Map<String, Object> message) {
        kafkaTemplate.send(TOPIC, key, message);
        System.out.println("Message sent: " + message);
    }




}
