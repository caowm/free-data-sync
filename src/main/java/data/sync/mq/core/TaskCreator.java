package data.sync.mq.core;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.StringUtils;

import data.sync.mq.channel.MQQueue;
import data.sync.mq.config.DataConsumerProperty;
import data.sync.mq.config.DataProducerProperty;
import data.sync.mq.config.MQTaskProperties;
import data.sync.mq.processor.DataConsumer;
import data.sync.mq.processor.DataImportProcessor;
import data.sync.mq.service.ExportService;
import data.sync.mq.service.ImportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskCreator implements InitializingBean, DisposableBean {

	@Autowired
	MQTaskProperties mqTaskProperties;
	@Autowired
	ImportService importService;
	@Autowired
	ExportService exportService;
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	public TaskCreator() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.debug("create data task");
		createTask();
	}

	@Override
	public void destroy() throws Exception {

	}

	private void createTask() {
		taskScheduler.setPoolSize(mqTaskProperties.getThreadPool());
		createDataProducer();
		createDataConsumer();
	}

	private void createDataProducer() {
		if (mqTaskProperties.getDataProducers() == null)
			return;

		for (DataProducerProperty item : mqTaskProperties.getDataProducers()) {
			try {
				if (StringUtils.isEmpty(item.getCron())) {
					taskScheduler.scheduleAtFixedRate(new DataExportRunner(item, exportService),
							item.getInterval() * 1000);
				} else {
					taskScheduler.schedule(new DataExportRunner(item, exportService), new CronTrigger(item.getCron()));
				}

			} catch (IOException | TimeoutException e) {
				log.error("create data producer erro: ", e);
			}
		}
	}

	private void createDataConsumer() {
		if (mqTaskProperties.getDataConsumers() == null)
			return;

		for (DataConsumerProperty item : mqTaskProperties.getDataConsumers()) {
			try {
				MQQueue queue = new MQQueue(item.getQueueName());
				DataImportProcessor importer = new DataImportProcessor();
				queue.consume(new DataConsumer(queue.getChannel(), importService, importer, item));
			} catch (Exception e) {
				log.error("Create data consumer error: ", e);
			}
		}
	}
}
