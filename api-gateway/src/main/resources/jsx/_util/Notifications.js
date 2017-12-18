const notifications = {
    defaultError: {
        title: 'notifications.error.title',
        message: 'notifications.error.message',
        level: 'error',
        position: 'tc'
    },
    acte: {
        noContent: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.no_content',
            level: 'warning',
            position: 'tc'
        },
        sent: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.sent.success',
            level: 'success',
            position: 'tc'
        },
        cancelled: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.cancelled.success',
            level: 'success',
            position: 'tc'
        },
        cancelledForbidden: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.cancelled.forbidden',
            level: 'error',
            position: 'tc'
        },
        draftDeleted: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.draft.single_deleted.success',
            level: 'success',
            position: 'tc'
        },
        draftsDeleted: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.draft.multiple_deleted.success',
            level: 'success',
            position: 'tc'
        }
    },
    pes: {
        sent: {
            title: 'notifications.pes.title',
            message: 'notifications.pes.sent.success',
            level: 'success',
            position: 'tc'
        },
        virus: {
            title: 'notifications.pes.title',
            message: 'notifications.pes.sent.virus',
            level: 'error',
            position: 'tc'
        },
        missingData: {
            title: 'notifications.pes.title',
            message: 'notifications.pes.sent.missing_data',
            level: 'warning',
            position: 'tc'
        }
    },
    admin: {
        localAuthorityUpdate: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.local_authority.update.success',
            level: 'success',
            position: 'tc'
        },
        moduleUpdated: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.admin_module.update.success',
            level: 'success',
            position: 'tc'
        },
        groupsUpdated: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.groups_updated',
            level: 'success',
            position: 'tc'
        }
    }
}

module.exports = { notifications }