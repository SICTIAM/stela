create table acte (uuid varchar(255) not null, code varchar(255), creation timestamp, decision date, file bytea, filename varchar(255), is_public boolean not null, is_public_website boolean not null, nature int4, number varchar(255), objet varchar(255), primary key (uuid));
create table acte_acte_histories (acte_uuid varchar(255) not null, acte_histories_uuid varchar(255) not null, primary key (acte_uuid, acte_histories_uuid));
create table acte_annexes (acte_uuid varchar(255) not null, annexes_uuid varchar(255) not null);
create table acte_history (uuid varchar(255) not null, acte_uuid varchar(255), date timestamp, file bytea, file_name varchar(255), message varchar(255), status varchar(255), primary key (uuid));
create table attachment (uuid varchar(255) not null, file bytea, filename varchar(255), primary key (uuid));
create table enveloppe_counter (date date not null, counter int4, primary key (date));
create table local_authority (uuid varchar(255) not null, can_publish_registre boolean, can_publish_web_site boolean, department varchar(255), district varchar(255), name varchar(255), nature varchar(255), siren varchar(255), primary key (uuid));
alter table acte add constraint UK_g8a6f1nhlfwy054tlc8vjobcx unique (number);
alter table acte_acte_histories add constraint UK_285hogda5da15lr5bhdrh4u4h unique (acte_histories_uuid);
alter table acte_annexes add constraint UK_5n0ajc437mrv7jnl5jg1fa7nx unique (annexes_uuid);
alter table acte_acte_histories add constraint FKh54mtxv2uytlqmw51j3u70ddw foreign key (acte_histories_uuid) references acte_history;
alter table acte_acte_histories add constraint FKj3spssdu8wi67qjx02mwr2vn foreign key (acte_uuid) references acte;
alter table acte_annexes add constraint FK5dsqis78rvpn7ft7fc6fosy6h foreign key (annexes_uuid) references attachment;
alter table acte_annexes add constraint FKfwmup5p8y63j8auhn7mj2ef7r foreign key (acte_uuid) references acte;

