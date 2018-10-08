ALTER TABLE acte ADD COLUMN last_history_date timestamp without time zone;
ALTER TABLE acte ADD COLUMN last_history_status character varying(255);
ALTER TABLE acte ADD COLUMN last_history_flux character varying(255);

update acte set (last_history_date, last_history_status, last_history_flux) = (
  select date, status, flux
  from acte_history
  where acte_history.acte_uuid = acte.uuid
    and status != 'NOTIFICATION_SENT'
    and status != 'GROUP_NOTIFICATION_SENT'
  order by acte_history.date desc
  limit 1
)