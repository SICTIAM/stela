import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon, Button, Checkbox, Form } from 'semantic-ui-react'

import history from '../../_util/history'
import {
    checkStatus,
    getLocalAuthoritySlug,
    handleSearchChange,
    handlePageClick,
    updateItemPerPage,
    sortTable
} from '../../_util/utils'
import { notifications } from '../../_util/Notifications'

import { withAuthContext } from '../../Auth'

import StelaTable from '../../_components/StelaTable'
import AdvancedSearch from '../../_components/AdvancedSearch'
import { Page, FormFieldInline } from '../../_components/UI'
import Pagination from '../../_components/Pagination'
import QuickView from '../../_components/QuickView'
import Breadcrumb from '../../_components/Breadcrumb'

class RecipientsList extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    csrfToken: PropTypes.string,
	    csrfTokenHeaderName: PropTypes.string,
	    _addNotification: PropTypes.func,
	}
	state = {
	    recipients:[],
	    search: {
	        multifield: '',
	        fistname: '',
	        lastname: '',
	        email: '',
	        active: ''
	    },
	    column: '',
	    direction: '',
	    limit: 10,
	    offset: 0,
	    currentPage: 0,
	    totalCount: 0,
	    quickViewData: null,
	    quickViewOpened: false
	}
	componentDidMount() {
	    const itemPerPage = localStorage.getItem('itemPerPage')
	    if (!itemPerPage) localStorage.setItem('itemPerPage', 10)
	    else this.setState({ limit: 10 }, this.loadData)
	}

	loadData = () => {
	    const { _fetchWithAuthzHandling } = this.context
	    const data = this.getSearchData()
	    _fetchWithAuthzHandling({ url: '/api/convocation/recipient', query: data, method: 'GET' })
	        .then(response => response.json())
	        .then((response) => this.setState({recipients: response.results, totalCount: response.totalCount}))
	}

	getSearchData = () => {
	    const { limit, offset, direction, column } = this.state
	    const data = { limit, offset, direction, column }
	    Object.keys(this.state.search)
	        .filter(k => this.state.search[k] !== '')
	        .map(k => data[k] = this.state.search[k])
	    return data
	}

	negativeResolver = (recipient) => {
	    return !recipient.active
	}
	onClickCell = (e, property, row) => {
	    e.preventDefault()
	    e.stopPropagation()

	    const data = this.createData(row)
	    this.setState({ quickViewOpened: true, quickViewData: data })

	}
	createData = (row) => {
	    const { t } = this.context
	    let assemblyTypes = t('convocation.admin.modules.convocation.assembly_type_liste.no_assembly_type')
	    if(row.assemblyTypes && row.assemblyTypes.length > 0) {
	        if (row.assemblyTypes.length === 1){
	            assemblyTypes = row.assemblyTypes[0].name
	        }
	        if(row.assemblyTypes.length > 1) {
	            let temp = row.assemblyTypes.reduce((acc, curr, index) => {
	                return acc.name ? acc.name + ', ' + curr.name : acc + ', ' + curr.name
	            })
	            assemblyTypes = temp
	        }
	    }

	    return {
	        headerContent: row.firstname + ' ' + row.lastname,
	        action:
			<div>
			    <label htmlFor='status'>
			        {row.active && (
			            <span style={{verticalAlign: 'super', marginRight: '5px', fontStyle: 'italic'}}>{t('convocation.admin.modules.convocation.recipient_list.deactivate')}</span>
			        )}
			        {!row.active && (
			            <span style={{verticalAlign: 'super', marginRight: '5px', fontStyle: 'italic'}}>{t('convocation.admin.modules.convocation.recipient_list.activate')}</span>
			        )}
			    	<Checkbox id='status' toggle className='mr-20' checked={row.active} onChange={e => this.handleFieldCheckboxChange(row)}/>
			    </label>
			    <Button type="button" basic primary onClick={() => this.onEditRecipient(row)}>
			        {t('convocation.admin.modules.convocation.recipient_config.edit')}
			    </Button>
			</div>,
	        data: [
	            {label: t('convocation.admin.modules.convocation.recipient_config.email'), value: row.email, id: 'email', computer: '8'},
	            {label: t('convocation.admin.modules.convocation.recipient_config.phonenumber'), value: row.phoneNumber, id: 'phoneNumber', computer: '8'},
	            {label: t('convocation.admin.modules.convocation.assembly_types'), value: assemblyTypes, id: 'assemblyType', computer: '16'}
	        ]
	    }
	}
	onEditRecipient = (recipient) => {
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    history.push(`/${localAuthoritySlug}/admin/convocation/destinataire/liste-destinataires/${recipient.uuid}`)
	}

	handleFieldCheckboxChange = (row) => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const url = !row.active ? `/api/convocation/recipient/${row.uuid}` : `/api/convocation/recipient/${row.uuid}`
	    const body = {
	        active: !row.active ? true : false
	    }
	    const headers = { 'Content-Type': 'application/json' }

	    _fetchWithAuthzHandling({url: url, method: 'PUT', headers: headers, body: JSON.stringify(body), context: this.props.authContext})
	        .then(checkStatus)
	        .then(response => {
	            _addNotification(notifications.admin.statusUpdated)
	            this.loadData()
	            row.active = !row.active
	            const data = this.createData(row)
	            this.setState({ quickViewData: data })
	        })
	}
	onCloseQuickView = () => {
	    this.setState({ quickViewOpened: false })
	}
	render() {
	    const { t } = this.context
	    const { search } = this.state
	    const statusDisplay = (active) => active ? t('convocation.admin.modules.convocation.recipient_list.active') : t('convocation.admin.modules.convocation.recipient_list.inactive')
	    const assemblyTypes = (assemblyTypes) => {
	        if(assemblyTypes && assemblyTypes.length > 0) {
	            let temp = assemblyTypes
	            if (assemblyTypes.length === 1){
	                return <span>{assemblyTypes[0].name}</span>
	            }
	            if(assemblyTypes.length > 2) {
	                temp = assemblyTypes.slice(0, 2)
	            }
	            temp = temp.reduce((acc, curr, index) => {
	                if(index > 0) {
	                    acc = acc.name + ', '
	                }
	                return acc + curr.name
	            })
	            if(assemblyTypes.length > 2) {
	                return <span>{temp},... <span style={{fontStyle: 'italic', marginLeft: '5px'}}>Voir Tout <Icon name='arrow right'/></span></span>
	            }
	            return <span>{temp}</span>
	        }
	        return ''
	    }
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'firstname', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.firstname') },
	        { property: 'lastname', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.lastname') },
	        { property: 'email', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.email') },
	        { property: 'phoneNumber', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.phonenumber') },
	        { property: 'assemblyTypes', displayed: true, searchable: false, sortable: false, displayName: t('convocation.admin.modules.convocation.assembly_types'), displayComponent: assemblyTypes },
	        { property: 'active', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.status'), displayComponent: statusDisplay }
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
	                    {title: t('api-gateway:breadcrumb.admin_home'), url: `/${localAuthoritySlug}/admin/ma-collectivite`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation'), url: `/${localAuthoritySlug}/admin/ma-collectivite/convocation`},
	                    {title: t('api-gateway:breadcrumb.convocation.recipients_list')}
	                ]}
	            />
	            <QuickView
	                open={this.state.quickViewOpened}
	                header={true}
	                data={this.state.quickViewData}
	                onClose={this.onCloseQuickView}></QuickView>
	            <Segment>
	                <AdvancedSearch
	                    isDefaultOpen={false}
	                    fieldId='multifield'
	                    fieldValue={search.multifield}
	                    fieldOnChange={(id, value) => handleSearchChange(this, id, value)}
	                    onSubmit={this.loadData}>
	                    <Form onSubmit={this.loadData}>
	                        <FormFieldInline htmlFor='firstname' label={t('convocation.admin.modules.convocation.recipient_config.firstname')} >
	                            <input id='firstname' aria-label={t('convocation.admin.modules.convocation.recipient_config.firstname')} value={search.firstname} onChange={e => handleSearchChange(this, 'firstname', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='name' label={t('convocation.admin.modules.convocation.recipient_config.lastname')} >
	                            <input id='lastname' aria-label={t('convocation.admin.modules.convocation.recipient_config.lastname')} value={search.lastname} onChange={e => handleSearchChange(this, 'lastname', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='email' label={t('convocation.admin.modules.convocation.recipient_config.email')} >
	                            <input id='email' aria-label={t('convocation.admin.modules.convocation.recipient_config.email')} value={search.email} onChange={e => handleSearchChange(this, 'email', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='active' label={t('convocation.admin.modules.convocation.recipient_config.status')}>
	                            <select id='active' aria-label={t('convocation.admin.modules.convocation.recipient_config.status')} onBlur={e => handleSearchChange(this, 'active', e.target.value)}>
	                                <option value=''>{t('convocation.admin.modules.convocation.recipient_list.active_inactive')}</option>
	                                <option value={true}>{t('convocation.admin.modules.convocation.recipient_list.active')}</option>
	                                <option value={false}>{t('convocation.admin.modules.convocation.recipient_list.inactive')}</option>
	                            </select>
	                        </FormFieldInline>
	                        <div style={{ textAlign: 'right' }}>
	                            <Button type='submit' basic primary>{t('api-gateway:form.search')}</Button>
	                        </div>
	                    </Form>
	                </AdvancedSearch>
	                <StelaTable
	                    header={true}
	                    search={false}
	                    click={true}
	                    onClick={this.onClickCell}
	                    sortable={true}
	                    metaData={metaData}
	                    data={this.state.recipients}
	                    keyProperty="uuid"
	                    striped={false}
	                    negativeResolver={this.negativeResolver}
	                    pagination={pagination}
	                    sort={(clickedColumn) => sortTable(this, clickedColumn, this.loadData)}
	                    direction={this.state.direction}
	                    column={this.state.column}
	                    noDataMessage={t('convocation.new.no_recipient')}
	                />
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(RecipientsList))