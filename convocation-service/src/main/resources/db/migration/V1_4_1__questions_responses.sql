alter table question drop convocation_uuid;
alter table if exists question_response add recipient_uuid varchar(255);
alter table if exists question_response add constraint FKadshhmnwscdd5ut1fj7b9ajd foreign key (recipient_uuid) references recipient;
drop table question_responses cascade;