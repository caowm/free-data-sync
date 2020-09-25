package data.sync.mq.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Packaged Data
 *
 * @author caowm 2020-09-20
 * 
 */

public class MQData implements Serializable{

	private static final long serialVersionUID = 1L;
	// data name = job type
	private String dataType;
	// source, can be mq routing key or data source name when importing
	private String source;
	// data time
	private String date;
	// data tags
	private Map<String, String> dataTags;
	// data sets
	private List<MQDataSet> dataSets;
	
	public MQData() {
		dataTags = new HashMap<String, String>();
		dataSets = new ArrayList<MQDataSet>();
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Map<String, String> getDataTags() {
		return dataTags;
	}

	public void setDataTags(Map<String, String> dataTags) {
		this.dataTags = dataTags;
	}

	public List<MQDataSet> getDataSets() {
		return dataSets;
	}

	public void setDataSets(List<MQDataSet> dataSets) {
		this.dataSets = dataSets;
	}
	
	public MQDataSet getDataSet(int index) {
		return dataSets.get(index);
	}
	
	public void addDataSet(MQDataSet dataSet) {
		dataSets.add(dataSet);
	}
	
	public String getDataTag(String key) {
		return dataTags.get(key);
	}
	
	public void writeDataTag(String key, String value) {
		dataTags.put(key, value);
	}

}
