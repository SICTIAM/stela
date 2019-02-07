import React, { Component } from 'react'
import { translate } from 'react-i18next'
import { Segment, Form, Button } from 'semantic-ui-react'
import PropTypes from 'prop-types'
import moment from 'moment'

import { checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'

import {
    getLocalAuthoritySlug,
    handleSearchChange,
    updateItemPerPage,
    handlePageClick,
    sortTable
} from '../_util/utils'

import StelaTable from '../_components/StelaTable'
import Breadcrumb from '../_components/Breadcrumb'
import InputDatetime from '../_components/InputDatetime'
import AdvancedSearch from '../_components/AdvancedSearch'
import Pagination from '../_components/Pagination'
import { Page, FormFieldInline, FormField } from '../_components/UI'

class SentConvocation extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	state = {
	    sentConvocation: [],
	    search: {
	        multifield: '',
	        subject: '',
	        assemblyType: '',
	        sentDateFrom: '',
	        sentDateTo: ''
	    },
	    column: '',
	    direction: '',
	    limit: 10,
	    offset: 0,
	    currentPage: 0,
	    totalCount: 0,
	}

	componentDidMount() {
	    const itemPerPage = localStorage.getItem('itemPerPage')
	    if (!itemPerPage) localStorage.setItem('itemPerPage', 10)
	    else this.setState({ limit: 10 }, this.loadData)
	}
	/** Load data list */
	loadData = () => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const data = this.getSearchData()

	    _fetchWithAuthzHandling({ url: '/api/convocation/sent', query: data, method: 'GET' })
	        .then(checkStatus)
	        .then(response => response.json())
	        .then((response) => this.setState({sentConvocation: response.results, totalCount: response.totalCount}))
	        .catch(response => {
	            response.json().then(json => {
	                _addNotification(notifications.defaultError, 'notifications.title', json.message)
	            })
	        })
	}
	/** Search Function */

	getSearchData = () => {
	    const { limit, offset, direction, column } = this.state
	    const data = { limit, offset, direction, column }
	    Object.keys(this.state.search)
	        .filter(k => this.state.search[k] !== '')
	        .map(k => data[k] = this.state.search[k])
	    if (data.sentDateFrom) data.sentDateFrom = moment(data.sentDateFrom).format('YYYY-MM-DD')
	    if (data.sentDateTo) data.sentDateTo = moment(data.sentDateTo).format('YYYY-MM-DD')
	    if (data.meetingDateTo) data.meetingDateTo = moment(data.meetingDateTo).format('YYYY-MM-DD')
	    if (data.meetingDateFrom) data.meetingDateFrom = moment(data.meetingDateFrom).format('YYYY-MM-DD')
	    return data
	}

	render() {
	    const { t } = this.context
	    const { search } = this.state
	    const dateDisplay = (date) => date && moment(date, 'YYYY-MM-DDTHH:mm:ss').format('DD-MM-YYYY HH:mm')
	    const assemblyTypeDisplay = (type) => type && type.name
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'sentDate', displayed: true, searchable: true, sortable: true, displayName: t('convocation.list.sent_date'), displayComponent: dateDisplay},
	        { property: 'assemblyType', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.assembly_type'), displayComponent: assemblyTypeDisplay},
	        { property: 'meetingDate', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.date'), displayComponent: dateDisplay},
	        { property: 'subject', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.object')}
	    ]
	    const options = [
	        { key: 10, text: 10, value: 10 },
	        { key: 25, text: 25, value: 25 },
	        { key: 50, text: 50, value: 50 },
	        { key: 100, text: 100, value: 100 }]
	    const displayedColumns = metaData.filter(metaData => metaData.displayed)
	    const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
	    const pagination =
            <Pagination
                columns={displayedColumns.length}
                pageCount={pageCount}
                handlePageClick={(data) => handlePageClick(this, data, this.loadData)}
                itemPerPage={this.state.limit}
                updateItemPerPage={(itemPerPage) => updateItemPerPage(this, itemPerPage, this.loadData)}
                currentPage={this.state.currentPage}
                options={options} />
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    return (
	        <Page>
	            <Breadcrumb
	                data={[
	                    {title: t('api-gateway:breadcrumb.home'), url: `/${localAuthoritySlug}`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation')},
	                    {title: t('api-gateway:breadcrumb.convocation.sent_convocations_list')}
	                ]}
	            />
	            <Segment>
	                <AdvancedSearch
	                    isDefaultOpen={false}
	                    fieldId='multifield'
	                    fieldValue={search.multifield}
	                    fieldOnChange={(id, value) => handleSearchChange(this, id, value)}
	                    onSubmit={this.loadData}>
	                    <Form onSubmit={this.loadData}>
	                        <FormFieldInline htmlFor='assemblyType' label={t('convocation.fields.assembly_type')} >
	                            <input id='assemblyType' aria-label={t('convocation.fields.assembly_type')} value={search.assemblyType} onChange={e => handleSearchChange(this, 'assemblyType', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='subject' label={t('convocation.fields.object')} >
	                            <input id='subject' aria-label={t('convocation.fields.object')} value={search.subject} onChange={e => handleSearchChange(this, 'subject', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='sentDateFrom' label={t('convocation.list.sent_date')}>
	                            <Form.Group style={{ marginBottom: 0 }} widths='equal'>
	                                <FormField htmlFor='sentDateFrom' label={t('api-gateway:form.from')}>
	                                    <InputDatetime id='sentDateFrom'
	                                        ariaLabel={t('api-gateway:form.decision_from')}
	                                        timeFormat={false}
	                                        value={search.sentDateFrom}
	                                        onChange={date => handleSearchChange(this, 'sentDateFrom', date)} />
	                                </FormField>
	                                <FormField htmlFor='sentDateTo' label={t('api-gateway:form.to')}>
	                                    <InputDatetime id='sentDateTo'
	                                        timeFormat={false}
	                                        ariaLabel={t('api-gateway:form.decision_to')}
	                                        value={search.sentDateTo}
	                                        onChange={date => handleSearchChange(this, 'sentDateTo', date)} />
	                                </FormField>
	                            </Form.Group>
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='meetingDateFrom' label={t('convocation.fields.date')}>
	                            <Form.Group style={{ marginBottom: 0 }} widths='equal'>
	                                <FormField htmlFor='meetingDateFrom' label={t('api-gateway:form.from')}>
	                                    <InputDatetime id='meetingDateFrom'
	                                        ariaLabel={t('api-gateway:form.decision_from')}
	                                        timeFormat={false}
	                                        value={search.meetingDateFrom}
	                                        onChange={date => handleSearchChange(this, 'meetingDateFrom', date)} />
	                                </FormField>
	                                <FormField htmlFor='meetingDateTo' label={t('api-gateway:form.to')}>
	                                    <InputDatetime id='meetingDateTo'
	                                        timeFormat={false}
	                                        ariaLabel={t('api-gateway:form.decision_to')}
	                                        value={search.meetingDateTo}
	                                        onChange={date => handleSearchChange(this, 'meetingDateTo', date)} />
	                                </FormField>
	                            </Form.Group>
	                        </FormFieldInline>
	                        <div style={{ textAlign: 'right' }}>
	                            <Button type='submit' basic primary>{t('api-gateway:form.search')}</Button>
	                        </div>
	                    </Form>
	                </AdvancedSearch>
	                <StelaTable
	                    header={true}
	                    search={false}
	                    sortable={true}
	                    metaData={metaData}
	                    data={this.state.sentConvocation}
	                    keyProperty="uuid"
	                    striped={false}
	                    pagination={pagination}
	                    sort={(clickedColumn) => sortTable(this, clickedColumn, this.loadData)}
	                    direction={this.state.direction}
	                    column={this.state.column}
	                    link={`/${localAuthoritySlug}/convocation/liste-envoyees/`}
	                    linkProperty='uuid'
	                    noDataMessage={t('convocation.admin.modules.convocation.sent_convocation_list.no_sent_convocation')}
	                />
	            </Segment>
	        </Page>
	    )
	}
}
export default translate(['convocation', 'api-gateway'])(SentConvocation)