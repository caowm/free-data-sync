package data.sync.mq.service;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.baomidou.dynamic.datasource.annotation.DS;
import data.sync.mq.config.DataProducerProperty;
import data.sync.mq.model.MQData;
import data.sync.mq.processor.DataExportProcessor;
import data.sync.mq.processor.DataPublisher;

/**
 * Export Service
 *
 * @author caowm 2020-09-20
 * 
 */

@Service
public class ExportService {

	@DS("#property.dataSource")
	public MQData exportData(DataProducerProperty property, DataExportProcessor exporter, DataPublisher publisher)
			throws SQLException {
		return exporter.process(publisher);
	}

}
