create table material_code (uuid varchar(255) not null,code varchar(255),label varchar(255),local_authority_uuid varchar(255), primary key (uuid));
alter table material_code add constraint fk_material_code foreign key (local_authority_uuid) references local_authority;
