ALTER TABLE sesile_configuration DROP COLUMN token;
ALTER TABLE sesile_configuration DROP COLUMN secret;

ALTER TABLE local_authority ADD COLUMN secret character varying(255) NULL;
ALTER TABLE local_authority ADD COLUMN token character varying(255) NULL;
ALTER TABLE local_authority ADD COLUMN sesile_subscription boolean;


