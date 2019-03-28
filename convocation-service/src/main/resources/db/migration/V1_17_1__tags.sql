create table attachment_tag (attachment_uuid varchar(255) not null, tag_uuid varchar(255) not null, primary key (attachment_uuid, tag_uuid));
create table tag (uuid varchar(255) not null, name varchar(32), color varchar(8), icon varchar(128), local_authority_uuid varchar(255), primary key (uuid));

alter table if exists attachment_tag add constraint FKk42ahx0qo52db23h68h0rwum foreign key (tag_uuid) references tag;
alter table if exists attachment_tag add constraint FKbesdfklvxuekdciidgnevl0qa foreign key (attachment_uuid) references attachment;
alter table if exists tag add constraint FK5lt5upq21tjcsmslpf6bwued2 foreign key (local_authority_uuid) references local_authority;