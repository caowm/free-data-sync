package data.sync.mq.processor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import data.sync.mq.channel.MQExchange;
import data.sync.mq.config.DataProducerProperty;
import data.sync.mq.model.MQData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataPublisher {

	private MQExchange exchange;
	private DataProducerProperty property;

	public DataPublisher(MQExchange exchange, DataProducerProperty property) throws IOException, TimeoutException {
		this.property = property;
		this.exchange = exchange;
	}

	/**
	 * Publish data
	 * 
	 * Support routing key.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public boolean publish(MQData data) {
		try {
			String routingKey = property.getRouting();
			if (StringUtils.isEmpty(routingKey))
				routingKey = data.getSource();

			exchange.setRoutingKey(routingKey);
			exchange.publish(JSON.toJSONBytes(data), (Map<String, Object>) (Object) data.getDataTags());

			return true;
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (TimeoutException e) {
			log.error(e.getMessage());
		}

		return false;
	}

}
