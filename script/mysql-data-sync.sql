
drop table if exists DATA2SYNC;
CREATE TABLE DATA2SYNC (
  SYNC_SN VARCHAR(50)  NOT NULL COMMENT 'package serial number：same package for same sn',
  SYNC_MODE VARCHAR(5)  NOT NULL DEFAULT 'UC' COMMENT 'sync mode：(D)elete (C)reate (U)pdate',
  SYNC_TIME DATETIME NOT NULL COMMENT 'create time',
  SYNC_ORD TINYINT(4) DEFAULT 1 NOT NULL COMMENT 'data order',
  DATA_TYPE VARCHAR(24)  DEFAULT '' COMMENT 'data category',
  DATA_NAME VARCHAR(32)  DEFAULT '' COMMENT 'table name',
  DATA_DATE VARCHAR(10)  DEFAULT '' COMMENT 'date label',
  PK_NAME VARCHAR(32)  DEFAULT '' NOT NULL COMMENT 'key name',
  PK_VALUE VARCHAR(64)  DEFAULT '' NOT NULL COMMENT 'key value',
  SOURCE VARCHAR(24)  DEFAULT '' NOT NULL COMMENT 'routing for mq and data source name for db',
  WORKING INT(11) DEFAULT 0 NOT NULL COMMENT 'increment per sync',
  WORKER VARCHAR(16)  DEFAULT '' NOT NULL COMMENT 'reset after sync failed'
);

drop procedure if exists DATA_SYNC_OUT_BEGIN;
create procedure DATA_SYNC_OUT_BEGIN (
	DataTypeIn varchar(24), 
	SourceIn varchar(24),
	WorkerIn varchar(16), 
	out SyncSN varchar(50), 
	out SyncDate varchar(20), 
	out DataTypeOut varchar(24), 
	out DataDate varchar(10), 
	out DataName varchar(32), 
	out PKName varchar(32), 
	out PKValue varchar(64),
	out SourceOut varchar(24)
)
begin
	-- =============================================
	-- Author:		caowm
	-- Create date: 2020-09-25
	-- Description:	1 Get sync out package serial num
	--              2 Lock the data
	-- =============================================
	start transaction;
	
	select WORKER from DATA2SYNC where WORKING = -1 for update;

	set SyncSN = ''; 
	
	-- Get sync sn
	select DATA_TYPE, DATA_NAME, SOURCE, ifnull(DATA_DATE, ''), SYNC_SN,
		DATE_FORMAT(SYNC_TIME, '%Y-%m-%d %H:%I:%S'), PK_NAME, PK_VALUE
	into DataTypeOut, DataName, SourceOut, DataDate, SyncSN, SyncDate, PKName, PKValue
	from DATA2SYNC
	where 
		WORKING >=0 and WORKING <3 and (WORKER = '')
		and (SourceIn = '' or SOURCE = SourceIn)
		and (DataTypeIn = '' or DATA_TYPE = DataTypeIn)
	order by WORKING, SYNC_TIME, SYNC_ORD
	limit 1;

	if (SyncSN <> '') then
		-- lock the sync data
		update DATA2SYNC
		set WORKING = WORKING + 1,
			WORKER = WorkerIn
		where SYNC_SN = SyncSN;
	end if;
	
	commit;
end;

DROP PROCEDURE IF EXISTS DATA_SYNC_OUT_NAMES;
create procedure DATA_SYNC_OUT_NAMES(
	SyncSN varchar(50), 
	DataType varchar(24), 
	DataDate varchar(10),
	Source varchar(24), 
	Worker varchar(16) 
)
begin
	-- =============================================
	-- Author:		caowm
	-- Create date: 2020-09-25
	-- Description:	get sync out data names 
	-- =============================================
	select SYNC_ORD, DATA_NAME, PK_NAME, SYNC_MODE
	from DATA2SYNC
	where SYNC_SN = SyncSN
	group by SYNC_ORD, DATA_NAME, PK_NAME, SYNC_MODE
	order by SYNC_ORD;
end;



DROP PROCEDURE IF EXISTS DATA_SYNC_OUT_DATA;
create procedure DATA_SYNC_OUT_DATA (
	SyncSN varchar(50), 
	SyncMode varchar(5), 
	DataType varchar(24), 
	DataDate varchar(10),
	DataName varchar(32), 
	PKName varchar(32), 
	Source varchar(24), 
	Worker varchar(16) 
)
begin
	-- ========================================================================
	-- Author:		caowm
	-- Create date: 2020-09-25
	-- Description:	Get one sync out data
	-- ========================================================================	
	declare SqlText nvarchar(4000);
	declare Fields nvarchar(2000);
	
	set Fields = '*';
	set @SyncMode = ifnull(SyncMode, '');
	set @SyncSN = SyncSN;
	set @DataName = DataName;
	
	if @SyncMode = 'delete' then 
		set SqlText = concat('select PK_VALUE as ', PKName, 
			' from DATA2SYNC 
			where SYNC_SN = @SyncSN
			and DATA_NAME = @DataName
			and SYNC_MODE = @SyncMode');
	else	
		set SqlText = concat('select ', Fields, 
			' from ', DataName, 
			' where ', PKName,
			' in (select PK_VALUE from DATA2SYNC ',
			' where SYNC_SN = @SyncSN', 
			' and DATA_NAME = @DataName ',
			' and SYNC_MODE = @SyncMode)');
	end if;
	
	set @SqlText = SqlText;

	prepare stmt from @SqlText;
	execute stmt;
	deallocate prepare stmt;
end;


DROP PROCEDURE IF EXISTS DATA_SYNC_OUT_END;
create procedure DATA_SYNC_OUT_END (
	SyncSN varchar(50), 
	SyncResult varchar(1),
	DataType varchar(24),
	DataDate varchar(10),
	DataName varchar(32),
	Source varchar(24),
	Worker varchar(16)
)
begin
	-- =============================================
	-- Author:		caowm
	-- Create date: 2020-09-25
	-- Description:	Commit sync out result
	-- =============================================
	if (SyncResult = 'Y') then
		-- delete if success
		delete from DATA2SYNC
		where SYNC_SN = SyncSN;
	else
		-- else reset worker
		update DATA2SYNC
		set WORKER = ''
		where SYNC_SN = SyncSN;	
	end if;
	
end;



DROP PROCEDURE IF EXISTS DATA_SYNC_IN_BEGIN;
create procedure DATA_SYNC_IN_BEGIN (
	SyncSN varchar(50),
	SyncDate varchar(20),
	DataType varchar(24),
	DataDate varchar(10),
	DataName varchar(32),
	PKName varchar(32), 
	PKValue varchar(64), 
	Source varchar(24),
	Worker varchar(16),
	out SqlText varchar(2000)
)
begin
	-- ==========================================================================
	-- Author:		caowm
	-- Create date: 2020-09-25
	-- Description:	Prepare for importing data
	-- ==========================================================================
	-- return empty string, then system auto build sql
	set SqlText = '';
end;


DROP PROCEDURE IF EXISTS DATA_SYNC_IN_END;
create procedure DATA_SYNC_IN_END (
	SyncSN varchar(50), 
	SyncDate varchar(30),
	DataType varchar(24),
	DataDate varchar(10),
	DataName varchar(32),
	PKName varchar(32),
	PKValue varchar(64),
	Source varchar(24),
	Worker varchar(16)
) 
begin
	-- ============================================================
	-- Author:		caowm
	-- Create date: 2020-09-25
	-- Description:	Finish import
	-- ============================================================
end;


DROP PROCEDURE IF EXISTS DATA_SYNC_IN_TEMPLATE;
create procedure DATA_SYNC_IN_TEMPLATE (
	SyncSN varchar(50),
	SyncDate varchar(20),
	DataType varchar(24),
	DataDate varchar(10),
	DataName varchar(32),
	PKName varchar(32),
	PKValue varchar(64), 
	Source varchar(24), 
	JsonData text
)
begin	
	
end;


