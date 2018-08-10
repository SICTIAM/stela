import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'
import { translate } from 'react-i18next'
import { Segment } from 'semantic-ui-react'

import StelaTable from '../_components/StelaTable'
import { Page } from '../_components/UI'
import { checkStatus, getLocalAuthoritySlug } from '../_util/utils'
import { notifications } from '../_util/Notifications'

class DraftList extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        actes: []
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/acte/drafts' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ actes: json }))
    }
    deleteDrafts = (selectedUuids) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const headers = { 'Content-Type': 'application/json' }
        _fetchWithAuthzHandling({ url: '/api/acte/drafts', body: JSON.stringify(selectedUuids), headers: headers, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                _addNotification(notifications.acte.draftsDeleted)
                const actes = selectedUuids.length > 0 ? this.state.actes.filter(acte => !selectedUuids.includes(acte.uuid)) : []
                this.setState({ actes })
            })
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    render() {
        const { t } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        const numberDisplay = (actes) => actes[0].number
        const objetDisplay = (actes) => actes[0].objet
        const natureDisplay = (actes) => actes[0].nature ? t(`acte.nature.${actes[0].nature}`) : ''
        const draftDateDisplay = (date) => moment(date).format('DD/MM/YYYY - HH:mm')
        const deleteSelection = { title: t('acte.drafts.delete_selected_drafts'), titleNoSelection: t('acte.drafts.delete_all_drafts'), action: this.deleteDrafts }

        return (
            <Page title={t('acte.drafts.title')}>
                <Segment>
                    <StelaTable
                        data={this.state.actes}
                        metaData={[
                            { property: 'uuid', displayed: false, searchable: false },
                            { property: 'actes', displayed: true, displayName: t('acte.fields.number'), searchable: true, displayComponent: numberDisplay },
                            { property: 'actes', displayed: true, displayName: t('acte.fields.objet'), searchable: true, displayComponent: objetDisplay },
                            { property: 'actes', displayed: true, displayName: t('acte.fields.nature'), searchable: true, displayComponent: natureDisplay },
                            { property: 'lastModified', displayed: true, displayName: t('api-gateway:list.last_modified'), searchable: false, displayComponent: draftDateDisplay },
                            { property: 'batch', displayed: false, searchable: false }
                        ]}
                        header={true}
                        select={true}
                        selectOptions={[deleteSelection]}
                        link={`/${localAuthoritySlug}/actes/brouillons/`}
                        linkProperty='uuid'
                        noDataMessage={t('acte.drafts.no_draft')}
                        keyProperty='uuid' />
                </Segment >
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(DraftList)