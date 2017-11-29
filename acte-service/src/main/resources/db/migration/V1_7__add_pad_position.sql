create table stamp_position (uuid varchar(255) not null, x int, y int, primary key (uuid));
alter table local_authority add column stamp_position_uuid varchar(255);

create function stamp_position_migration() returns void as $$
  declare
    current_rec record;
    stamp_uuid varchar(255);
  begin
    for current_rec in select uuid from local_authority loop
      stamp_uuid := uuid_in(md5(random()::text || now()::text)::cstring);
      insert into stamp_position (uuid, x, y) values (stamp_uuid, 10, 10);
      update local_authority set stamp_position_uuid = stamp_uuid where uuid = current_rec.uuid;
    end loop;
    RETURN;
  end;
$$ language plpgsql;
select stamp_position_migration();
drop function stamp_position_migration();