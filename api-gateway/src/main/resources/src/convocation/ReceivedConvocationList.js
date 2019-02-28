/* eslint-disable jsx-a11y/no-onchange */
/* eslint-disable default-case */
import React, { Component } from 'react'
import { translate } from 'react-i18next'
import { Segment, Form, Button } from 'semantic-ui-react'
import PropTypes from 'prop-types'
import moment from 'moment'

import {
    getLocalAuthoritySlug,
    updateItemPerPage,
    handlePageClick,
    sortTable,
    convertDateBackFormatToUIFormat
} from '../_util/utils'
import ConvocationService from '../_util/convocation-service'

import StelaTable from '../_components/StelaTable'
import Breadcrumb from '../_components/Breadcrumb'
import Pagination from '../_components/Pagination'
import InputDatetime from '../_components/InputDatetime'
import AdvancedSearch from '../_components/AdvancedSearch'
import { Page, FormFieldInline, FormField } from '../_components/UI'

class ReceivedConvocation extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func
	}
	state = {
	    receivedConvocation: [],
	    search: {
	        multifield: '',
	        subject: '',
	        assemblyType: '',
	        meetingDateFrom: '',
	        meetingDateTo: '',
	        filter: 'future'
	    },
	    column: '',
	    direction: 'ASC',
	    limit: 10,
	    offset: 0,
	    currentPage: 0,
	    totalCount: 0,
	    assemblyTypes: []
	}

	componentDidMount = async() => {
	    this._convocationService = new ConvocationService()

	    const itemPerPage = localStorage.getItem('itemPerPage')
	    if (!itemPerPage) {
	        localStorage.setItem('itemPerPage', 10)
	        await this.loadData()

	    }
	    else this.setState({ limit: 10 }, await this.loadData)
	    const assemblyTypesResponse = await this._convocationService.getAllAssemblyType(this.context, this.props.location.search)
	    this.setState({assemblyTypes: assemblyTypesResponse.map(item => { return {text: item.name, uuid: item.uuid}})})
	}
	/** Load data list */
	loadData = async () => {
	    const search = this.getSearchData()
	    const convocationsResponse = await this._convocationService.getReceivedConvocationList(this.context, search, this.props.location.search)
	    this.setState({receivedConvocation: convocationsResponse.results, totalCount: convocationsResponse.totalCount})
	}
	/** Search Function */
	getSearchData = () => {
	    const { limit, offset, direction, column } = this.state
	    const data = { limit, offset, direction, column }
	    Object.keys(this.state.search)
	        .filter(k => this.state.search[k] !== '')
	        .map(k => data[k] = this.state.search[k])
	    if (data.meetingDateTo) data.meetingDateTo = moment(data.meetingDateTo).format('YYYY-MM-DD')
	    if (data.meetingDateFrom) data.meetingDateFrom = moment(data.meetingDateFrom).format('YYYY-MM-DD')
	    return data
	}

	greyResolver = (convocation) => {
	    return !convocation.opened
	}

	negativeResolver = (convocation) => {
	    return convocation.cancelled
	}

	handleSearchChange = (field, value, callback) => {
	    const search = this.state.search
	    search[field] = value
	    let direction = 'DESC'
	    if(field === 'meetingDateFrom' || field === 'meetingDateTo') {
	        search['filter'] = ''
	    }
	    if(field === 'filter' && value !== '') {
	        search['meetingDateFrom'] = ''
	        search['meetingDateTo'] = ''
	        direction = value === 'future' ? 'ASC' : 'DESC'
	    }
	    this.setState({search, offset: 0, currentPage: 0, direction}, callback)
	}

	render() {
	    const { t } = this.context
	    const { search, assemblyTypes } = this.state
	    const dateDisplay = (date) => date && convertDateBackFormatToUIFormat(date, 'DD/MM/YYYY HH:mm')

	    const answerDisplay = (answer) => {
	        switch(answer) {
	        case 'PRESENT': return <p className='green text-bold'>{t('convocation.page.present')}</p>
	        case 'NOT_PRESENT': return <p className='red'>{t('convocation.page.absent')}</p>
	        case 'SUBSTITUTED': return <p className='red'>{t('convocation.page.substituted')}</p>
	        default: return ''
	        }
	    }
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'meetingDate', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.date'), displayComponent: dateDisplay},
	        { property: 'assemblyType', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.assembly_type')},
	        { property: 'subject', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.object')},
	        { property: 'response', displayed: true, searchable: true, sortable: true, displayName: t('convocation.page.my_answer'), displayComponent: answerDisplay}
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

	    const assemblyTypesFilter = assemblyTypes.map(assemblyType => {
	        return <option key={assemblyType.uuid} value={assemblyType.uuid}>{assemblyType.text}</option>
	    })

	    const additionnalFilter =
			<Form>
			    <select id='convocationFilter' aria-label={t('convocation.list.convocation_filters')} value={this.state.search.filter} onChange={e => this.handleSearchChange('filter', e.target.value, this.loadData)}>
			        <option value='future'>{t('convocation.list.future_convocation')}</option>
			        <option value='past'>{t('convocation.list.past_convocation')}</option>
			        <option value=''>{t('convocation.list.all_convocation')}</option>
			    </select>
			</Form>

	    return (
	        <Page>
	            <Breadcrumb
	                data={[
	                    {title: t('api-gateway:breadcrumb.home'), url: `/${localAuthoritySlug}`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation')},
	                    {title: t('api-gateway:breadcrumb.convocation.reveived_convocations_list')}
	                ]}
	            />
	            <Segment>
	                <AdvancedSearch
	                    isDefaultOpen={false}
	                    fieldId='multifield'
	                    fieldValue={search.multifield}
	                    fieldOnChange={(id, value) => this.handleSearchChange(id, value)}
	                    onSubmit={this.loadData}
	                    additionnalFilter={additionnalFilter}>
	                    <Form onSubmit={this.loadData}>
	                        <FormFieldInline htmlFor='assemblyType' label={t('convocation.fields.assembly_type')} >
	                            <select id='assemblyType' aria-label={t('convocation.fields.assembly_type')} onBlur={e => this.handleSearchChange('assemblyType', e.target.value)}>
	                                <option key='all' value=''>{t('convocation.list.all_convocation_type')}</option>
	                                {assemblyTypesFilter}
	                            </select>
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='subject' label={t('convocation.fields.object')} >
	                            <input id='subject' aria-label={t('convocation.fields.object')} value={search.subject} onChange={e => this.handleSearchChange('subject', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='meetingDateFrom' label={t('convocation.fields.date')}>
	                            <Form.Group style={{ marginBottom: 0 }} widths='equal'>
	                                <FormField htmlFor='meetingDateFrom' label={t('api-gateway:form.from')}>
	                                    <InputDatetime id='meetingDateFrom'
	                                        ariaLabel={t('api-gateway:form.decision_from')}
	                                        timeFormat={false}
	                                        viewDate={this.state.search.meetingDateTo}
	                                        value={search.meetingDateFrom}
	                                        onChange={date => this.handleSearchChange('meetingDateFrom', date)} />
	                                </FormField>
	                                <FormField htmlFor='meetingDateTo' label={t('api-gateway:form.to')}>
	                                    <InputDatetime id='meetingDateTo'
	                                        timeFormat={false}
	                                        viewDate={this.state.search.meetingDateFrom}
	                                        ariaLabel={t('api-gateway:form.decision_to')}
	                                        value={search.meetingDateTo}
	                                        onChange={date => this.handleSearchChange('meetingDateTo', date)} />
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
	                    data={this.state.receivedConvocation}
	                    keyProperty="uuid"
	                    striped={false}
	                    greyResolver={this.greyResolver}
	                    pagination={pagination}
	                    sort={(clickedColumn) => sortTable(this, clickedColumn, this.loadData)}
	                    direction={this.state.direction}
	                    column={this.state.column}
	                    link={`/${localAuthoritySlug}/convocation/liste-recues/`}
	                    linkProperty='uuid'
	                    noDataMessage={t('convocation.list.no_received_convocation')}
	                    negativeResolver={this.negativeResolver}
	                />
	            </Segment>
	        </Page>
	    )
	}
}
export default translate(['convocation', 'api-gateway'])(ReceivedConvocation)