CREATE TABLE generic_account (
    uuid character varying(255) NOT NULL,
    software character varying(255),
    email character varying(255),
    password character varying(255),
    serial character varying(400),
    vendor character varying(400),
    primary key (uuid)
);

CREATE TABLE generic_account_local_authorities (
    generic_account_uuid character varying(255) NOT NULL,
    local_authority_uuid character varying(255) NOT NULL
);

ALTER TABLE ONLY generic_account_local_authorities
    ADD CONSTRAINT generic_account_local_authorities_pkey PRIMARY KEY (generic_account_uuid, local_authority_uuid);
    
ALTER TABLE ONLY generic_account_local_authorities
	ADD CONSTRAINT fk_gen_account FOREIGN KEY (generic_account_uuid) REFERENCES generic_account(uuid);

ALTER TABLE ONLY generic_account_local_authorities
	ADD CONSTRAINT fk_gen_account_local_authorities FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);
