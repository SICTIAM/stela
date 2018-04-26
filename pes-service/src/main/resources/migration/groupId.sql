SELECT `groupid`
FROM `stela_stelahelios_liaison`
WHERE `idcoll` LIKE '%{{siren}}%'
LIMIT 0, 1