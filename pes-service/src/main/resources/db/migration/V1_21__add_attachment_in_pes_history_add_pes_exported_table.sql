ALTER TABLE pes_history ADD COLUMN attachment_uuid character varying(255) NULL;
ALTER TABLE pes_history ADD CONSTRAINT fk_attachment FOREIGN KEY (attachment_uuid) REFERENCES attachment(uuid);


ALTER TABLE attachment ADD COLUMN storage_key character varying(255);
ALTER TABLE pending_message DROP date, DROP file, DROP file_name, DROP message;

CREATE TABLE pes_export (
    uuid character varying(255) NOT NULL,
    pes_uuid character varying(255) NOT NULL,
    transmission_date_time timestamp with time zone,
    file_name character varying(255),
    size bigint NOT NULL,
    sha1 character varying(255),
    siren character varying(255),
    agent_name character varying(255),
    agent_first_name character varying(255),
    agent_email character varying(255)
);