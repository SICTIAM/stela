import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import { modules } from '../../_util/constants'
import { Page, Pagination } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class LocalAuthorityList extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        localAuthorities: [],
        totalCount: 0,
        limit: 25,
        offset: 0,
        column: '',
        direction: ''
    }
    componentDidMount() {
        this.fetchLocalAuthorityies()
    }
    handlePageClick = (data) => {
        const offset = Math.ceil(data.selected * this.state.limit)
        this.setState({ offset }, this.fetchLocalAuthorityies)
    }
    fetchLocalAuthorityies = () => {
        const { limit, offset, column, direction } = this.state
        let params = { limit, offset, column, direction }
        fetchWithAuthzHandling({ url: '/api/admin/local-authority', query: params })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ localAuthorities: json.results, totalCount: json.totalCount }))
    }
    sort = (clickedColumn) => {
        const { column, direction } = this.state
        if (column !== clickedColumn) {
            this.setState({ column: clickedColumn, direction: 'ASC' }, this.fetchLocalAuthorityies)
            return
        }
        this.setState({ direction: direction === 'ASC' ? 'DESC' : 'ASC' }, this.fetchLocalAuthorityies)
    }
    renderActivatedModule = (activatedModules, moduleName) =>
        activatedModules.includes(moduleName) ? <Icon name='checkmark' color='green' /> : <Icon name='remove' color='red' />
    render() {
        const { t } = this.context
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
                handlePageClick={this.handlePageClick} />
        return (
            <Page title={t('admin.modules.local_authority_settings')}>
                <Segment>
                    <StelaTable
                        data={this.state.localAuthorities}
                        metaData={metaData}
                        header={true}
                        search={false}
                        link='/admin/collectivite/'
                        linkProperty='uuid'
                        noDataMessage='Aucune collectivité'
                        keyProperty='uuid'
                        pagination={pagination}
                        sort={this.sort}
                        direction={this.state.direction}
                        column={this.state.column} />
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthorityList)