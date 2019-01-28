import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon, Button } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import Pagination from '../../_components/Pagination'
import { modules } from '../../_util/constants'
import { Page } from '../../_components/UI'
import { notifications } from '../../_util/Notifications'
import {
    checkStatus,
    getLocalAuthoritySlug,
    handlePageClick,
    updateItemPerPage,
    sortTable
} from '../../_util/utils'
import { withAuthContext } from '../../Auth'

class LocalAuthorityList extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        localAuthorities: [],
        totalCount: 0,
        limit: 25,
        currentPage: 0,
        offset: 0,
        column: '',
        direction: ''
    }
    componentDidMount() {
        const itemPerPage = localStorage.getItem('itemPerPage')
        if (!itemPerPage) localStorage.setItem('itemPerPage', this.state.limit)
        else this.setState({ limit: parseInt(itemPerPage, 10) }, this.fetchLocalAuthorities)
    }
    fetchLocalAuthorities = () => {
        const { _fetchWithAuthzHandling } = this.context
        const { limit, offset, column, direction } = this.state
        _fetchWithAuthzHandling({ url: '/api/admin/local-authority', query: { limit, offset, column, direction } })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ localAuthorities: json.results, totalCount: json.totalCount }))
    }
    askAllClassificationUpdate = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const url = '/api/acte/ask-classification/all'
        _fetchWithAuthzHandling({ url, method: 'POST', context: this.props.authContext })
            .then(checkStatus)
            .then(() => _addNotification(notifications.admin.classificationAsked))
            .catch((response) => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    renderActivatedModule = (activatedModules, moduleName) =>
        activatedModules.includes(moduleName) ? <Icon name='checkmark' color='green' /> : <Icon name='remove' color='red' />
    render() {
        const { t } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'siren', displayed: true, displayName: t('local_authority.siren'), searchable: true, sortable: true },
            { property: 'name', displayed: true, displayName: t('local_authority.name'), searchable: true, sortable: true }
        ]
        // TODO: fetch module list from backend
        modules.forEach(moduleName =>
            metaData.push({
                property: 'activatedModules',
                displayed: true,
                displayName: t(`modules.${moduleName}`),
                searchable: false,
                sortable: false,
                displayComponent: (activatedModules) => this.renderActivatedModule(activatedModules, moduleName)
            })
        )
        const displayedColumns = metaData.filter(metaData => metaData.displayed)
        const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
        const pagination =
            <Pagination
                columns={displayedColumns.length}
                pageCount={pageCount}
                handlePageClick={(data) => handlePageClick(this, data, this.fetchLocalAuthorities)}
                itemPerPage={this.state.limit}
                updateItemPerPage={(itemPerPage) => updateItemPerPage(this, itemPerPage, this.fetchLocalAuthorities)}
                currentPage={this.state.currentPage} />
        return (
            <Page title={t('admin.modules.local_authority_settings')}>
                <Segment>
                    <div style={{ textAlign: 'right', marginBottom: '1em' }}>
                        <Button basic primary onClick={this.askAllClassificationUpdate}>
                            {t('acte:admin.modules.acte.local_authority_settings.askClassifications')}
                        </Button>
                    </div>
                    <StelaTable
                        data={this.state.localAuthorities}
                        metaData={metaData}
                        header={true}
                        search={false}
                        link={`/${localAuthoritySlug}/admin/collectivite/`}
                        linkProperty='uuid'
                        noDataMessage='Aucune collectivité'
                        keyProperty='uuid'
                        pagination={pagination}
                        sort={(clickedColumn) => sortTable(this, clickedColumn, this.fetchLocalAuthorities)}
                        direction={this.state.direction}
                        column={this.state.column} />
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway', 'acte'])(withAuthContext(LocalAuthorityList))
