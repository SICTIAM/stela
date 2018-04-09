ALTER TABLE agent ALTER COLUMN sub DROP NOT NULL;
ALTER TABLE agent ADD COLUMN imported boolean default false;