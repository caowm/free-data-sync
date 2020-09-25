package data.sync.mq.processor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import data.sync.mq.config.*;
import data.sync.mq.util.ContextUtil;
import data.sync.mq.util.DataUtil;

import lombok.extern.slf4j.Slf4j;

import data.sync.mq.model.MQData;
import data.sync.mq.model.MQDataSet;

/**
 * Data Sync Import Processor
 * 
 * Synchronization Mode:
 * <li>CU: insert data first, update data if error happens</li>
 * <li>UC: update data first, create data if no effect</li>
 * <li>D: delete data</li>
 * <li>C: insert data</li>
 * <li>U: update data</li>
 * <li>

 * TODO:
 * cache SQL 
 * 
 * @author caowm 2020-09-20
 */

@Slf4j
public class DataImportProcessor {

	public static String SYNC_MODE_CREATE = "C";
	public static String SYNC_MODE_READ = "R";
	public static String SYNC_MODE_UPDATE = "U";
	public static String SYNC_MODE_DELETE = "D";
	public static String SYNC_MODE_UPDATE_FAIL_CREATE = "UC";
	public static String SYNC_MODE_CREATE_FAIL_UPDATE = "CU";

	private String syncSN = "";
	private String syncDate = "";
	private String dataType = "";
	private String dataDate = "";
	private String source = "";
	private String pkName = "";
	private String pkValue = "";
	private String worker = "";

	protected String dataName = "";
	protected String keyField = "";
	protected String syncMode = "";

	private String customSyncInSql = null;
	private CallableStatement sycInBeginStatement = null;
	private PreparedStatement syncInEndStatement = null;
	private PreparedStatement customSyncInSqlStatement = null;
	
	private PreparedStatement createStatement = null;
	private PreparedStatement deleteStatement = null;
	private PreparedStatement updateStatement = null;

	protected Connection connection = null;
	protected int recordCount = 0;
	protected int totalCount = 0;

	private DataSource dataSource;
	private DataProcessorProperties config;

	public DataImportProcessor() {
		dataSource = ContextUtil.getBean(DataSource.class);
		config = ContextUtil.getBean(DataProcessorProperties.class);
	}

	public int process(MQData data) throws SQLException {
		int result = 0;

		processBegin(data);
		try {
			processDataTags(data);
			processDataSetsBegin(data);

			try {
				for (int i = 0; i < data.getDataSets().size(); i++) {
					try {
						MQDataSet dataset = data.getDataSet(i);
						processDataSetBegin(dataset);
						processDataSetRows(dataset);
						for (int j = 0; j < data.getDataSet(i).getRows().size(); j++) {
							processDataSetRow(dataset, j);
							result++;
						}
					} finally {
						processDataSetEnd(data.getDataSets().get(i));
					}
				}
			} finally {
				processDataSetsEnd(data);
			}
		} finally {
			processEnd(data);
		}

		return result;
	}

	public void processBegin(MQData data) throws SQLException {
		totalCount = 0;
		connection = dataSource.getConnection();
	}

	public void processDataTags(MQData data) {
		syncSN = data.getDataTag(DataProcessorProperties.DATA_TAG_SYNC_SN);
		syncDate = data.getDataTag(DataProcessorProperties.DATA_TAG_SYNC_DATE);
		dataType = data.getDataTag(DataProcessorProperties.DATA_TAG_DATA_TYPE);
		dataDate = data.getDataTag(DataProcessorProperties.DATA_TAG_DATA_DATE);
		pkName = data.getDataTag(DataProcessorProperties.DATA_TAG_PK_NAME);
		pkValue = data.getDataTag(DataProcessorProperties.DATA_TAG_PK_VALUE);
		source = data.getDataTag(DataProcessorProperties.DATA_TAG_SOURCE);
		worker = data.getDataTag(DataProcessorProperties.DATA_TAG_WORK);
	}

	public void processDataSetsBegin(MQData data) throws SQLException {
		if (!StringUtils.isEmpty(config.syncInBeginSql)) {
			sycInBeginStatement = connection.prepareCall(config.syncInBeginSql);
			sycInBeginStatement.registerOutParameter(10, Types.VARCHAR);
		}

		if (!StringUtils.isEmpty(config.syncInEndSql)) {
			syncInEndStatement = connection.prepareStatement(config.syncInEndSql);
		}
	}

	public void processDataSetBegin(MQDataSet dataset) throws SQLException {
		recordCount = 0;
		customSyncInSql = "";
		dataName = dataset.getDataName();
		keyField = dataset.getPkName();
		syncMode = dataset.getSyncMode().toUpperCase();
		if (syncMode == null || syncMode.isEmpty())
			syncMode = SYNC_MODE_UPDATE_FAIL_CREATE;

		log.debug("importing data: {} sync-mode: {} pk: {}", dataName, syncMode, keyField);

		if (sycInBeginStatement != null) {
			// Execute custom stored procedure after importing data
			sycInBeginStatement.setString(1, syncSN);
			sycInBeginStatement.setString(2, syncDate);
			sycInBeginStatement.setString(3, dataType);
			sycInBeginStatement.setString(4, dataDate);
			sycInBeginStatement.setString(5, dataName);
			sycInBeginStatement.setString(6, pkName);
			sycInBeginStatement.setString(7, pkValue);
			sycInBeginStatement.setString(8, source);
			sycInBeginStatement.setString(9, worker);
			sycInBeginStatement.setString(10, "");
			sycInBeginStatement.execute();

			customSyncInSql = sycInBeginStatement.getString(10);

			if (!StringUtils.isEmpty(customSyncInSql)) {
				customSyncInSqlStatement = connection.prepareStatement(customSyncInSql);
			}
		}
		
		if (StringUtils.isEmpty(customSyncInSql)) {
			String sql = "";

			if (syncMode.equals(SYNC_MODE_CREATE) || syncMode.equals(SYNC_MODE_UPDATE_FAIL_CREATE)
					|| syncMode.equals(SYNC_MODE_CREATE_FAIL_UPDATE)) {
				sql = getInsertSQL(dataset);
				createStatement = connection.prepareStatement(sql);
				log.debug("statement: {}", sql);
			}

			if (syncMode.equals(SYNC_MODE_DELETE)) {
				sql = getDeleteSQL();
				deleteStatement = connection.prepareStatement(sql);
				log.debug("statement: {}", sql);
			}

			if (syncMode.equals(SYNC_MODE_UPDATE) || syncMode.equals(SYNC_MODE_UPDATE_FAIL_CREATE)
					|| syncMode.equals(SYNC_MODE_CREATE_FAIL_UPDATE)) {
				sql = getUpdateSQL(dataset);
				updateStatement = connection.prepareStatement(sql);
				log.debug("statement: {}", sql);
			}
		}
	}

	public void processDataSetRows(MQDataSet dataset) throws SQLException {
		
	}

	public void processDataSetRow(MQDataSet dataset, int index) throws SQLException {
		if (StringUtils.isEmpty(customSyncInSql)) {
			// standard synchronization
			if (syncMode.equals(SYNC_MODE_CREATE)) {
				createDataSetRow(dataset, index);
			} else if (syncMode.equals(SYNC_MODE_UPDATE_FAIL_CREATE)) {
				if (!updateDataSetRow(dataset, index)) {
					createDataSetRow(dataset, index);
				}
			} else if (syncMode.equals(SYNC_MODE_UPDATE)) {
				updateDataSetRow(dataset, index);
			} else if (syncMode.equals(SYNC_MODE_DELETE)) {
				delteDataSetRow(dataset, index);
			} else if (syncMode.equals(SYNC_MODE_CREATE_FAIL_UPDATE)) {
				if (!createDataSetRow(dataset, index)) {
					updateDataSetRow(dataset, index);
				}
			}
		} else {
			// custom processing
			cusotmSyncInDataSetRow(dataset, index);
		}
	}

	private void cusotmSyncInDataSetRow(MQDataSet dataset, int index) throws SQLException {
		customSyncInSqlStatement.setString(1, syncSN);
		customSyncInSqlStatement.setString(2, syncDate);
		customSyncInSqlStatement.setString(3, dataType);
		customSyncInSqlStatement.setString(4, dataDate);
		customSyncInSqlStatement.setString(5, dataName);
		customSyncInSqlStatement.setString(6, pkName);
		customSyncInSqlStatement.setString(7, pkValue);
		customSyncInSqlStatement.setString(8, source);
		// call with JSON data
		customSyncInSqlStatement.setString(9, JSON.toJSONString(dataset.getRow(index)));
		customSyncInSqlStatement.execute();
		recordCount++;
	}

	private boolean createDataSetRow(MQDataSet dataset, int index) throws SQLException {
		int updateCount = 0;
		if (createStatement != null) {
			for (int i = 0; i < dataset.getFields().size(); i++) {
				String fieldValue = dataset.getRowValue(index, dataset.getFieldName(i));

				if (DataUtil.isBlobType(dataset.getFieldClass(i))) {
					createStatement.setBytes(i + 1, DataUtil.hexStringToBytes(fieldValue));
				} else {
					createStatement.setString(i + 1, fieldValue);
				}
			}
			updateCount = createStatement.executeUpdate();
			recordCount = recordCount + updateCount;
		}
		return updateCount > 0;
	}

	private boolean updateDataSetRow(MQDataSet dataset, int index) throws SQLException {
		int updateCount = 0;
		if (updateStatement != null) {
			String keyValue = "";
			int j = 1;
			for (int i = 0; i < dataset.getFields().size(); i++) {
				String fieldValue = dataset.getRowValue(index, dataset.getFieldName(i));
				// case sensitive
				if (dataset.getFieldName(i).equals(keyField)) {
					keyValue = fieldValue;
					continue;
				}
				if (DataUtil.isBlobType(dataset.getFieldClass(i))) {
					updateStatement.setBytes(j, DataUtil.hexStringToBytes((String) fieldValue));
				} else {
					updateStatement.setString(j, fieldValue);
				}
				j++;
			}
			// key condition
			updateStatement.setString(dataset.getFields().size(), keyValue);
			updateCount = updateStatement.executeUpdate();
			recordCount = recordCount + updateCount;
		}
		return (updateCount > 0);
	}

	private boolean delteDataSetRow(MQDataSet dataset, int index) throws SQLException {
		int updateCount = 0;
		if (deleteStatement != null) {
			deleteStatement.setString(1, dataset.getRowValue(index, keyField));
			updateCount = deleteStatement.executeUpdate();
			recordCount = recordCount + updateCount;
		}
		return (updateCount > 0);
	}

	public void processDataSetEnd(MQDataSet dataset) {
		if (customSyncInSqlStatement != null) {
			try {
				customSyncInSqlStatement.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
			customSyncInSqlStatement = null;
		}

		if (createStatement != null)
			try {
				createStatement.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}

		if (deleteStatement != null)
			try {
				deleteStatement.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}

		if (updateStatement != null)
			try {
				updateStatement.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}

		createStatement = null;
		deleteStatement = null;
		updateStatement = null;

		totalCount += recordCount;
	}

	public void processDataSetsEnd(MQData data) throws SQLException {
		if (syncInEndStatement != null) {
			syncInEndStatement.setString(1, syncSN);
			syncInEndStatement.setString(2, syncDate);
			syncInEndStatement.setString(3, dataType);
			syncInEndStatement.setString(4, dataDate);
			syncInEndStatement.setString(5, dataName);
			syncInEndStatement.setString(6, pkName);
			syncInEndStatement.setString(7, pkValue);
			syncInEndStatement.setString(8, source);
			syncInEndStatement.setString(9, worker);
			syncInEndStatement.execute();
		}
	}

	public void processEnd(MQData data) throws SQLException {
		if (syncInEndStatement != null) {
			try {
				syncInEndStatement.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
			syncInEndStatement = null;
		}

		if (sycInBeginStatement != null) {
			try {
				sycInBeginStatement.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
			sycInBeginStatement = null;
		}

		connection.close();
		connection = null;
		log.info("import count:{} name:{} source:{} pk:{} ", totalCount, dataName, source, pkValue);
	}

	/**
	 * Get Insert SQL by data
	 * 
	 * @param dataset
	 * @return
	 */
	private String getInsertSQL(MQDataSet dataset) {
		String fieldtext = "", quotetext = "";

		for (int i = 0; i < dataset.getFields().size(); i++) {
			if (i == 0) {
				fieldtext = dataset.getFieldName(i);
				quotetext = "?";
			} else {
				fieldtext += "," + dataset.getFieldName(i);
				quotetext += "," + "?";
			}
		}

		return "insert into " + dataName + "(" + fieldtext + ") values (" + quotetext + ")";
	}

	/**
	 * Get update SQL by data
	 */
	private String getUpdateSQL(MQDataSet dataset) {
		String fieldtext = "";

		for (int i = 0; i < dataset.getFields().size(); i++) {

			if (dataset.getFieldName(i).equalsIgnoreCase(keyField))
				continue;

			if (fieldtext.isEmpty()) {
				fieldtext = dataset.getFieldName(i) + "=?";
			} else {
				fieldtext += ", " + dataset.getFieldName(i) + "=?";
			}
		}

		return "update " + dataName + " set " + fieldtext + " where " + keyField + "=?";
	}

	// Get delete SQl
	private String getDeleteSQL() {
		return "delete from " + dataName + " where " + keyField + " = ?";
	}

}
