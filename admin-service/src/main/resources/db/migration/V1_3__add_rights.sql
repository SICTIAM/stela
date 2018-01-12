CREATE TABLE work_group_rights (
    work_group_uuid character varying(255) NOT NULL,
    rights character varying(255)
);

ALTER TABLE ONLY work_group_rights
    ADD CONSTRAINT fk_rights FOREIGN KEY (work_group_uuid) REFERENCES work_group(uuid);
