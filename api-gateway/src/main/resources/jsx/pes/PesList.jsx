import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button } from 'semantic-ui-react'
import moment from 'moment'

import StelaTable from '../_components/StelaTable'
import Pagination from '../_components/Pagination'
import AdvancedSearch from '../_components/AdvancedSearch'
import InputDatetime from '../_components/InputDatetime'
import { Page, FormFieldInline, FormField, LoadingContent } from '../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import { notifications } from '../_util/Notifications'

class PesList extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
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
        fetchStatus: ''
    }
    componentDidMount() {
        const itemPerPage = localStorage.getItem('itemPerPage')
        if (!itemPerPage) localStorage.setItem('itemPerPage', 25)
        else this.setState({ limit: 25 }, this.submitForm)
        fetchWithAuthzHandling({ url: '/api/pes/statuses' })
            .then(response => response.json())
            .then(json => this.setState({ pesStatuses: json }))
    }
    getSearchData = () => {
        const { limit, offset, direction, column } = this.state
        const data = { limit, offset, direction, column }
        Object.keys(this.state.search)
            .filter(k => this.state.search[k] !== '')
            .forEach(k => data[k] = this.state.search[k])
        if (data.creationFrom) data.creationFrom = moment(data.creationFrom).format('YYYY-MM-DD')
        if (data.creationTo) data.creationTo = moment(data.creationTo).format('YYYY-MM-DD')
        return data
    }
    handleFieldChange = (field, value) => {
        const search = this.state.search
        search[field] = value
        this.setState({ search: search })
    }
    submitForm = () => {
        this.setState({ fetchStatus: 'loading' })
        const headers = { 'Accept': 'application/json' }
        const data = this.getSearchData()
        fetchWithAuthzHandling({ url: '/api/pes', method: 'GET', query: data, headers: headers })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ pess: json.results, totalCount: json.totalCount, fetchStatus: 'fetched' }))
            .catch(response => {
                this.setState({ fetchStatus: 'api-gateway:error.default' })
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    handlePageClick = (data) => {
        const offset = Math.ceil(data.selected * this.state.limit)
        this.setState({ offset }, () => this.submitForm())
    }
    sort = (clickedColumn) => {
        const { column, direction } = this.state
        if (column !== clickedColumn) {
            this.setState({ column: clickedColumn, direction: 'ASC' }, () => this.submitForm())
            return
        }
        this.setState({ direction: direction === 'ASC' ? 'DESC' : 'ASC' }, () => this.submitForm())
    }
    updateItemPerPage = (limit) => {
        this.setState({ limit }, this.submitForm)
    }
    render() {
        const { t, _addNotification } = this.context
        const { search } = this.state
        const statusOptions = this.state.pesStatuses.map(statusItem =>
            <option key={statusItem} value={statusItem}>{t(`pes.status.${statusItem}`)}</option>
        )
        const statusDisplay = (histories) => {
            if (histories.length === 0) return ''
            const lastHistory = histories[histories.length - 1]
            return <span>{moment(lastHistory.date).format('DD/MM/YYYY')} : {t(`pes.status.${lastHistory.status}`)}</span>
        }
        const creationDisplay = (creation) => moment(creation).format('DD/MM/YYYY')
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'creation', displayed: true, displayName: t('pes.fields.creation'), searchable: true, displayComponent: creationDisplay },
            { property: 'objet', displayed: true, displayName: t('pes.fields.objet'), searchable: true },
            { property: 'comment', displayed: true, displayName: t('pes.fields.comment'), searchable: true },
            { property: 'pesHistories', displayed: true, displayName: t('pes.fields.status'), searchable: false, displayComponent: statusDisplay }
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
            <Page title={t('pes.list.title')}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Segment>
                        <AdvancedSearch
                            isDefaultOpen={false}
                            fieldId='multifield'
                            fieldValue={search.multifield}
                            fieldOnChange={this.handleFieldChange}
                            onSubmit={this.submitForm}>

                            <Form onSubmit={this.submitForm}>
                                <FormFieldInline htmlFor='objet' label={t('pes.fields.objet')} >
                                    <input id='objet' value={search.objet} onChange={e => this.handleFieldChange('objet', e.target.value)} />
                                </FormFieldInline>
                                <FormFieldInline htmlFor='creationFrom' label={t('pes.fields.creation')}>
                                    <Form.Group style={{ marginBottom: 0 }} widths='equal'>
                                        <FormField htmlFor='creationFrom' label={t('api-gateway:form.from')}>
                                            <InputDatetime id='creationFrom'
                                                timeFormat={false}
                                                value={search.decisionFrom}
                                                onChange={date => this.handleFieldChange('creationFrom', date)} />
                                        </FormField>
                                        <FormField htmlFor='creationTo' label={t('api-gateway:form.to')}>
                                            <InputDatetime id='creationTo'
                                                timeFormat={false}
                                                value={search.creationTo}
                                                onChange={date => this.handleFieldChange('creationTo', date)} />
                                        </FormField>
                                    </Form.Group>
                                </FormFieldInline>
                                <FormFieldInline htmlFor='status' label={t('pes.fields.status')}>
                                    <select id='status' value={search.status} onChange={e => this.handleFieldChange('status', e.target.value)}>
                                        <option value=''>{t('api-gateway:form.all')}</option>
                                        {statusOptions}
                                    </select>
                                </FormFieldInline>
                                <div style={{ textAlign: 'right' }}>
                                    <Button type='submit' basic primary>{t('api-gateway:form.search')}</Button>
                                </div>
                            </Form>
                        </AdvancedSearch>

                        <StelaTable
                            data={this.state.pess}
                            metaData={metaData}
                            header={true}
                            search={false}
                            link='/pes/'
                            linkProperty='uuid'
                            noDataMessage={t('pes.list.empty')}
                            keyProperty='uuid'
                            pagination={pagination}
                            sort={this.sort}
                            direction={this.state.direction}
                            column={this.state.column} />
                    </Segment>
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesList)