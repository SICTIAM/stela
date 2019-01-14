SELECT
form_actes_ACTE.`form_id`,
form_actes_ACTE.`num_acte` as number,
form_actes_ACTE.`objet`,
forms_ACTE.`form_type`,
messages_ACTE.`dateIncoming` as creation,
enveloppesCLMISILL_ACTE.`dateOutgoing` as sendDate,
form_actes_ACTE.`date_acte` as decision,
form_actes_ACTE.`code_matiere1`,
form_actes_ACTE.`code_matiere2`,
form_actes_ACTE.`code_matiere3`,
form_actes_ACTE.`code_matiere4`,
form_actes_ACTE.`code_matiere5`,
form_actes_ACTE.`code_matiere_nom` as code_label,
form_actes_ACTE.`affichage_registre` as is_public,
form_actes_ACTE.`affichage_sur_site` as is_public_website,
form_actes_ACTE.`code_nature` as nature,
messages_ACTE.`archivename` as archivePath,
form_actes_ACTE.`document_filename` as acteAttachment,
form_actes_ACTE.`annexes_list` as annexes,
messages_ACTE.`status`,
messages_AR.`dateIncoming` as dateAR,
messages_AR.`archivename` as archivePathAR,
enveloppesMISILLCL_AR.`metier_list` as filenameAR,
form_acte_anomalies_ANO.`date_notification` as dateANO,
messages_ANO.`archivename` as archivePathANO,
form_acte_anomalies_ANO.`form_filename` as filenameANO,
form_acte_anomalies_ANO.`detail` as messageANO,
enveloppesCLMISILL_ASK_CANCEL.`dateOutgoing` as dateASKCANCEL,
messages_ASK_CANCEL.`archivename` as archivePathASKCANCEL,
enveloppesCLMISILL_ASK_CANCEL.`metier_list` as filenameASKCANCEL,
form_acte_annulation_ar_AR_CANCEL.`date_reception` as dateARCANCEL,
messages_AR_CANCEL.`archivename` as archivePathARCANCEL,
enveloppesMISILLCL_AR_CANCEL.`metier_list` as filenameARCANCEL

FROM
`stela_stelamiat_form_actes` form_actes_ACTE
	LEFT JOIN `stela_stelamiat_forms` forms_ACTE ON forms_ACTE.`form_id` = form_actes_ACTE.`form_id`
	LEFT JOIN `stela_stela_messages` messages_ACTE ON forms_ACTE.`enveloppe_id` = messages_ACTE.`message_id`
	LEFT JOIN `stela_stelamiat_enveloppesCLMISILL` enveloppesCLMISILL_ACTE ON forms_ACTE.`enveloppe_id` = enveloppesCLMISILL_ACTE.`message_id`
  LEFT JOIN `stela_stelamiat_form_acte_ar` form_acte_ar_ACTE ON form_actes_ACTE.`ar_id` = form_acte_ar_ACTE.`form_id`
  LEFT JOIN `stela_stelamiat_forms` forms_AR ON form_acte_ar_ACTE.`form_id` = forms_AR.`form_id`
  LEFT JOIN `stela_stela_messages` messages_AR ON forms_AR.`enveloppe_id` = messages_AR.`message_id`
  LEFT JOIN `stela_stelamiat_enveloppesMISILLCL` enveloppesMISILLCL_AR ON messages_AR.`message_id` = enveloppesMISILLCL_AR.`message_id`
	LEFT JOIN `stela_stelamiat_form_acte_anomalies` form_acte_anomalies_ANO ON forms_ACTE.`form_id` = form_acte_anomalies_ANO.`acte_id`
	LEFT JOIN `stela_stelamiat_forms` forms_ANO ON form_acte_anomalies_ANO.`form_id` = forms_ANO.`form_id`
	LEFT JOIN `stela_stela_messages` messages_ANO ON forms_ANO.`enveloppe_id` = messages_ANO.`message_id`
	LEFT JOIN `stela_stelamiat_form_acte_annulations` form_acte_annulations_ASK_CANCEL ON form_actes_ACTE.`annulation_id` = form_acte_annulations_ASK_CANCEL.`form_id`
	LEFT JOIN `stela_stelamiat_forms` forms_ASK_CANCEL ON form_actes_ACTE.`annulation_id` = forms_ASK_CANCEL.`form_id`
	LEFT JOIN `stela_stela_messages` messages_ASK_CANCEL ON forms_ASK_CANCEL.`enveloppe_id` = messages_ASK_CANCEL.`message_id`
	LEFT JOIN `stela_stelamiat_enveloppesCLMISILL` enveloppesCLMISILL_ASK_CANCEL ON forms_ASK_CANCEL.`enveloppe_id` = enveloppesCLMISILL_ASK_CANCEL.`message_id`
	LEFT JOIN `stela_stelamiat_form_acte_annulation_ar` form_acte_annulation_ar_AR_CANCEL ON form_acte_annulations_ASK_CANCEL.`annulation_ar_id` = form_acte_annulation_ar_AR_CANCEL.`form_id`
	LEFT JOIN `stela_stelamiat_forms` forms_AR_CANCEL ON form_acte_annulations_ASK_CANCEL.`annulation_ar_id` = forms_AR_CANCEL.`form_id`
	LEFT JOIN `stela_stela_messages` messages_AR_CANCEL ON forms_AR_CANCEL.`enveloppe_id` = messages_AR_CANCEL.`message_id`
	LEFT JOIN `stela_stelamiat_enveloppesMISILLCL` enveloppesMISILLCL_AR_CANCEL ON messages_AR_CANCEL.`message_id` = enveloppesMISILLCL_AR_CANCEL.`message_id`

WHERE
FROM_UNIXTIME(messages_ACTE.`dateIncoming`) >= NOW() - INTERVAL {{month}} month AND
forms_ACTE.`env_siren` = {{siren}} and
forms_ACTE.`form_type` = 1