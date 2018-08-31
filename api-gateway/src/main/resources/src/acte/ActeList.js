import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'
import { translate } from 'react-i18next'
import { Form, Button, Segment } from 'semantic-ui-react'
import FileSaver from 'file-saver'

import StelaTable from '../_components/StelaTable'
import Pagination from '../_components/Pagination'
import AdvancedSearch from '../_components/AdvancedSearch'
import InputDatetime from '../_components/InputDatetime'
import { checkStatus, getHistoryStatusTranslationKey, getLocalAuthoritySlug } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import { FormFieldInline, FormField, Page, LoadingContent, StatusDisplay } from '../_components/UI'
import { natures, status } from '../_util/constants'

class ActeList extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        actes: [],
        totalCount: 0,
        column: '',
        direction: '',
        search: {
            multifield: '',
            number: '',
            objet: '',
            nature: '',
            status: '',
            decisionFrom: '',
            decisionTo: ''
        },
        limit: 25,
        offset: 0,
        fetchStatus: ''
    }
    componentDidMount() {
        const itemPerPage = localStorage.getItem('itemPerPage')
        if (!itemPerPage) localStorage.setItem('itemPerPage', 25)
        else this.setState({ limit: 25 }, this.submitForm)
    }
    handleFieldChange = (field, value) => {
        const search = this.state.search
        search[field] = value
        this.setState({ search: search })
    }
    getSearchData = () => {
        const { limit, offset, direction, column } = this.state
        const data = { limit, offset, direction, column }
        Object.keys(this.state.search)
            .filter(k => this.state.search[k] !== '')
            .map(k => data[k] = this.state.search[k])
        if (data.decisionFrom) data.decisionFrom = moment(data.decisionFrom).format('YYYY-MM-DD')
        if (data.decisionTo) data.decisionTo = moment(data.decisionTo).format('YYYY-MM-DD')
        return data
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
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        this.setState({ fetchStatus: 'loading' })
        const headers = { 'Accept': 'application/json' }
        const data = this.getSearchData()
        _fetchWithAuthzHandling({ url: '/api/acte', method: 'GET', query: data, headers: headers })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ actes: json.results, totalCount: json.totalCount, fetchStatus: 'fetched' }))
            .catch(response => {
                this.setState({ fetchStatus: 'api-gateway:error.default' })
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    downloadMergedStamp = (selectedUuids) => this.downloadFromSelectionOrSearch(selectedUuids, '/api/acte/actes.pdf', 'actes.pdf')
    downloadZipedStamp = (selectedUuids) => this.downloadFromSelectionOrSearch(selectedUuids, '/api/acte/actes.zip', 'actes.zip')
    downloadACKs = (selectedUuids) => this.downloadFromSelectionOrSearch(selectedUuids, '/api/acte/ARs.pdf', 'ARs.pdf')
    downloadCSV = (selectedUuids) => this.downloadFromSelectionOrSearch(selectedUuids, '/api/acte/actes.csv', 'actes.csv')
    downloadFromSelectionOrSearch = (selectedUuids, url, filename) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const ActeUuidsAndSearchUI = Object.assign({ uuids: selectedUuids }, this.getSearchData())
        const headers = { 'Content-Type': 'application/json' }
        _fetchWithAuthzHandling({ url: url, body: JSON.stringify(ActeUuidsAndSearchUI), headers: headers, method: 'POST', context: this.context })
            .then(checkStatus)
            .then(response => {
                if (response.status === 204) throw response
                else return response
            })
            .then(response => response.blob())
            .then(blob => FileSaver.saveAs(blob, filename))
            .catch(response => {
                if (response.status === 204) _addNotification(notifications.acte.noContent)
                else response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    render() {
        const { t } = this.context
        const { search } = this.state
        const localAuthoritySlug = getLocalAuthoritySlug()
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const statusOptions = status.map(statusItem =>
            <option key={statusItem} value={statusItem}>{t(`acte.status.${statusItem}`)}</option>
        )
        const statusDisplay = (histories) => {
            const lastHistory = histories[histories.length - 1]
            return <StatusDisplay status={t(getHistoryStatusTranslationKey('acte', lastHistory))} date={lastHistory.date}/>
        }
        const natureDisplay = (nature) => t(`acte.nature.${nature}`)
        const decisionDisplay = (decision) => moment(decision).format('DD/MM/YYYY')
        const downloadACKsSelectOption = {
            title: t('acte.list.download_selected_ACKs'),
            titleNoSelection: t('acte.list.download_all_ACKs'),
            action: this.downloadACKs
        }
        const downloadCSVSelectOption = {
            title: t('acte.list.download_selected_CSV'),
            titleNoSelection: t('acte.list.download_all_CSV'),
            action: this.downloadCSV
        }
        const downloadMergedStampedsSelectOption = {
            title: t('acte.list.download_selected_merged_stamped'),
            titleNoSelection: t('acte.list.download_all_merged_stamped'),
            action: this.downloadMergedStamp
        }
        const downloadZipedStampedsSelectOption = {
            title: t('acte.list.download_selected_ziped_stamped'),
            titleNoSelection: t('acte.list.download_all_ziped_stamped'),
            action: this.downloadZipedStamp
        }
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'number', displayed: true, displayName: t('acte.fields.number'), searchable: true, sortable: true, collapsing: true },
            { property: 'objet', displayed: true, displayName: t('acte.fields.objet'), searchable: true, sortable: true },
            { property: 'decision', displayed: true, displayName: t('acte.fields.decision'), searchable: true, displayComponent: decisionDisplay,
                sortable: true, collapsing: true },
            { property: 'nature', displayed: true, displayName: t('acte.fields.nature'), searchable: true, displayComponent: natureDisplay,
                sortable: true, collapsing: true },
            { property: 'code', displayed: false, searchable: false },
            { property: 'creation', displayed: false, searchable: false },
            { property: 'acteHistories', displayed: true, displayName: t('acte.fields.status'), searchable: true, displayComponent: statusDisplay,
                sortable: false, collapsing: true },
            { property: 'public', displayed: false, searchable: false },
            { property: 'publicWebsite', displayed: false, searchable: false },
        ]
        const displayedColumns = metaData.filter(metaData => metaData.displayed)
        const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
        const pagination =
            <Pagination
                columns={displayedColumns.length + 1}
                pageCount={pageCount}
                handlePageClick={this.handlePageClick}
                itemPerPage={this.state.limit}
                updateItemPerPage={this.updateItemPerPage} />
        return (
            <Page title={t('acte.list.title')}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Segment>
                        <AdvancedSearch
                            isDefaultOpen={false}
                            fieldId='multifield'
                            fieldValue={search.multifield}
                            fieldOnChange={this.handleFieldChange}
                            onSubmit={this.submitForm}>

                            <Form onSubmit={this.submitForm}>
                                <FormFieldInline htmlFor='number' label={t('acte.fields.number')} >
                                    <input id='number' value={search.number} onChange={e => this.handleFieldChange('number', e.target.value)} />
                                </FormFieldInline>
                                <FormFieldInline htmlFor='objet' label={t('acte.fields.objet')} >
                                    <input id='objet' value={search.objet} onChange={e => this.handleFieldChange('objet', e.target.value)} />
                                </FormFieldInline>
                                <FormFieldInline htmlFor='decisionFrom' label={t('acte.fields.decision')}>
                                    <Form.Group style={{ marginBottom: 0 }} widths='equal'>
                                        <FormField htmlFor='decisionFrom' label={t('api-gateway:form.from')}>
                                            <InputDatetime id='decisionFrom'
                                                timeFormat={false}
                                                value={search.decisionFrom}
                                                onChange={date => this.handleFieldChange('decisionFrom', date)} />
                                        </FormField>
                                        <FormField htmlFor='decisionTo' label={t('api-gateway:form.to')}>
                                            <InputDatetime id='decisionTo'
                                                timeFormat={false}
                                                value={search.decisionTo}
                                                onChange={date => this.handleFieldChange('decisionTo', date)} />
                                        </FormField>
                                    </Form.Group>
                                </FormFieldInline>
                                <FormFieldInline htmlFor='nature' label={t('acte.fields.nature')}>
                                    <select id='nature' value={search.nature} onChange={e => this.handleFieldChange('nature', e.target.value)}>
                                        <option value=''>{t('api-gateway:form.all_feminine')}</option>
                                        {natureOptions}
                                    </select>
                                </FormFieldInline>
                                <FormFieldInline htmlFor='status' label={t('acte.fields.status')}>
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
                            data={this.state.actes}
                            metaData={metaData}
                            header={true}
                            select={true}
                            search={false}
                            selectOptions={[
                                downloadMergedStampedsSelectOption,
                                downloadZipedStampedsSelectOption,
                                downloadACKsSelectOption,
                                downloadCSVSelectOption
                            ]}
                            link={`/${localAuthoritySlug}/actes/`}
                            linkProperty='uuid'
                            noDataMessage='Aucun acte'
                            keyProperty='uuid'
                            pagination={pagination}
                            sort={this.sort}
                            direction={this.state.direction}
                            column={this.state.column} />
                    </Segment >
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(ActeList)