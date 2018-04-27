ALTER TABLE ONLY acte DROP CONSTRAINT fk_acte_profile;

DROP TABLE group_to_profile;

DROP TABLE work_group;

DROP TABLE profile;

DROP TABLE agent;

alter table acte add column group_uuid character varying(255);

