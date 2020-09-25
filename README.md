# free-data-sync
A data synchronization solution that can be controlled freely.

# Features
1. Support multiple database.
2. Through RabbitMQ we can route data flexibly.
3. You can extend your needs through stored procedures.
4. Support Blob field.

# Architecture

# How to test
1. Run the script on the databases.
2. Configure application.yml, including data source, RabbitMQ, Data consumers, and Data producer.
3. In RabbitMQ, create exchange and quene, bind exchange and quene.
4. Start data.sync.Application.
5. Insert data to data2sync, for example:
````sql
set @sn = uuid();

insert into data2sync (sync_sn, sync_mode, sync_time, data_type, data_name, pk_name, pk_value, source)
select @sn, '', now(), 'wp', 'wp_posts', 'ID', id, ''
from wp_posts;
````
6. Observe results.

Author: caowm (remobjects@qq.com)





