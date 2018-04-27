ALTER TABLE attachment DROP COLUMN attachment_type_uuid;
ALTER TABLE attachment ADD COLUMN attachment_type_code varchar(255);