package com.praveen.controller;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.praveen.service.Consumer;
import com.praveen.service.Producer;
import org.simpleflatmapper.csv.CsvParser;
import org.simpleflatmapper.csv.CsvReader;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/realestate")
public class EngineController {
	@Autowired
	Producer producer;
	@Value(value = "${kafka.bootstrapAddress}")
	private String bootstrapAddress;
	@Value(value = "${file.path}")
	private String filepath;
	ExecutorService executor = Executors.newFixedThreadPool(10);

	public static List<String> topics = new ArrayList<>();

	@CrossOrigin
	@PostMapping(path = "/sendMessage", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Map<String, Boolean> searchProperties(@RequestBody(required = true) Map<String, String> resp) {
		Map<String, Boolean> response = new HashMap<String, Boolean>();
		response.put("status", true);
		String eventType = resp.get("event_type");
		if (!topics.contains(eventType)) {
			NewTopic eventTypeTopic = new NewTopic(eventType, 4, (short) 1);
			this.executor.submit(new Consumer(eventType, bootstrapAddress,filepath));
			System.out.println(eventTypeTopic);
			topics.add(eventType);
		}
		this.producer.sendMessage(resp, eventType);
		return response;
	}

	@CrossOrigin
	@RequestMapping(value = "/sendMessageCSV", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Boolean> sendMessageCSV(@RequestParam("fileName") MultipartFile file) {
		System.out.println("############");
		Map<String, Boolean> response = new HashMap<String, Boolean>();
		response.put("status", true);
		Reader filereader;
		int j = 0;
		List<String> headers = new ArrayList<>();
		CsvReader reader = null;
		try {
			filereader = new InputStreamReader(file.getInputStream());
			reader = CsvParser.reader(filereader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Iterator<String[]> iterator = reader.iterator();
		while (iterator.hasNext()) {
			String[] row = iterator.next();
			Map<String, String> message = new HashMap<>();
			if (j == 0) {
				for (String column: row) {
					headers.add(column);
				}
			} else {
				for (int i = 0; i < row.length - 1; i++) {
					message.put(headers.get(i), row[i]);
				}
				System.out.println(message);
				String eventType=message.get("event_type");
				 if(!topics.contains(eventType)) {
				 NewTopic eventTypeTopic = new NewTopic(eventType, 4, (short) 1);
				 this.executor.submit(new Consumer(eventType,bootstrapAddress,filepath));
				 System.out.println(eventTypeTopic);
				 topics.add(eventType);
				 }
				 this.producer.sendMessage(message,eventType);
			}
			j++;
			
		}
		return response;
	}

}
