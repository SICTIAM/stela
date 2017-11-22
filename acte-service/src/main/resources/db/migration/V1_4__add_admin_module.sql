create table admin (uuid varchar(255) not null,main_email varchar(255), primary key (uuid));
create table additional_emails (admin_uuid varchar(255) not null, additional_email varchar(255));
alter table additional_emails add constraint fk_mails foreign key (admin_uuid) references admin;