ALTER TABLE acte ADD COLUMN imported boolean default false;
ALTER TABLE local_authority ADD COLUMN migration_users character varying(255);
ALTER TABLE local_authority ADD COLUMN migration_data character varying(255);
ALTER TABLE local_authority ADD COLUMN migration_users_deactivation character varying(255);