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
        republished: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.republished.success',
            level: 'success',
            position: 'br'
        },
        republishedError: {
            title: 'notifications.acte.title',
            message: 'notifications.acte.sent.error',
            level: 'error',
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
    convocation: {
        created: {
            title: 'notifications.convocation.title',
            message: 'notifications.convocation.created.success',
            level: 'success',
            position: 'br'
        },
        sent: {
            title: 'notifications.convocation.title',
            message: 'notifications.convocation.sent.success',
            level: 'success',
            position: 'br'
        },
        reponseSent: {
            title: 'notifications.convocation.title',
            message: 'notifications.convocation.reponseSent.success',
            level: 'success',
            position: 'br'
        },
        cancel: {
            title: 'notifications.convocation.title',
            message: 'notifications.convocation.cancel.success',
            level: 'success',
            position: 'br'
        },
        complet: {
            title: 'notifications.convocation.title',
            message: 'notifications.convocation.complet.success',
            level: 'success',
            position: 'br'
        },
        complet_with_document: {
            title: 'notifications.convocation.title',
            message: 'notifications.convocation.complet_with_document.success',
            level: 'success',
            position: 'br'
        },
    },
    admin: {
        instanceParamsUpdated: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.instance_params_updated',
            level: 'success',
            position: 'br'
        },
        generic_account_created: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.generic_account.created',
            level: 'success',
            position: 'br'
        },
        generic_account_deleted: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.generic_account.deleted',
            level: 'success',
            position: 'br'
        },
        generic_account_updated: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.generic_account.updated',
            level: 'success',
            position: 'br'
        },
        localAuthorityUpdate: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.local_authority.update.success',
            level: 'success',
            position: 'br'
        },
        localAuthorityPesUpdate: {
            title: 'notifications.pes.admin.title',
            message: 'notifications.pes.admin.local_authority.update.success',
            level: 'success',
            position: 'br'
        },
        classificationAsked: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.local_authority.classificationAsked',
            level: 'success',
            position: 'br'
        },
        moduleUpdated: {
            title: 'notifications.acte.admin.title',
            message: 'notifications.acte.admin.admin_module.update.success',
            level: 'success',
            position: 'br'
        },
        moduleConvocationUpdated: {
            title: 'notifications.convocation.admin.title',
            message: 'notifications.acte.admin.admin_module.update.success',
            level: 'success',
            position: 'br'
        },
        groupCreated: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.group_created',
            level: 'success',
            position: 'br'
        },
        groupUpdated: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.group_updated',
            level: 'success',
            position: 'br'
        },
        groupDeleted: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.group_deleted',
            level: 'success',
            position: 'br'
        },
        agentProfileUpdated: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.agent_profile_updated',
            level: 'success',
            position: 'br'
        },
        certificateCreated: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.certificateCreated',
            level: 'success',
            position: 'br'
        },
        certificateDeleted: {
            title: 'notifications.admin.title',
            message: 'notifications.admin.certificateDeleted',
            level: 'success',
            position: 'br'
        },
        sesileValidTokens: {
            title: 'notifications.pes.admin.sesile.validTokens',
            level: 'success',
            position: 'br'
        },
        sesileInvalidTokens: {
            title: 'notifications.pes.admin.sesile.invalidTokens',
            level: 'error',
            position: 'br'
        },
        sesileUnavailableService: {
            title: 'notifications.pes.admin.sesile.unavailableService',
            level: 'error',
            position: 'br'
        },
        recipientCreated: {
            title: 'notifications.convocation.admin.recipients.recipient_created',
            level: 'success',
            position: 'br'
        },
        recipientUpdated: {
            title: 'notifications.convocation.admin.recipients.recipient_updated',
            level: 'success',
            position: 'br'
        },
        assemblyTypeCreated: {
            title: 'notifications.convocation.admin.recipients.assembly_type_created',
            level: 'success',
            position: 'br'
        },
        assemblyTypeUpdated: {
            title: 'notifications.convocation.admin.recipients.assembly_type_updated',
            level: 'success',
            position: 'br'
        },
        statusUpdated: {
            title: 'notifications.convocation.admin.recipients.status_updated',
            level: 'success',
            position: 'br'
        },
        email_validation_in_progress: {
            title: 'notifications.convocation.admin.title',
            message: 'notifications.convocation.admin.email_validation.in_progress',
            level: 'info',
            position: 'br'
        },
        email_validation_success: {
            title: 'notifications.convocation.admin.title',
            message: 'notifications.convocation.admin.email_validation.success',
            level: 'success',
            position: 'br'
        },
        all_recipients_deactivated_success: {
            title: 'notifications.convocation.admin.title',
            message: 'notifications.convocation.admin.all_recipients_deactivated',
            level: 'success',
            position: 'br'
        },
        notificationMailUpdated: {
            title: 'notifications.convocation.admin.title',
            message: 'notifications.convocation.admin.notifications_email.success',
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
        },
        certificatePairedSuccess: {
            title: 'notifications.profile.title',
            message: 'notifications.profile.certificatePairedSuccess',
            level: 'success',
            position: 'br'
        },
        certificateNotValid: {
            title: 'notifications.profile.title',
            message: 'notifications.profile.certificateNotValid',
            level: 'error',
            position: 'br'
        },
        certificateConflict: {
            title: 'notifications.profile.title',
            message: 'notifications.profile.certificateConflict',
            level: 'error',
            position: 'br'
        }
    }
}

export { notifications }