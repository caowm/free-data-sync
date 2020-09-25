package data.sync.mq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import data.sync.mq.core.TaskCreator;

@Configuration
@EnableScheduling
public class Config {

	@Bean
	public MQTaskProperties taskProperties() {
		return new MQTaskProperties();
	}

	@Bean
	public TaskCreator taskCreator() {
		return new TaskCreator();
	}
}
