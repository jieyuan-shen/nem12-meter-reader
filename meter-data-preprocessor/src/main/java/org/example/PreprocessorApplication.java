package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PreprocessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreprocessorApplication.class, args);

		// Keep application running, to avoid message loss while closing too early
		synchronized (PreprocessorApplication.class) {
			try {
				PreprocessorApplication.class.wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
