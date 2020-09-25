package data.sync.mq.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;
import data.sync.mq.config.DataConsumerProperty;
import data.sync.mq.model.MQData;
import data.sync.mq.service.ImportService;

/**
 * MQ Data Consumer
 *
 * @author caowm 2020-09-20
 * 
 */

@Slf4j
public class DataConsumer extends DefaultConsumer {
	private DataImportProcessor importer;
	private ImportService service;
	private DataConsumerProperty property;

	public DataConsumer(Channel channel, ImportService service, DataImportProcessor importer,
			DataConsumerProperty property) {
		super(channel);
		this.service = service;
		this.importer = importer;
		this.property = property;
	}

	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
			throws IOException {
		boolean result = false;
		try {
			MQData data = JSON.parseObject(body, MQData.class);
			// determine data source
			String dataSource = property.getDataSource();
			if (StringUtils.isEmpty(dataSource))
				dataSource = data.getSource();

			service.importData(dataSource, data, importer);
		} catch (Exception e) {
			log.error("import data error:",  e);
		} finally {
			try {
				// acknowledge message
				getChannel().basicAck(envelope.getDeliveryTag(), false);
				result = true;
			} finally {
				if (property.isSaveData() || !result) {
					saveMQData(body);
				}
			}
		}
	}

	public static void saveMQData(byte[] body) {
		SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd/HHmmss-SSS");
		String fileName = "data/" + format.format(new Date()) + ".json";

		new File(fileName.substring(0, 13)).mkdirs();

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fileName);
			fos.write(body);
			fos.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
