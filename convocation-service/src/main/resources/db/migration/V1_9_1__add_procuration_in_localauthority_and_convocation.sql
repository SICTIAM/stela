alter table local_authority add default_procuration_uuid varchar(255);
alter table convocation add procuration_uuid varchar(255);
alter table if exists local_authority add constraint FK765o062jervyimni7fpuqwe22 foreign key (default_procuration_uuid) references attachment;
alter table if exists convocation add constraint FKc8bqfmb5j7357mts14rg37as0 foreign key (procuration_uuid) references attachment;

drop table if exists convocation_histories;