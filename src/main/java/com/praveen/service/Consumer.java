package com.praveen.service;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;

public class Consumer implements Runnable {
	AtomicInteger counter = new AtomicInteger();
	KafkaConsumer<String, String> consumer;
	String topic;
	String bootstrapServers;
	OutputStreamWriter osw;
	AtomicInteger size = new AtomicInteger();
	FileOutputStream out = null;
	long start = 0L;
	String filePath;

	public Consumer(String topic, String bootstrapServers,String filePath) {
		this.filePath=filePath;
		if (this.start == 0L) {
			this.start = System.currentTimeMillis();
		}
		try {
			out = new FileOutputStream(filePath+"\\output.csv",true);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.topic = topic;
		this.bootstrapServers = bootstrapServers;
		new NewTopic("finalTopic", 4, (short) 1);

		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, topic);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		this.consumer = new KafkaConsumer<>(properties);

	}

	@Override
	public void run() {

		List<String> topicList = new ArrayList<>();
		topicList.add(this.topic);
		this.consumer.subscribe(topicList);
		
		while (true) {
			try {
				String line = "";
				ConsumerRecords<String, String> records = this.consumer.poll(100);
				for (ConsumerRecord<String, String> record : records) {
					line="";
					JSONObject jsonObject = new JSONObject(record.value());
					synchronized (this) {

						Iterator<String> keys = jsonObject.keys();

						while (keys.hasNext()) {
							String key = (String) keys.next();
							if (line != "") {
								line = line + "," + jsonObject.getString(key);
							} else {
								line = jsonObject.getString(key);
							}
						}
						size.set(size.get() + line.getBytes().length);
						out.write(line.getBytes());
						out.write("\n".getBytes());
						out.flush();
					}
					double lineSize = size.get();
					double sizeInMb = (double) lineSize / (1024 * 1024);
					InputStream result = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));
					String filePath;
					float msec = System.currentTimeMillis() - start;
					float sec = msec / 1000F;
					float minutes = sec / 60F;
					if (sizeInMb > 10 || minutes > 30) {
						filePath = this.filePath+"\\output" + counter.getAndIncrement() + ".csv";

						out = new FileOutputStream(filePath,true);

					}
				}

				

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
