ALTER TABLE pes_aller ADD COLUMN sesile_document_id int;

CREATE TABLE sesile_configuration (
    profile_uuid character varying(255) NOT NULL,
    visibility int NOT NULL,
    validation_limit int NOT NULL,
    service_organisation_number int NOT NULL,
    type int NOT NULL,   
    token character varying(255) NOT NULL,
    secret character varying(255) NOT NULL
);
