alter table acte
  add column miat_id varchar(255);

update acte
  set miat_id = (select concat((select department from local_authority where local_authority.uuid = acte.local_authority_uuid), '-',
                               (select siren from local_authority where local_authority.uuid = acte.local_authority_uuid), '-',
                               to_char(decision, 'YYYYMMdd'), '-',
                               number, '-',
                               CASE
                                 WHEN nature = '0' THEN 'DE'
                                 WHEN nature = '1' THEN 'AR'
                                 WHEN nature = '2' THEN 'AI'
                                 WHEN nature = '3' THEN 'CC'
                                 WHEN nature = '4' THEN 'BF'
                                 WHEN nature = '5' THEN 'AU'
                                 ELSE ''
                              END)
                );
