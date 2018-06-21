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

const anomalies = [
    "ANTIVIRUS_KO",
    "NOT_SENT",
    "NACK_RECEIVED",
    "ARCHIVE_TOO_LARGE",
    "FILE_ERROR"
]

const sesileVisibility = [0, 1, 3]

const materialCodeBudgetaire = "7-1-"

// Default period we have to keep actes before archiving. 
// It also corresponds to the number of months of actes that must be migrated.
const monthBeforeArchiving = 6

const daysBeforeResendPes = 2

module.exports = { modules, natures, status, anomalies, sesileVisibility, materialCodeBudgetaire, monthBeforeArchiving, daysBeforeResendPes }