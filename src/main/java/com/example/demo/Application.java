package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application-ssl.properties")
public class Application {

	public static void main(String... args) {
		SpringApplication application = new SpringApplication(Application.class);
		application.setAdditionalProfiles("ssl");
		application.run(args);
	}
}
