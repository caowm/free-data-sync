package data.sync.mq.channel;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

@Component
public class MQChannel implements InitializingBean {

	@Value("${mq.host}")
	private String host;

	@Value("${mq.username}")
	private String userName;

	@Value("${mq.password}")
	private String password;

	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Channel channel = null;

	public MQChannel() throws IOException, TimeoutException {
		
	}

	/**
	 * Other thread or task get channel from this base function
	 * 
	 * @return
	 * @throws IOException
	 */
	public Channel newChannel() throws IOException {
		return connection.createChannel();
	}

	/**
	 * Publish message
	 * 
	 */
	public void publish(String exchangeName, String routingKey, Map<String, Object> headers, byte[] body,
			String contentType) throws IOException, TimeoutException {
		// Pesistent message
		BasicProperties messageProperties = new BasicProperties(contentType, null, headers, 2, 0, null, null, null,
				null, null, null, null, null, null);

		channel.basicPublish(exchangeName, routingKey, messageProperties, body);
	}

	/**
	 * Consume message
	 * 
	 */
	public String consume(String queueName, DefaultConsumer consumer) throws IOException, TimeoutException {
		channel.basicQos(1);
		return channel.basicConsume(queueName, false, consumer);
	}

	public void close() throws IOException, TimeoutException {
		if (channel != null && channel.isOpen())
			channel.close();
		if (connection != null && connection.isOpen())
			connection.close();
	}

	public Connection getConnection() {
		return connection;
	}

	public Channel getChannel() {
		return channel;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		factory = new ConnectionFactory();
		factory.setAutomaticRecoveryEnabled(true);
		factory.setHost(host);
		factory.setUsername(userName);
		factory.setPassword(password);

		connection = factory.newConnection();
		channel = connection.createChannel();		
	}

}
