const modules = [
    "ACTES",
    "PES",
    "CONVOCATION"
]

const natures = [
    "DELIBERATIONS",
    "ARRETES_REGLEMENTAIRES",
    "ARRETES_INDIVIDUELS",
    "CONTRATS_ET_CONVENTIONS",
    "DOCUMENTS_BUDGETAIRES_ET_FINANCIERS",
    "AUTRES"
]

const status = [
    "CREATED",
    "ARCHIVE_CREATED",
    "ANTIVIRUS_OK",
    "ANTIVIRUS_KO",
    "SENT",
    "NOT_SENT",
    "ACK_RECEIVED",
    "NACK_RECEIVED",
    "CANCELLATION_ASKED",
    "CANCELLATION_ARCHIVE_CREATED",
    "ARCHIVE_TOO_LARGE",
    "ARCHIVE_SIZE_CHECKED",
    "FILE_ERROR"
]

const pesStatus = [
    "CREATED",
    "SENT",
    "NOT_SENT",
    "ACK_RECEIVED",
    "NACK_RECEIVED",
    "PES_RETOUR_RECEIVED",
    "MAX_RETRY_REACH",
    "FILE_ERROR",
    "NOTIFICATION_SENT",
    "RESENT"
]

const anomalies = [
    "ANTIVIRUS_KO",
    "NOT_SENT",
    "NACK_RECEIVED",
    "ARCHIVE_TOO_LARGE",
    "FILE_ERROR"
]

module.exports = { modules, natures, status, anomalies, pesStatus }