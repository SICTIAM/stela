alter table convocation add minutes_uuid varchar(255);
alter table if exists convocation add constraint FKc8bqfmb5j6543mts14rg37as0 foreign key (minutes_uuid) references attachment;

