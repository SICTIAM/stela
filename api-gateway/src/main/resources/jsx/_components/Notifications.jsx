const errorNotification = (title, message) => {
    return {
        title: title,
        message: message,
        level: 'error',
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

module.exports = { errorNotification, acteSentSuccess, pesSentSuccess, pesSentVirus, pesSentMissingData }