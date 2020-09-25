package data.sync.mq.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import data.sync.mq.channel.MQExchange;
import data.sync.mq.config.DataProducerProperty;
import data.sync.mq.processor.DataExportProcessor;
import data.sync.mq.processor.DataPublisher;
import data.sync.mq.service.ExportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataExportRunner implements Runnable {

	private DataProducerProperty property;
	private DataPublisher publisher;
	private MQExchange exchange;
	private DataExportProcessor exporter;
	private ExportService service;

	public DataExportRunner(DataProducerProperty property, ExportService service) throws IOException, TimeoutException {
		this.property = property;
		exchange = new MQExchange(property.getExchange(), property.getRouting(), "text/json");
		publisher = new DataPublisher(exchange, property);
		exporter = new DataExportProcessor(property);
		this.service = service;
	}

	@Override
	public void run() {
		try {
			log.info("{} exporting...", property.getWorker());
			while (service.exportData(property, exporter, publisher) != null) {
			}
		} catch (SQLException e) {
			log.error("export error: ", e);
		} 
	}

}
