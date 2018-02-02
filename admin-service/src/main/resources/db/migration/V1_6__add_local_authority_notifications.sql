CREATE TABLE profile_local_authority_notifications (
    profile_uuid character varying(255) NOT NULL,
    local_authority_notifications character varying(255)
);

ALTER TABLE ONLY profile_local_authority_notifications
    ADD CONSTRAINT fk_profile_local_authority_notifications FOREIGN KEY (profile_uuid) REFERENCES profile(uuid);