ALTER TABLE pes_history ADD COLUMN attachment_uuid character varying(255) NULL;
ALTER TABLE pes_history ADD CONSTRAINT fk_attachment FOREIGN KEY (attachment_uuid) REFERENCES attachment(uuid);


ALTER TABLE attachment ADD COLUMN storage_key character varying(255);
ALTER TABLE pending_message DROP date, DROP file, DROP file_name, DROP message;
