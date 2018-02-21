const notifications = {
    defaultError: {
        title: 'notifications.error.title',
        message: 'notifications.error.message',
        level: 'error',
        position: 'br'
    },
    acte: {
        noContent: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.no_content',
            level: 'warning',
            position: 'br'
        },
        sent: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.sent.success',
            level: 'success',
            position: 'br'
        },
        cancelled: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.cancelled.success',
            level: 'success',
            position: 'br'
        },
        cancelledForbidden: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.cancelled.forbidden',
            level: 'error',
            position: 'br'
        },
        draftDeleted: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.draft.single_deleted.success',
            level: 'success',
            position: 'br'
        },
        draftsDeleted: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.draft.multiple_deleted.success',
            level: 'success',
            position: 'br'
        },
        courrierSimpleAsked: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.courrier_simple_asked',
            level: 'success',
            position: 'br'
        },
        lettreObservationAsked: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.lettre_observation_asked',
            level: 'success',
            position: 'br'
        },
        piecesComplementairesAsked: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.pieces_complementaires_asked',
            level: 'success',
            position: 'br'
        }
    },
    pes: {
        sent: {
            title: 'notifications.pes.title',
            message: 'notifications.pes.sent.success',
            level: 'success',
            position: 'br'
        },
        virus: {
            title: 'notifications.pes.title',
            message: 'notifications.pes.sent.virus',
            level: 'error',
            position: 'br'
        },
        missingData: {
            title: 'notifications.pes.title',
            message: 'notifications.pes.sent.missing_data',
            level: 'warning',
            position: 'br'
        }
    },
    admin: {
        localAuthorityUpdate: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.local_authority.update.success',
            level: 'success',
            position: 'br'
        },
        moduleUpdated: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.admin_module.update.success',
            level: 'success',
            position: 'br'
        },
        groupsUpdated: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.groups_updated',
            level: 'success',
            position: 'br'
        }
    },
    profile: {
        updated: {
            title: 'notifications.profile.title',
            message: 'notifications.profile.updated',
            level: 'success',
            position: 'br'
        }
    }
}

module.exports = { notifications }