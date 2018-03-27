SELECT
messages_PES.`message_id` as message_id,
messages_PES.`title` as objet,
messages_PES.`comment`,
enveloppe_pes_PES.`date_enveloppe_pes` as creation,
enveloppe_pes_PES.`codfich_enveloppe_pes` as file_type,
enveloppe_pes_PES.`codcol_enveloppe_pes` as col_code,
enveloppe_pes_PES.`idpost_enveloppe_pes` as post_id,
enveloppe_pes_PES.`codbud_enveloppe_pes` as bud_code,
enveloppe_pes_PES.`nomfic_enveloppe_pes` as file_name,
enveloppe_pes_PES.`metier_list_enveloppe_pes` as pesAttachment,
enveloppe_pes_PES.`taille` as pesAttachmentSize,
messages_PES.`archivename` as archivePath,
enveloppe_pes_PES.`dateOutGoing_enveloppe_pes` as sendDate,
messages_PES.`status`,
enveloppe_pes_ar_AR.`date_enveloppe_pes_ar` as dateAR,
enveloppe_pes_ar_AR.`metier_list_enveloppe_pes_ar` as filenameAR,
anomalies_ANO.`date_anomalies` as dateANO,
anomalies_ANO.`moreDetail` as messageANO,
anomalies_ANO.`metier_list_anomalies` as filenameANO,
messages_ANO.`archivename` as pathFilenameANO

FROM
`stela_stelahelios_enveloppe_pes` enveloppe_pes_PES
	LEFT JOIN `stela_stela_messages` messages_PES ON enveloppe_pes_PES.`message_id` = messages_PES.`message_id`
	LEFT JOIN `stela_stelahelios_enveloppe_pes_ar` enveloppe_pes_ar_AR ON enveloppe_pes_PES.`message_id` = enveloppe_pes_ar_AR.`message_origin_id`
	LEFT JOIN `stela_stelahelios_anomalies` anomalies_ANO ON enveloppe_pes_PES.`message_id` = anomalies_ANO.`message_origin_id`
	LEFT JOIN `stela_stela_messages` messages_ANO ON anomalies_ANO.`message_id` = messages_ANO.`message_id`

WHERE
messages_PES.`user_groupid` =
(
	SELECT `groupid`
	FROM `stela_stelahelios_liaison`
	WHERE `idcoll` LIKE '%{{siren}}%'
)