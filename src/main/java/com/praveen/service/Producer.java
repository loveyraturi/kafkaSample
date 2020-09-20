package com.praveen.service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
	@Autowired
	private KafkaTemplate<String, Map<String,String>> kafkaTemplate;
	
	AtomicInteger atomicInteger = new AtomicInteger();

	public void sendMessage(Map<String,String> message,String topic) {
    		kafkaTemplate.send(topic,topic+atomicInteger.getAndIncrement(), message);
	}
}
