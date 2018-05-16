CREATE TABLE generic_document (
    sesile_classeur_id int NOT NULL,
    sesile_document_id int NOT NULL,
    service_organisation_number int NOT NULL,
    creation timestamp without time zone NOT NULL,
	deposit_email character varying(255) NOT NULL,
	local_authority_uuid character varying(255) NOT NULL
);

ALTER TABLE ONLY generic_document
    ADD CONSTRAINT generic_document_pkey PRIMARY KEY (sesile_classeur_id);
    
ALTER TABLE ONLY generic_document
    ADD CONSTRAINT fk_generic_document_local_authority FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);