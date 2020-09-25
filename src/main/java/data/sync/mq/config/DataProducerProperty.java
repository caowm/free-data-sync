package data.sync.mq.config;

import lombok.Data;

/**
 * Data Producer Property
 *
 * @author caowm 2020-09-20
 * 
 */

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
