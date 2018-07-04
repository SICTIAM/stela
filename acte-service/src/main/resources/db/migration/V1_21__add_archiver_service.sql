ALTER TABLE local_authority ADD COLUMN archive_activated boolean default false;
ALTER TABLE local_authority ADD COLUMN pastell_url character varying(255);
ALTER TABLE local_authority ADD COLUMN pastell_entity character varying(255);
ALTER TABLE local_authority ADD COLUMN pastell_login character varying(255);
ALTER TABLE local_authority ADD COLUMN pastell_password character varying(255);
ALTER TABLE local_authority ADD COLUMN days_before_archiving int4;

CREATE TABLE archive (
    uuid character varying(255) NOT NULL,
    status character varying(255),
    archive_url character varying(2000),
    asalae_document_id character varying(255),
    primary key (uuid)
);

ALTER TABLE acte ADD COLUMN archive_uuid varchar(255);

ALTER TABLE acte ADD CONSTRAINT FKH8RTJ3hDWBxgn3j82x4Q6it69 FOREIGN KEY (archive_uuid) REFERENCES archive;