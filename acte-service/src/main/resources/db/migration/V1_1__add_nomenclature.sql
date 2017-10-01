ALTER TABLE acte ADD COLUMN local_authority_uuid varchar(255);

alter table acte add constraint FKaxchbb12tyntriooibbg29cpk foreign key (local_authority_uuid) references local_authority;

ALTER TABLE acte_history ALTER message TYPE VARCHAR(1024);

ALTER TABLE local_authority ADD COLUMN nomenclature_date date;
ALTER TABLE local_authority ADD COLUMN nomenclature_file bytea;
