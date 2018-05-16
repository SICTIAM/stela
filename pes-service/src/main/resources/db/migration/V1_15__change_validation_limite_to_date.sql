ALTER TABLE pes_aller DROP COLUMN days_to_validated;
ALTER TABLE pes_aller ADD COLUMN validation_limit date NULL;
