package data.sync.mq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataProcessorProperties {

	public static final String DATA_TAG_SYNC_SN = "SYNC_SN";
	public static final String DATA_TAG_SYNC_DATE = "SYNC_DATE";
	public static final String DATA_TAG_DATA_TYPE = "DATA_TYPE";
	public static final String DATA_TAG_DATA_DATE = "DATA_DATE";
	public static final String DATA_TAG_DATA_NAME = "DATA_NAME";
	public static final String DATA_TAG_PK_NAME = "PK_NAME";
	public static final String DATA_TAG_PK_VALUE = "PK_VALUE";
	public static final String DATA_TAG_SOURCE = "SOURCE";
	public static final String DATA_TAG_WORK = "WORKER";
	
	@Value("${sync-sql.syncoutbegin}")
	public String syncOutBeginSql;
	
	@Value("${sync-sql.syncoutnames}")
	public String syncOutNamesSql;
	
	@Value("${sync-sql.syncoutdata}")
	public String syncOutDataSql;
	
	@Value("${sync-sql.syncoutend}")
	public String syncOutEndSql;

	@Value("${sync-sql.syncinbegin}")
	public String syncInBeginSql;
	
	@Value("${sync-sql.syncinend}")
	public String syncInEndSql;
	
}
