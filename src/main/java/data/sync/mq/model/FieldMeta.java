package data.sync.mq.model;

/**
 * Field Meta
 *
 * @author caowm 2020-09-20
 * 
 */

public class FieldMeta {

	private String name;

	private String type;

	private int size;

	private String className;

	public FieldMeta() {

	}

	public FieldMeta(String name, String type, int size, String className) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
