CREATE TABLE local_authority_certificate (
    local_authority_uuid character varying(255) NOT NULL,
    certificate_uuid character varying(255)
);

ALTER TABLE local_authority_certificate ADD CONSTRAINT fk_local_authority FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);
ALTER TABLE local_authority_certificate ADD CONSTRAINT fk_certificate FOREIGN KEY (certificate_uuid) REFERENCES certificate(uuid);
