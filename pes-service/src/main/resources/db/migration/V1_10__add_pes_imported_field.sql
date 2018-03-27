ALTER TABLE pes_aller ADD COLUMN imported boolean default false;
ALTER TABLE local_authority ADD COLUMN migration_status character varying(255);