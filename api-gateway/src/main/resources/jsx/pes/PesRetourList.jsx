import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment } from 'semantic-ui-react'
import moment from 'moment'

import StelaTable from '../_components/StelaTable'
import Pagination from '../_components/Pagination'
import { Page } from '../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import { notifications } from '../_util/Notifications'

class PesRetourList extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        pesRetours: [],
        totalCount: 0,
        limit: 25,
        offset: 0
    }
    componentDidMount() {
        this.submitForm()
    }
    getSearchData = () => {
        const { limit, offset } = this.state
        return { limit, offset }
    }
    submitForm = () => {
        const headers = { 'Accept': 'application/json' }
        const data = this.getSearchData()
        fetchWithAuthzHandling({ url: '/api/pes/retour', method: 'GET', query: data, headers: headers })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ pesRetours: json.results, totalCount: json.totalCount }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    handlePageClick = (data) => {
        const offset = Math.ceil(data.selected * this.state.limit)
        this.setState({ offset }, () => this.submitForm())
    }
    updateItemPerPage = (limit) => {
        this.setState({ limit }, this.submitForm)
    }
    render() {
        const { t, _addNotification } = this.context
        const creationDisplay = (creation) => moment(creation).format('DD/MM/YYYY')
        const attachmentLink = (pesRetour) => <a id='attachment' target='_blank' href={`/api/pes/retour/${pesRetour.uuid}/file`}>{pesRetour.attachment.filename}</a>
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'creation', displayed: true, displayName: t('pes.fields.creation'), searchable: true, displayComponent: creationDisplay },
            { property: '_self', displayed: true, displayName: t('pes.fields.attachment'), searchable: false, displayComponent: attachmentLink }
        ]
        const displayedColumns = metaData.filter(metaData => metaData.displayed)
        const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
        const pagination =
            <Pagination
                columns={displayedColumns.length}
                pageCount={pageCount}
                handlePageClick={this.handlePageClick}
                itemPerPage={this.state.limit}
                updateItemPerPage={this.updateItemPerPage} />
        return (
            <Page title={t('pes.retour.list.title')}>
                <Segment>
                    <StelaTable
                        data={this.state.pesRetours}
                        metaData={metaData}
                        header={true}
                        search={false}
                        noDataMessage={t('pes.retour.list.empty')}
                        keyProperty='uuid'
                        pagination={pagination} />
                </Segment>
            </Page>
        )
    }
}

export default translate(['pes'])(PesRetourList)