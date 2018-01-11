alter table admin add column miat_accessible boolean not null default true;
alter table admin add column inaccessibility_miat_start_date timestamp not null default CURRENT_TIMESTAMP;
alter table admin add column inaccessibility_miat_end_date timestamp not null default CURRENT_TIMESTAMP;