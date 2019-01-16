import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon, Checkbox, Button, Form } from 'semantic-ui-react'

import { checkStatus, getLocalAuthoritySlug } from '../../_util/utils'
import { notifications } from '../../_util/Notifications'
import history from '../../_util/history'

import { withAuthContext } from '../../Auth'

import StelaTable from '../../_components/StelaTable'
import Breadcrumb from '../../_components/Breadcrumb'
import Pagination from '../../_components/Pagination'
import AdvancedSearch from '../../_components/AdvancedSearch'
import QuickView from '../../_components/QuickView'
import { Page, FormFieldInline } from '../../_components/UI'

class AssemblyTypeList extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    csrfToken: PropTypes.string,
	    csrfTokenHeaderName: PropTypes.string,
	    _addNotification: PropTypes.func,
	}
	state = {
	    assemblyTypes:[],
	    search: {
	        multifield: '',
	        name: '',
	        active: '',
	        location: ''
	    },
	    column: '',
	    direction: '',
	    limit: 10,
	    offset: 0,
	    currentPage: 0,
	    totalCount: 0,
	    quickViewOpened: false,
	    quickViewData: null
	}

	componentDidMount() {
	    const itemPerPage = localStorage.getItem('itemPerPage')
	    if (!itemPerPage) localStorage.setItem('itemPerPage', 10)
	    else this.setState({ limit: 10 }, this.loadData)
	}

	/** Search Function */
	getSearchData = () => {
	    const { limit, offset, direction, column } = this.state
	    const data = { limit, offset, direction, column }
	    Object.keys(this.state.search)
	        .filter(k => this.state.search[k] !== '')
	        .map(k => data[k] = this.state.search[k])
	    return data
	}

	/** Load data list */
	loadData = () => {
	    const { _fetchWithAuthzHandling } = this.context
	    const data = this.getSearchData()
	    _fetchWithAuthzHandling({ url: '/api/convocation/assembly-type', query: data, method: 'GET' })
	        .then(response => response.json())
	        .then((response) => this.setState({assemblyTypes: response.results, totalCount: response.totalCount}))
	}

	/** Display line in negative style if assembly is inactive */
	negativeResolver = (recipient) => {
	    return !recipient.active
	}

	/** Change current page */
	handlePageClick = (data) => {
	    const offset = Math.ceil(data.selected * this.state.limit)
	    this.setState({ offset, currentPage: data.selected }, () => this.loadData())
	}

	/** Sort function */
	sort = (clickedColumn) => {
	    const { column, direction } = this.state
	    if (column !== clickedColumn) {
	        this.setState({ column: clickedColumn, direction: 'ASC' }, () => this.loadData())
	        return
	    }
	    this.setState({ direction: direction === 'ASC' ? 'DESC' : 'ASC' }, () => this.loadData())
	}

	handleFieldChange = (field, value) => {
	    const search = this.state.search
	    search[field] = value
	    this.setState({ search: search })
	}

	updateItemPerPage = (limit) => {
	    this.setState({ limit, offset: 0, currentPage: 0 }, this.loadData)
	}

	onClickCell = (e, property, row) => {
	    e.preventDefault()
	    e.stopPropagation()

	    const data = this.createData(row)
	    this.setState({ quickViewOpened: true, quickViewData: data })

	}

	/** Create HTML for quick view */
	createData = (row) => {
	    const { t } = this.context
	    let recipients = t('convocation.new.no_recipient')
	    if(row.recipients && row.recipients.length > 0) {
	        if (row.recipients.length === 1){
	            recipients = `${row.recipients[0].firstname} ${row.recipients[0].lastname}`
	        }
	        if(row.recipients.length > 1) {
	            let temp = row.recipients.reduce((acc, curr, index) => {
	                return acc.firstname ? acc.firstname + ' ' + acc.lastname + ', ' + curr.firstname + ' ' + curr.lastname : acc + ', ' + curr.firstname + ' ' + curr.lastname
	            })
	            recipients = temp
	        }
	    }

	    return {
	        headerContent: row.name,
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
			    <Button type="button" basic primary onClick={() => this.onEditAssemblyType(row)}>
			        {t('convocation.admin.modules.convocation.recipient_config.edit')}
			    </Button>
			</div>,
	        data: [
	            {label: t('convocation.admin.modules.convocation.assembly_type_config.place'), value: row.location, id: 'location', computer: '16'},
	            {label: t('convocation.admin.modules.convocation.assembly_type_config.convocation_delay'), value: row.delay, id: 'delay', computer: '8'},
	            {label: t('convocation.admin.modules.convocation.assembly_type_config.reminder_time'), value: row.reminderDelay, id: 'reminderDelay', computer: '8'},
	            {label: t('convocation.admin.modules.convocation.assembly_types'), value: recipients, id: 'recipients', computer: '16'}
	        ]
	    }
	}
	onEditAssemblyType = (assembly) => {
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    history.push(`/${localAuthoritySlug}/admin/convocation/type-assemblee/liste-type-assemblee/${assembly.uuid}`)
	}
	onCloseQuickView = () => {
	    this.setState({ quickViewOpened: false })
	}

	handleFieldCheckboxChange = (row) => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const url = `/api/convocation/assembly-type/${row.uuid}`
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

	render() {
	    const { t } = this.context
	    const { search } = this.state

	    const statusDisplay = (active) => active ? t('convocation.admin.modules.convocation.recipient_list.active') : t('convocation.admin.modules.convocation.recipient_list.inactive')
	    const recipientsDisplay = (recipients) => {
	        if(recipients && recipients.length > 0) {
	            let temp = recipients
	            if (recipients.length === 1){
	                return <span>{recipients[0].firstname} {recipients[0].lastname}</span>
	            }
	            if(recipients.length > 2) {
	                temp = recipients.slice(0, 2)
	            }
	            temp = temp.reduce((acc, curr, index) => {
	                if(index > 0) {
	                    acc = acc.firstname + ' ' + acc.lastname + ', '
	                }
	                return acc + curr.firstname + ' ' + curr.lastname
	            })
	            if(recipients.length > 2) {
	                return <span>{temp},... <span style={{fontStyle: 'italic', marginLeft: '5px'}}>Voir Tout <Icon name='arrow right'/></span></span>
	            }
	            return <span>{temp}</span>
	        }
	        return ''
	    }
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'name', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.assembly_type') },
	        { property: 'location', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.place') },
	        { property: 'delay', displayed: true, searchable: false, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.convocation_delay') },
	        { property: 'reminderDelay', displayed: true, searchable: false, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.reminder_time') },
	        { property: 'recipients', displayed: true, searchable: false, sortable: false, displayName: t('convocation.admin.modules.convocation.assembly_type_config.recipients'), displayComponent: recipientsDisplay },
	        { property: 'active', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.status'), displayComponent: statusDisplay },
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
                handlePageClick={this.handlePageClick}
                itemPerPage={this.state.limit}
                updateItemPerPage={this.updateItemPerPage}
                currentPage={this.state.currentPage}
                options={options} />
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    return (
	        <Page>
	            <Breadcrumb
	                data={[
	                    {title: 'Accueil Admin', url: `/${localAuthoritySlug}/admin/ma-collectivite`},
	                    {title: 'Convocation', url: `/${localAuthoritySlug}/admin/convocation/parametrage-module`},
	                    {title: 'Liste des types d\'assemblée'}
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
	                    fieldOnChange={this.handleFieldChange}
	                    onSubmit={this.loadData}>
	                    <Form onSubmit={this.loadData}>
	                        <FormFieldInline htmlFor='name' label={t('convocation.fields.assembly_type')} >
	                            <input id='name' aria-label={t('convocation.fields.assembly_type')} value={search.name} onChange={e => this.handleFieldChange('name', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='location' label={t('convocation.admin.modules.convocation.assembly_type_config.place')} >
	                            <input id='location' aria-label={t('convocation.admin.modules.convocation.assembly_type_config.place')} value={search.location} onChange={e => this.handleFieldChange('location', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='active' label={t('convocation.admin.modules.convocation.recipient_config.status')}>
	                            <select id='active' aria-label={t('convocation.admin.modules.convocation.recipient_config.status')} onBlur={e => this.handleFieldChange('active', e.target.value)}>
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
	                    data={this.state.assemblyTypes}
	                    keyProperty="uuid"
	                    striped={false}
	                    negativeResolver={this.negativeResolver}
	                    pagination={pagination}
	                    sort={this.sort}
	                    direction={this.state.direction}
	                    column={this.state.column}
	                    noDataMessage={t('convocation.admin.modules.convocation.assembly_type_liste.no_assembly_type')}
	                />
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(AssemblyTypeList))