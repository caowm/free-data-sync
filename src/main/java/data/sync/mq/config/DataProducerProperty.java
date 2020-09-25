package data.sync.mq.config;

import lombok.Data;

@Data
public class DataProducerProperty {
	
	private String dataSource;
	
	private int interval = 5;	
	
	private String cron;
		
	private String exchange;
	
	private String routing;
	
	private String dataType;
	
	private String source;
	
	private String worker;
	
}
