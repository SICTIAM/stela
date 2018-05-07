ALTER TABLE local_authority ADD COLUMN archive_activated boolean default false;
ALTER TABLE local_authority ADD COLUMN pastell_url character varying(255);
ALTER TABLE local_authority ADD COLUMN pastell_entity character varying(255);
ALTER TABLE local_authority ADD COLUMN pastell_login character varying(255);
ALTER TABLE local_authority ADD COLUMN pastell_password character varying(255);
ALTER TABLE pes_aller ADD COLUMN archived boolean default false;