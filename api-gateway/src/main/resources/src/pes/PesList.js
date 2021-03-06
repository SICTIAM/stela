import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button } from 'semantic-ui-react'
import moment from 'moment'

import StelaTable from '../_components/StelaTable'
import Pagination from '../_components/Pagination'
import AdvancedSearch from '../_components/AdvancedSearch'
import InputDatetime from '../_components/InputDatetime'
import { Page, FormFieldInline, FormField, LoadingContent, StatusDisplay } from '../_components/UI'
import {
    checkStatus,
    getLocalAuthoritySlug,
    handleSearchChange,
    handlePageClick,
    updateItemPerPage,
    sortTable,
    onSearch
} from '../_util/utils'
import { anomalies } from '../_util/constants'
import { notifications } from '../_util/Notifications'

class PesList extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        pess: [],
        pesStatuses: [],
        search: {
            multifield: '',
            objet: '',
            creationFrom: '',
            creationTo: '',
            status: ''
        },
        totalCount: 0,
        column: '',
        direction: '',
        limit: 25,
        offset: 0,
        currentPage: 0,
        fetchStatus: ''
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        const itemPerPage = localStorage.getItem('itemPerPage')
        if (!itemPerPage) localStorage.setItem('itemPerPage', this.state.limit)
        else this.setState({ limit: parseInt(itemPerPage, 10) }, this.submitForm)
        _fetchWithAuthzHandling({ url: '/api/pes/statuses' })
            .then(response => response.json())
            .then(json => this.setState({ pesStatuses: json }))
    }
    getSearchData = () => {
        const { limit, offset, direction, column } = this.state
        const data = { limit, offset, direction, column }
        Object.keys(this.state.search)
            .filter(k => this.state.search[k] !== '')
            .forEach(k => (data[k] = this.state.search[k]))
        if (data.creationFrom)
            data.creationFrom = moment(data.creationFrom).format('YYYY-MM-DD')
        if (data.creationTo)
            data.creationTo = moment(data.creationTo).format('YYYY-MM-DD')
        return data
    }
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        this.setState({ fetchStatus: 'loading' })
        const headers = { Accept: 'application/json' }
        const data = this.getSearchData()
        _fetchWithAuthzHandling({ url: '/api/pes', method: 'GET', query: data, headers: headers })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ pess: json.results, totalCount: json.totalCount, fetchStatus: 'fetched' }))
            .catch(response => {
                this.setState({ fetchStatus: 'api-gateway:error.default' })
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    negativeResolver = pes => anomalies.includes(pes.lastHistoryStatus)
    render() {
        const { t } = this.context
        const { search } = this.state
        const localAuthoritySlug = getLocalAuthoritySlug()
        const statusOptions = this.state.pesStatuses.map(statusItem => (
            <option key={statusItem} value={statusItem}>
                {t(`pes.status.${statusItem}`)}
            </option>
        ))
        const statusDisplay = (pes) => <StatusDisplay status={t(`pes.status.${pes.lastHistoryStatus}`)} date={pes.lastHistoryDate}/>
        const creationDisplay = (creation) => moment(creation).format('DD/MM/YYYY')
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'creation', displayed: true, sortable: true, displayName: t('pes.fields.creation'), searchable: true, displayComponent: creationDisplay,
                collapsing: true },
            { property: 'objet', displayed: true, sortable: true, displayName: t('pes.fields.objet'), searchable: true },
            { property: 'fileType', displayed: true, displayName: t('pes.fields.fileType'), searchable: false, collapsing: true },
            { property: '_self', displayed: true, displayName: t('pes.fields.status'), searchable: false, displayComponent: statusDisplay,
                collapsing: true }
        ]
        const displayedColumns = metaData.filter(metaData => metaData.displayed)
        const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
        const pagination = (
            <Pagination
                columns={displayedColumns.length}
                pageCount={pageCount}
                handlePageClick={(data) => handlePageClick(this, data, this.submitForm)}
                itemPerPage={this.state.limit}
                updateItemPerPage={(itemPerPage) => updateItemPerPage(this, itemPerPage, this.submitForm)}
                currentPage={this.state.currentPage}
            />
        )
        return (
            <Page title={t('pes.list.title')}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Segment>
                        <AdvancedSearch isDefaultOpen={false} fieldId="multifield" fieldValue={search.multifield}
                            fieldOnChange={(id, value ) => handleSearchChange(this, id, value)} onSubmit={() => onSearch(this, this.submitForm)}>
                            <Form onSubmit={() => onSearch(this, this.submitForm)}>
                                <FormFieldInline htmlFor="objet" label={t('pes.fields.objet')}>
                                    <input id="objet" value={search.objet} onChange={e => handleSearchChange(this, 'objet', e.target.value)} />
                                </FormFieldInline>
                                <FormFieldInline htmlFor="creationFrom" label={t('pes.fields.creation')}>
                                    <Form.Group style={{ marginBottom: 0 }} widths="equal">
                                        <FormField htmlFor="creationFrom" label={t('api-gateway:form.from')}>
                                            <InputDatetime id="creationFrom" timeFormat={false} value={search.decisionFrom}
                                                onChange={date => handleSearchChange(this, 'creationFrom', date) } />
                                        </FormField>
                                        <FormField htmlFor="creationTo" label={t('api-gateway:form.to')}>
                                            <InputDatetime
                                                id="creationTo"
                                                timeFormat={false}
                                                value={search.creationTo}
                                                onChange={date => handleSearchChange(this, 'creationTo', date)} />
                                        </FormField>
                                    </Form.Group>
                                </FormFieldInline>
                                <FormFieldInline htmlFor="status" label={t('pes.fields.status')} >
                                    <select id="status" value={search.status} onBlur={e => handleSearchChange(this, 'status', e.target.value)} onChange={e => handleSearchChange(this, 'status', e.target.value)}>
                                        <option value="">{t('api-gateway:form.all')}</option>
                                        {statusOptions}
                                    </select>
                                </FormFieldInline>
                                <div style={{ textAlign: 'right' }}>
                                    <Button type="submit" basic primary>
                                        {t('api-gateway:form.search')}
                                    </Button>
                                </div>
                            </Form>
                        </AdvancedSearch>

                        <StelaTable
                            data={this.state.pess}
                            metaData={metaData}
                            header={true}
                            search={false}
                            link={`/${localAuthoritySlug}/pes/liste/`}
                            linkProperty="uuid"
                            noDataMessage={t('pes.list.empty')}
                            keyProperty="uuid"
                            pagination={pagination}
                            sort={(clickedColumn) => sortTable(this, clickedColumn, this.submitForm)}
                            direction={this.state.direction}
                            column={this.state.column}
                            negativeResolver={this.negativeResolver} />
                    </Segment>
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesList)
