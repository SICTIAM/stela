CREATE TABLE profile_notifications (
    profile_uuid character varying(255) NOT NULL,
    notifications character varying(255)
);

ALTER TABLE ONLY profile_notifications
    ADD CONSTRAINT fk_profile_notifications FOREIGN KEY (profile_uuid) REFERENCES profile(uuid);

ALTER TABLE profile ADD COLUMN email VARCHAR(255);