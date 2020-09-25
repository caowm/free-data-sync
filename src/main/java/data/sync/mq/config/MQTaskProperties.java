package data.sync.mq.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * MQ Task Property
 *
 * @author caowm 2020-09-20
 * 
 */

@Data
@ConfigurationProperties("mq-task")
public class MQTaskProperties {
	
	private int threadPool = 2;

	private List<DataConsumerProperty> dataConsumers = new ArrayList<DataConsumerProperty>();
	
	private List<DataProducerProperty> dataProducers = new ArrayList<DataProducerProperty>();
	
}
