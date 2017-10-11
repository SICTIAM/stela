ALTER TABLE acte ADD COLUMN draft boolean not null default false;
ALTER TABLE attachment ADD COLUMN size bigint not null default 0;

ALTER TABLE acte ADD COLUMN file_uuid varchar(255);
alter table acte add constraint FKnwZXmB55c5y9f2bi92G7FXQF2 foreign key (file_uuid) references attachment;

CREATE FUNCTION acte_file_migration() RETURNS void AS $$
  DECLARE
    current_rec RECORD;
    attachment_uuid varchar(255);
  BEGIN
    FOR current_rec IN SELECT uuid, file, filename, 0 as size FROM acte LOOP
      attachment_uuid := uuid_in(md5(random()::text || now()::text)::cstring);
      insert into attachment (uuid, file, filename, size) values (attachment_uuid, current_rec.file, current_rec.filename, 0);
      update acte set file_uuid = attachment_uuid where uuid = current_rec.uuid;
    END LOOP;
    RETURN;
  END;
$$ LANGUAGE plpgsql;
SELECT acte_file_migration();
DROP FUNCTION acte_file_migration();

ALTER TABLE acte DROP COLUMN file;
ALTER TABLE acte DROP COLUMN filename;