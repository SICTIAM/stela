const modules = [
    'ACTES',
    'PES',
    // 'CONVOCATION'
]

const natures = [
    'DELIBERATIONS',
    'ARRETES_REGLEMENTAIRES',
    'ARRETES_INDIVIDUELS',
    'CONTRATS_ET_CONVENTIONS',
    'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS',
    'AUTRES'
]

const status = [
    'CREATED',
    'ARCHIVE_CREATED',
    'ANTIVIRUS_OK',
    'ANTIVIRUS_KO',
    'SENT',
    'NOT_SENT',
    'ACK_RECEIVED',
    'NACK_RECEIVED',
    'CANCELLATION_ASKED',
    'CANCELLATION_ARCHIVE_CREATED',
    'ARCHIVE_TOO_LARGE',
    'ARCHIVE_SIZE_CHECKED',
    'FILE_ERROR'
]

const anomalies = [
    'SIGNATURE_INVALID',
    'SIGNATURE_MISSING',
    'ANTIVIRUS_KO',
    'NOT_SENT',
    'NACK_RECEIVED',
    'MAX_RETRY_REACH',
    'ARCHIVE_TOO_LARGE',
    'FILE_ERROR',
    'SIGNATURE_SENDING_ERROR'
]

const sesileVisibility = [0, 1, 3]

const materialCodeBudgetaire = '7-1-'

// Default period we have to keep actes before archiving.
// It also corresponds to the number of months of actes that must be migrated.
const monthBeforeArchiving = 6

const hoursBeforeResendActe = 24
const hoursBeforeResendPes = 24

const acceptFileDocumentConvocation = '.doc, docx, .pdf, .jpg, .png, .zip, .odt, .ott, .oth, and .odm, .dwg'

const publicPages = ['/choix-collectivite', '/', '/mentions-legales', '/registre-des-deliberations']

export {
    modules,
    natures,
    status,
    anomalies,
    sesileVisibility,
    materialCodeBudgetaire,
    monthBeforeArchiving,
    hoursBeforeResendActe,
    hoursBeforeResendPes,
    acceptFileDocumentConvocation,
    publicPages
}
