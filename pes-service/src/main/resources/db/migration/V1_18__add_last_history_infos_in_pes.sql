ALTER TABLE pes_aller ADD COLUMN last_history_date timestamp without time zone;
ALTER TABLE pes_aller ADD COLUMN last_history_status character varying(255);

update pes_aller set (last_history_date, last_history_status) = (
  select date, status
  from pes_history
  where pes_history.pes_uuid = pes_aller.uuid
  order by pes_history.date desc
  limit 1
)