package data.sync.mq.channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import data.sync.mq.util.ContextUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;

import lombok.extern.slf4j.Slf4j;

/**
 * Queue
 * 
 * Consume message
 *
 * @author caowm 2020-09-20
 * 
 */
@Slf4j
public class MQQueue {
	private String queueName;
	private Channel channel = null;

	public MQQueue(String queueName) throws IOException, TimeoutException {
		super();
		this.queueName = queueName;

		MQChannel baseMQ = ContextUtil.getBean(MQChannel.class);
		channel = baseMQ.newChannel();
	}

	public String consume(DefaultConsumer consumer) throws IOException, TimeoutException {
		channel.basicQos(1);
		String tag = channel.basicConsume(queueName, false, consumer);
		log.debug("mq consume:{}", queueName);
		return tag;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	public Channel getChannel() {
		return channel;
	}

}
