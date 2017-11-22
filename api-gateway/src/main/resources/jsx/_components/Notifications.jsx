const errorNotification = (title, message) => {
    return {
        title: title,
        message: message,
        level: 'error',
        position: 'tc'
    }
}

const localAuthorityUpdateSuccess = (t) => {
    return {
        title: t('notifications.acte.admin.title'),
        message: t('notifications.acte.admin.local_authority.update.success'),
        level: 'success',
        position: 'tc'
    }
}

const adminModuleUpdateSuccess = (t) => {
    return {
        title: t('notifications.acte.admin.title'),
        message: t('notifications.acte.admin.admin_module.update.success'),
        level: 'success',
        position: 'tc'
    }
}

const acteNoContent = (t) => {
    return {
        title: t('notifications.acte.title'),
        message: t('notifications.acte.no_content'),
        level: 'warning',
        position: 'tc'
    }
}

const acteSentSuccess = (t) => {
    return {
        title: t('notifications.acte.title'),
        message: t('notifications.acte.sent.success'),
        level: 'success',
        position: 'tc'
    }
}

const draftDeletedSuccess = (t) => {
    return {
        title: t('notifications.acte.title'),
        message: t('notifications.acte.draft.single_deleted.success'),
        level: 'success',
        position: 'tc'
    }
}

const draftsDeletedSuccess = (t) => {
    return {
        title: t('notifications.acte.title'),
        message: t('notifications.acte.draft.multiple_deleted.success'),
        level: 'success',
        position: 'tc'
    }
}

const acteCancelledSuccess = (t) => {
    return {
        title: t('notifications.acte.title'),
        message: t('notifications.acte.cancelled.success'),
        level: 'success',
        position: 'tc'
    }
}

const acteCancelledForbidden = (t) => {
    return {
        title: t('notifications.acte.title'),
        message: t('notifications.acte.cancelled.forbidden'),
        level: 'error',
        position: 'tc'
    }
}

const pesSentSuccess = (t) => {
    return {
        title: t('notifications.pes.title'),
        message: t('notifications.pes.sent.success'),
        level: 'success',
        position: 'tc'
    }
}

const pesSentVirus = (t) => {
    return {
        title: t('notifications.pes.title'), message: t('notifications.pes.sent.virus'), level: 'error', position: 'tc'
    }
}

const pesSentMissingData = (t) => {
    return {
        title: t('notifications.pes.title'),
        message: t('notifications.pes.sent.missing_data'),
        level: 'warning',
        position: 'tc'
    }
}

module.exports = {
    errorNotification,
    acteSentSuccess,
    acteCancelledSuccess,
    acteCancelledForbidden,
    acteNoContent,
    pesSentSuccess,
    pesSentVirus,
    pesSentMissingData,
    localAuthorityUpdateSuccess,
    draftDeletedSuccess,
    draftsDeletedSuccess,
    adminModuleUpdateSuccess

}