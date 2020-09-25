package data.sync.mq.util;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.sync.mq.model.*;

public class DataUtil {


	/**
	 * Convert ResultSet to MQDataSet
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	public static MQDataSet toMQDataSet(ResultSet resultSet, String dataName,
			String pkName, String syncMode) throws SQLException {

		MQDataSet dataSet = new MQDataSet();
		dataSet.setDataName(dataName);
		dataSet.setPkName(pkName);
		dataSet.setSyncMode(syncMode);

		// convert meta data
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		Boolean findKeyField = false;
		List<FieldMeta> fields = new ArrayList<FieldMeta>();
		for (int i = 1; i <= columnCount; i++) {
			FieldMeta fieldMeta = new FieldMeta(metaData.getColumnLabel(i), metaData.getColumnTypeName(i),
					metaData.getColumnDisplaySize(i), metaData.getColumnClassName(i));
			fields.add(fieldMeta);
			
			if (!findKeyField)
				findKeyField = metaData.getColumnLabel(i).equals(pkName);

		}
		dataSet.setFields(fields);
		
		if (!findKeyField) {
			throw new SQLException("Can't find key field(case sensitive)：" + pkName);
		}

		// convert rows
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
		while (resultSet.next()) {
			Map<String, String> rowData = new HashMap<String, String>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				// String column type = metaData.getColumnTypeName(i).toLowerCase();		
				String columnClass = metaData.getColumnClassName(i);
				rowData.put(metaData.getColumnName(i), getColumnValue(resultSet, i, columnClass));
			}
			rows.add(rowData);
		}
		dataSet.setRows(rows);

		return dataSet;
	}
	
	/**
	 * get column value by column type
	 * 
	 * @param resultSet
	 * @param colNum
	 * @param columnType
	 * @return
	 * @throws SQLException
	 */
	public static String getColumnValue(final ResultSet resultSet, int colNum,
			String columnClass) throws SQLException {
		// TODO: configure datetime format;
		String result = null;
		if (isBlobType(columnClass))
		{
			Blob blob = resultSet.getBlob(colNum);
			if (blob != null)
			{
				byte[] bdata = blob.getBytes(1, (int) blob.length());
				result = bytesToHex(bdata);
			}
		}
		else			
		  result = resultSet.getString(colNum);
		
		return result;		
	}
	
	/**
	 * convert result set to list of map object
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	private static List<Map<String, Object>> convertList(ResultSet rs) throws SQLException {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ResultSetMetaData md = rs.getMetaData();
		int columnCount = md.getColumnCount(); // Map rowData;

		while (rs.next()) {
			Map<String, Object> rowData = new HashMap<String, Object>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				rowData.put(md.getColumnName(i), rs.getObject(i));
			}
			list.add(rowData);

		}
		return list;
	}

	/**
	 * if a field type is blob type
	 * 
	 * @param fieldType
	 * @return
	 */
	public static boolean isBlobType(String fieldType) {
//		return (fieldType.equals("image") || fieldType.equals("longblob") || fieldType.equals("blob")
//				|| fieldType.equals("binary"));
		return "[B".equals(fieldType);
	}

	/**
	 * convert hex string to byte array
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * convert bytes array to string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null)
			return null;
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
