alter table admin add column miat_available boolean not null default true;
alter table admin add column unavailability_miat_start_date timestamp not null default CURRENT_TIMESTAMP;
alter table admin add column unavailability_miat_end_date timestamp not null default CURRENT_TIMESTAMP;