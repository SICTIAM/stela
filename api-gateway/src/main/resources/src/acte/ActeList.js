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
import {
    checkStatus,
    getHistoryStatusTranslationKey,
    getLocalAuthoritySlug,
    handleSearchChange,
    handlePageClick,
    updateItemPerPage,
    sortTable,
    onSearch
} from '../_util/utils'
import { notifications } from '../_util/Notifications'
import { FormFieldInline, FormField, Page, LoadingContent, StatusDisplay } from '../_components/UI'
import { natures, status, anomalies } from '../_util/constants'
import { withAuthContext } from '../Auth'

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
        currentPage: 0,
        fetchStatus: ''
    }
    componentDidMount() {
        const itemPerPage = localStorage.getItem('itemPerPage')
        if (!itemPerPage) localStorage.setItem('itemPerPage', this.state.limit)
        else this.setState({ limit: parseInt(itemPerPage, 10) }, this.submitForm)
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
        _fetchWithAuthzHandling({ url: url, body: JSON.stringify(ActeUuidsAndSearchUI), headers: headers, method: 'POST', context: this.props.authContext })
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
    negativeResolver = acte => {
        return anomalies.includes(acte.lastHistoryStatus)
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
        const statusDisplay = (acte) => {
            const lastHistory = {
                status: acte.lastHistoryStatus,
                flux: acte.lastHistoryFlux
            }
            return <StatusDisplay status={t(getHistoryStatusTranslationKey('acte', lastHistory))} date={acte.lastHistoryDate}/>
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
            { property: '_self', displayed: true, displayName: t('acte.fields.status'), searchable: true, displayComponent: statusDisplay,
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
                handlePageClick={(data) => handlePageClick(this, data, this.submitForm)}
                itemPerPage={this.state.limit}
                updateItemPerPage={(itemPerPage) => updateItemPerPage(this, itemPerPage, this.submitForm)}
                currentPage={this.state.currentPage} />
        return (
            <Page title={t('acte.list.title')}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Segment>
                        <AdvancedSearch
                            isDefaultOpen={false}
                            fieldId='multifield'
                            fieldValue={search.multifield}
                            fieldOnChange={(id, value) => handleSearchChange(this, id, value)}
                            onSubmit={() => onSearch(this, this.submitForm)}>

                            <Form onSubmit={() => onSearch(this, this.submitForm)}>
                                <FormFieldInline htmlFor='number' label={t('acte.fields.number')} >
                                    <input id='number' aria-label={t('acte.fields.number')} value={search.number} onChange={e => handleSearchChange(this, 'number', e.target.value)} />
                                </FormFieldInline>
                                <FormFieldInline htmlFor='objet' label={t('acte.fields.objet')} >
                                    <input id='objet' aria-label={t('acte.fields.objet')} value={search.objet} onChange={e => handleSearchChange(this, 'objet', e.target.value)} />
                                </FormFieldInline>
                                <FormFieldInline htmlFor='decisionFrom' label={t('acte.fields.decision')}>
                                    <Form.Group style={{ marginBottom: 0 }} widths='equal'>
                                        <FormField htmlFor='decisionFrom' label={t('api-gateway:form.from')}>
                                            <InputDatetime id='decisionFrom'
                                                ariaLabel={t('api-gateway:form.decision_from')}
                                                timeFormat={false}
                                                value={search.decisionFrom}
                                                onChange={date => handleSearchChange(this, 'decisionFrom', date)} />
                                        </FormField>
                                        <FormField htmlFor='decisionTo' label={t('api-gateway:form.to')}>
                                            <InputDatetime id='decisionTo'
                                                timeFormat={false}
                                                ariaLabel={t('api-gateway:form.decision_to')}
                                                value={search.decisionTo}
                                                onChange={date => handleSearchChange(this, 'decisionTo', date)} />
                                        </FormField>
                                    </Form.Group>
                                </FormFieldInline>
                                <FormFieldInline htmlFor='nature' label={t('acte.fields.nature')}>
                                    <select id='nature' value={search.nature} onBlur={e => handleSearchChange(this, 'nature', e.target.value)} onChange={e => handleSearchChange(this, 'nature', e.target.value)}>
                                        <option value=''>{t('api-gateway:form.all_feminine')}</option>
                                        {natureOptions}
                                    </select>
                                </FormFieldInline>
                                <FormFieldInline htmlFor='status' label={t('acte.fields.status')}>
                                    <select id='status' value={search.status} onBlur={e => handleSearchChange(this, 'status', e.target.value)} onChange={e => handleSearchChange(this, 'status', e.target.value)}>
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
                            title={t('acte.list.summary')}
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
                            link={`/${localAuthoritySlug}/actes/liste/`}
                            linkProperty='uuid'
                            noDataMessage='Aucun acte'
                            keyProperty='uuid'
                            pagination={pagination}
                            sort={(clickedColumn) => sortTable(this, clickedColumn, this.submitForm)}
                            direction={this.state.direction}
                            column={this.state.column}
                            negativeResolver={this.negativeResolver} />
                    </Segment >
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(withAuthContext(ActeList))