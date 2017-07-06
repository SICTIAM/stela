import React from 'react'
import PropTypes from 'prop-types'

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
        title: t('notifications.pes.title'),
        message: t('notifications.pes.sent.virus'),
        level: 'error',
        position: 'tc'
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

module.exports = { pesSentSuccess, pesSentVirus, pesSentMissingData }