CREATE TABLE certificate (
    uuid character varying(255) NOT NULL,
    serial character varying(400),
    issuer character varying(400),
    subject_common_name character varying(255),
    subject_organization character varying(255),
    subject_organization_unit character varying(255),
    subject_email character varying(255),
    issuer_common_name character varying(255),
    issuer_organization character varying(255),
    issuer_email character varying(255),
    issued_date date,
    expired_date date,
    primary key (uuid)
);

ALTER TABLE agent ADD COLUMN certificate_uuid varchar(255);

ALTER TABLE agent ADD CONSTRAINT FK29scnRh84V9gxRQ7iBYh27U5N FOREIGN KEY (certificate_uuid) REFERENCES certificate;