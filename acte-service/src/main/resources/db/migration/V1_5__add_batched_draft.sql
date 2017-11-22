create table draft (uuid varchar(255) not null, last_modified timestamp, mode varchar(255), primary key (uuid));
alter table acte add column draft_uuid varchar(255) default null;
alter table acte add constraint FK4nEQt5P322AiTbn3STe9Y6t5f foreign key (draft_uuid) references draft;

create function draft_migration() RETURNS void as $$
  declare
    current_rec RECORD;
    draft_tmp_var boolean;
    draft_uuid_var varchar(255);
  begin
    for current_rec in select uuid, draft from acte LOOP
      draft_uuid_var := uuid_in(md5(random()::text || now()::text)::cstring);
      draft_tmp_var := (select draft from acte where uuid = current_rec.uuid);
      if draft_tmp_var = true then
        update acte set draft_uuid = draft_tmp_var where uuid = current_rec.uuid;
        insert into draft (uuid, last_modified, mode) values (draft_uuid_var, localtimestamp, "ACTE");
      end if;
    end LOOP;
    return;
  end;
$$ language plpgsql;
select draft_migration();
drop function draft_migration();

alter table acte drop column draft;
