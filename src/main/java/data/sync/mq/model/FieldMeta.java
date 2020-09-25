package data.sync.mq.model;

public class FieldMeta {
	// 字段名
	private String name;
	// 字段类型
	private String type;
	// 字段大小
	private int size;
	// java类型名
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
