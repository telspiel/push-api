package com.noesis.push.api.kafka;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
 
@Component
public class MessageProducer {

	private static Log logger = LogFactory.getLog(MessageProducer.class);

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	public void send(String topic, String payload) {
		logger.info("Sending push message='" + payload + "' to topic='" + topic + "'");
		kafkaTemplate.send(topic, payload);
	}
}