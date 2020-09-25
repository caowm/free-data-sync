package data.sync.mq.processor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.DataSource;

import org.springframework.util.StringUtils;

import data.sync.mq.util.ContextUtil;
import data.sync.mq.util.DataUtil;

import lombok.extern.slf4j.Slf4j;

import data.sync.mq.config.*;
import data.sync.mq.model.MQData;
import data.sync.mq.model.MQDataSet;

/**
 * Data Sync Export Processor
 *
 * @author caowm 2020-09-20
 * 
 */

@Slf4j
public class DataExportProcessor {

	private String syncSN = "";
	private String syncDate = "";
	private String dataTypeOut = "";
	private String dataDate = "";
	private String dataName = "";
	private String pkName = "";
	private String pkValue = "";
	private String sourceOut = "";

	private DataSource dataSource;
	private DataProcessorProperties config;

	private DataProducerProperty property;
	private DataPublisher publisher;

	public DataExportProcessor(DataProducerProperty property) {
		this.property = property;
		dataSource = ContextUtil.getBean(DataSource.class);
		config = ContextUtil.getBean(DataProcessorProperties.class);
	}

	public MQData process(DataPublisher publisher) throws SQLException {
		this.publisher = publisher;
		Connection connection = dataSource.getConnection();
		try {
			return exportData(connection);
		} finally {
			connection.close();
		}
	}

	private MQData exportData(Connection connection) throws SQLException {
		syncSN = "";
		String syncResult = "N";

		ResultSet tableResultSet = syncOutBegin(connection);
		try {
			if (!StringUtils.isEmpty(syncSN)) {
				try {
					MQData data = new MQData();
					data.setDataType(dataTypeOut);
					data.setDate(dataDate);
					data.setSource(sourceOut);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_SYNC_SN, syncSN);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_SYNC_DATE, syncDate);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_DATA_TYPE, dataTypeOut);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_DATA_DATE, dataDate);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_DATA_NAME, dataName);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_PK_NAME, pkName);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_PK_VALUE, pkValue);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_SOURCE, sourceOut);
					data.writeDataTag(DataProcessorProperties.DATA_TAG_WORK, property.getWorker());

					while (tableResultSet.next()) {
						data.addDataSet(syncOutData(connection, tableResultSet.getString("DATA_NAME"),
								tableResultSet.getString("PK_NAME"), tableResultSet.getString("SYNC_MODE")));
					}

					if (publisher.publish(data))
						syncResult = "Y";
					
					return data;
				} finally {
					syncOutEnd(connection, syncResult);					
					log.info("export ok. name:{} source:{} pk={}", dataName, sourceOut, pkValue);
				}
			}
		} finally {
			if (tableResultSet != null) {
				tableResultSet.getStatement().close();
			}
		}
		return null;
	}

	private ResultSet syncOutBegin(Connection connection) throws SQLException {
		CallableStatement syncOutBeginStatement = connection.prepareCall(config.syncOutBeginSql);
		try {
			syncOutBeginStatement.setString(1, property.getDataType());
			syncOutBeginStatement.setString(2, property.getSource());
			syncOutBeginStatement.setString(3, property.getWorker());
			syncOutBeginStatement.registerOutParameter(4, Types.VARCHAR);
			syncOutBeginStatement.registerOutParameter(5, Types.VARCHAR);
			syncOutBeginStatement.registerOutParameter(6, Types.VARCHAR);
			syncOutBeginStatement.registerOutParameter(7, Types.VARCHAR);
			syncOutBeginStatement.registerOutParameter(8, Types.VARCHAR);
			syncOutBeginStatement.registerOutParameter(9, Types.VARCHAR);
			syncOutBeginStatement.registerOutParameter(10, Types.VARCHAR);
			syncOutBeginStatement.registerOutParameter(11, Types.VARCHAR);
			syncOutBeginStatement.execute();

			syncSN = syncOutBeginStatement.getString(4);
			syncDate = syncOutBeginStatement.getString(5);
			dataTypeOut = syncOutBeginStatement.getString(6);
			dataDate = syncOutBeginStatement.getString(7);
			dataName = syncOutBeginStatement.getString(8);
			pkName = syncOutBeginStatement.getString(9);
			pkValue = syncOutBeginStatement.getString(10);
			sourceOut = syncOutBeginStatement.getString(11);
		} finally {
			syncOutBeginStatement.close();
		}

		ResultSet tableResultSet = null;
		if (!syncSN.isEmpty()) {
			PreparedStatement statement = connection.prepareStatement(config.syncOutNamesSql);
			statement.setString(1, syncSN);
			statement.setString(2, dataTypeOut);
			statement.setString(3, dataDate);
			statement.setString(4, sourceOut);
			statement.setString(5, property.getWorker());
			tableResultSet = statement.executeQuery();
		}
		return tableResultSet;
	}

	private MQDataSet syncOutData(Connection connection, String dataName, String pkName, String syncMode)
			throws SQLException {

		PreparedStatement statement = connection.prepareStatement(config.syncOutDataSql);
		statement.setString(1, syncSN);
		statement.setString(2, syncMode);
		statement.setString(3, dataTypeOut);
		statement.setString(4, dataDate);
		statement.setString(5, dataName);
		statement.setString(6, pkName);
		statement.setString(7, sourceOut);
		statement.setString(8, property.getWorker());
		ResultSet resultSet = statement.executeQuery();
		try {
			return DataUtil.toMQDataSet(resultSet, dataName, pkName, syncMode);
		} finally {
			resultSet.close();
			statement.close();
		}
	}

	private void syncOutEnd(Connection connection, String syncResult) throws SQLException {
		if (!StringUtils.isEmpty(syncSN)) {
			PreparedStatement commitStatement = connection.prepareStatement(config.syncOutEndSql);
			try {
				commitStatement.setString(1, syncSN);
				commitStatement.setString(2, syncResult);
				commitStatement.setString(3, dataTypeOut);
				commitStatement.setString(4, dataDate);
				commitStatement.setString(5, dataName);
				commitStatement.setString(6, sourceOut);
				commitStatement.setString(7, property.getWorker());
				commitStatement.execute();
			} finally {
				commitStatement.close();
			}
		}
	}

}
