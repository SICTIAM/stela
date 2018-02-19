ALTER TABLE pes_aller ADD COLUMN sesile_document_id int;

CREATE TABLE sesile_configuration (
    profile_uuid character varying(255) NOT NULL,
    visibility int NULL,
    days_to_validated int NULL,
    service_organisation_number int NULL,
    type int NULL,   
    token character varying(255) NOT NULL,
    secret character varying(255) NOT NULL
);
