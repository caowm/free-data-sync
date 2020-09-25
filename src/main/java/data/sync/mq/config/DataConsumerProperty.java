package data.sync.mq.config;

import lombok.Data;

@Data
public class DataConsumerProperty {

	private String queueName;
	
	private boolean saveData;
	
	private String dataSource;	
	
	
}
