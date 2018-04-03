ALTER TABLE acte ADD COLUMN imported boolean default false;
ALTER TABLE local_authority ADD COLUMN migration_status character varying(255);