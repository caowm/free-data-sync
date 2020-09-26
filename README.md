# free-data-sync
A data synchronization solution that can be controlled freely.

# Features
1. Support multiple data source and heterogeneous database.
2. Through RabbitMQ we can route data flexibly.
3. You can extend your needs through stored procedures.
4. Support Blob field.

# Architecture

![design diagram](https://github.com/caowm/free-data-sync/blob/master/doc/data-sync-design.png)

# How to test
1. Run the script on the databases.
2. Configure application.yml, including data sources, RabbitMQ, Data consumers, and Data producers.
3. In RabbitMQ, create exchanges and queues, bind exchanges and queues.
4. Start data.sync.Application.
5. Insert data to data2sync, for example:
````sql
set @sn = uuid();

insert into data2sync (sync_sn, sync_mode, sync_time, data_type, data_name, pk_name, pk_value, source)
select @sn, '', now(), 'wp', 'wp_posts', 'ID', id, ''
from wp_posts;
````
6. Observe the results.

Author: caowm (remobjects@qq.com)







