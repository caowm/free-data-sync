package data.sync.mq.config;

import lombok.Data;

/**
 * Data Consumer Property
 *
 * @author caowm 2020-09-20
 * 
 */

@Data
public class DataConsumerProperty {

	private String queueName;
	
	private boolean saveData;
	
	private String dataSource;	
	
	
}
