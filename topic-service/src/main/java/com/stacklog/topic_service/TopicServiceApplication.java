package com.stacklog.topic_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.stacklog.topic_service", "com.stacklog.core_service"})
@EnableJpaRepositories(basePackages = "com.stacklog.topic_service.model.repo")
@EntityScan(basePackages = "com.stacklog.topic_service.model.entities")
@EnableFeignClients
public class TopicServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TopicServiceApplication.class, args);
	}

}
