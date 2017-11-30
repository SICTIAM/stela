create table pending_message (uuid varchar(255) not null, acte_uuid varchar(255), date timestamp, file bytea, file_name varchar(255), message varchar(255), status varchar(255), primary key (uuid))


