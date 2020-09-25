package data.sync.mq.channel;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import data.sync.mq.util.ContextUtil;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * 
 * Exchange
 * 
 * Publish message
 *
 * @author caowm 2020-09-20
 * 
 */
public class MQExchange {

	private String exchangeName;
	private String routingKey;
	private String contentType;
	private Channel channel = null;

	public MQExchange(String exchangeName, String routingKey, String contentType) throws IOException, TimeoutException {
		super();
		this.exchangeName = exchangeName;
		this.routingKey = routingKey;
		this.contentType = contentType;

		MQChannel baseMQ = ContextUtil.getBean(MQChannel.class);
		channel = baseMQ.newChannel();
	}

	public void publish(byte[] body, Map<String, Object> headers) throws IOException, TimeoutException {
		// Persistent message
		BasicProperties messageProperties = new BasicProperties(contentType, null, headers, 2, 0, null, null, null,
				null, null, null, null, null, null);
		channel.basicPublish(exchangeName, routingKey, messageProperties, body);
	}

	public String getExchange() {
		return exchangeName;
	}

	public void setExchange(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void close() {
		try {
			channel.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}

}
