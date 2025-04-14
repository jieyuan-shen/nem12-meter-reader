package org.example;

import org.example.service.IQueueReceiver;
import org.example.service.impl.ZeroMQReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DBWriterApplication {

	public static void main(String[] args) {
		SpringApplication.run(DBWriterApplication.class, args);
	}

}
