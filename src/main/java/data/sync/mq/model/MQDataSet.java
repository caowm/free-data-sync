package data.sync.mq.model;

import java.util.List;
import java.util.Map;

/**
 * Data set
 *
 * @author caowm 2020-09-20
 * 
 */

public class MQDataSet {
	// 表名
	private String dataName;
	// 主键字段名
	private String pkName;
	// 更新模式
	private String syncMode;
	// 数据行数
	private int rowCount;
	// 元数据
	private List<FieldMeta> fields;
	// 记录行
	private List<Map<String, String>> rows;
	
	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getPkName() {
		return pkName;
	}

	public void setPkName(String pkName) {
		this.pkName = pkName;
	}

	public String getSyncMode() {
		return syncMode;
	}

	public void setSyncMode(String syncMode) {
		this.syncMode = syncMode;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public List<FieldMeta> getFields() {
		return fields;
	}

	public void setFields(List<FieldMeta> fields) {
		this.fields = fields;
	}

	public List<Map<String, String>> getRows() {
		return rows;
	}

	public void setRows(List<Map<String, String>> rows) {
		this.rows = rows;
	}
	
	public String getRowValue(int index, String fieldName) {
		return this.rows.get(index).get(fieldName);
	}
	
	public Map<String, String> getRow(int index) {
		return this.rows.get(index);
	}
	
	public String getFieldName(int index) {
		return this.fields.get(index).getName();
	}
	
	public String getFieldType(int index) {
		return this.fields.get(index).getType();
	}
	
	public String getFieldClass(int index) {
		return this.fields.get(index).getClassName();
	}
}
