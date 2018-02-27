ALTER TABLE acte_history ADD COLUMN flux varchar(255);

ALTER TABLE pending_message DROP COLUMN date;
ALTER TABLE pending_message DROP COLUMN message;
ALTER TABLE pending_message DROP COLUMN status;

ALTER TABLE pending_message ADD COLUMN flux varchar(255);



