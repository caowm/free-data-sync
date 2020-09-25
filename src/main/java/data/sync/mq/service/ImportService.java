package data.sync.mq.service;

import java.sql.SQLException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.dynamic.datasource.annotation.DS;
import data.sync.mq.model.MQData;
import data.sync.mq.processor.DataImportProcessor;

/**
 * Import Service
 *
 * @author caowm 2020-09-20
 * 
 */

@Service
public class ImportService {
	
	@DS("#dataSource")
	@Transactional
	public int importData(String dataSource, MQData data, DataImportProcessor importer) throws SQLException {
		return importer.process(data);
	}

}
