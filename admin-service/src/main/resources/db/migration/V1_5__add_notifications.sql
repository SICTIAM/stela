CREATE TABLE notification_value (
    uuid character varying(255) NOT NULL,
    name character varying(255),
    active boolean,
    profile_uuid character varying(255) NOT NULL
);

ALTER TABLE ONLY notification_value
    ADD CONSTRAINT fk_profile_notifications FOREIGN KEY (profile_uuid) REFERENCES profile(uuid);

ALTER TABLE profile ADD COLUMN email VARCHAR(255);