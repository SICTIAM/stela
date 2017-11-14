import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'
import { translate } from 'react-i18next'
import { Segment } from 'semantic-ui-react'

import StelaTable from '../_components/StelaTable'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import { errorNotification, draftsDeletedSuccess } from '../_components/Notifications'

class DraftList extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        actes: []
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/acte/drafts' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ actes: json }))
    }
    deleteDrafts = (selectedUuids) => {
        const headers = {
            'Content-Type': 'application/json'
        }
        fetchWithAuthzHandling({ url: '/api/acte/drafts', body: JSON.stringify(selectedUuids), headers: headers, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                this.context._addNotification(draftsDeletedSuccess(this.context.t))
                const actes = selectedUuids.length > 0 ? this.state.actes.filter(acte => !selectedUuids.includes(acte.uuid)) : []
                this.setState({ actes })
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const natureDisplay = (nature) => nature ? t(`acte.nature.${nature}`) : ''
        const dateDisplay = (date) => date ? moment(date).format('DD/MM/YYYY - HH:mm') : ''
        const deleteSelection = { title: t('acte.drafts.delete_selected_drafts'), titleNoSelection: t('acte.drafts.delete_all_drafts'), action: this.deleteDrafts }
        return (
            <Segment>
                <h1>{t('acte.drafts.title')}</h1>
                <StelaTable
                    data={this.state.actes}
                    metaData={[
                        { property: 'uuid', displayed: false, searchable: false },
                        { property: 'number', displayed: true, displayName: t('acte.fields.number'), searchable: true },
                        { property: 'objet', displayed: true, displayName: t('acte.fields.objet'), searchable: true },
                        { property: 'decision', displayed: false, searchable: false },
                        { property: 'nature', displayed: true, displayName: t('acte.fields.nature'), searchable: true, displayComponent: natureDisplay },
                        { property: 'code', displayed: false, searchable: false },
                        { property: 'creation', displayed: true, displayName: t('api-gateway:list.last_modified'), searchable: true, displayComponent: dateDisplay },
                        { property: 'acteHistories', displayed: false, displayName: t('acte.fields.status'), searchable: true },
                        { property: 'public', displayed: false, searchable: false },
                        { property: 'publicWebsite', displayed: false, searchable: false },
                    ]}
                    header={true}
                    select={true}
                    selectOptions={[deleteSelection]}
                    link='/actes/brouillons/'
                    linkProperty='uuid'
                    noDataMessage={t('acte.drafts.no_draft')}
                    keyProperty='uuid' />
            </Segment >
        )
    }
}

export default translate(['acte', 'api-gateway'])(DraftList)