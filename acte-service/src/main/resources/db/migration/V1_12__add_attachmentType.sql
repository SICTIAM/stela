CREATE TABLE attachment_type_referencial (
    uuid varchar(255) NOT NULL,
    acte_nature int4,
    local_authority_uuid varchar(255),
    primary key (uuid)
);

alter table attachment_type_referencial add constraint fk_attachment_type_ref foreign key (local_authority_uuid) references local_authority;

CREATE TABLE attachment_type (
    uuid varchar(255) NOT NULL,
    code varchar(255),
    label varchar(255),
    attachment_type_referencial_uuid varchar(255),
    primary key (uuid)
);


ALTER TABLE ONLY attachment_type ADD CONSTRAINT fk_attachment_type FOREIGN KEY (attachment_type_referencial_uuid) REFERENCES attachment_type_referencial(uuid);

alter table attachment add column attachment_type_uuid character varying(255);

ALTER TABLE ONLY attachment ADD CONSTRAINT fk_attachment_to_type FOREIGN KEY (attachment_type_uuid) REFERENCES attachment_type(uuid);