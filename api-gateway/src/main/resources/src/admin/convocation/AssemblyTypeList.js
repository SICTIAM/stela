import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon, Button, Form } from 'semantic-ui-react'

import {
    getLocalAuthoritySlug,
    handleSearchChange,
    handlePageClick,
    updateItemPerPage,
    sortTable
} from '../../_util/utils'
import { notifications } from '../../_util/Notifications'

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
	    totalCount: 0
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
	lineThroughResolver = (recipient) => {
	    return !recipient.active
	}

	handleFieldCheckboxChange = (row) => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const url = `/api/convocation/assembly-type/${row.uuid}`
	    const body = {
	        active: !row.active ? true : false
	    }
	    const headers = { 'Content-Type': 'application/json' }

	    _fetchWithAuthzHandling({url: url, method: 'PUT', headers: headers, body: JSON.stringify(body), context: this.props.authContext})
	        .then(response => {
	            _addNotification(notifications.admin.statusUpdated)
	            this.loadData()
	            row.active = !row.active
	        })
	}

	render() {
	    const { t } = this.context
	    const { search } = this.state

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

	    const checkboxDisplay = (value) => value ? <Icon name='check' color='green'/>: <Icon name='close' color='red'/>
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'name', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.assembly_type') },
	        { property: 'location', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.place') },
	        { property: 'delay', width: 'one', displayed: true, searchable: false, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.convocation_delay') },
	        { property: 'reminder', displayed: true, searchable: false, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.reminder'), displayComponent: checkboxDisplay },
	        { property: 'useProcuration', width: 'one', displayed: true, searchable: false, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.procuration'), displayComponent: checkboxDisplay },
	        { property: 'recipients', displayed: true, searchable: false, sortable: false, displayName: t('convocation.admin.modules.convocation.assembly_type_config.recipients'), displayComponent: recipientsDisplay }	    ]
	    const options = [
	        { key: 10, text: 10, value: 10 },
	        { key: 25, text: 25, value: 25 },
	        { key: 50, text: 50, value: 50 },
	        { key: 100, text: 100, value: 100 }]
	    const displayedColumns = metaData.filter(metaData => metaData.displayed)
	    const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
	    const pagination =
            <Pagination
                columns={displayedColumns.length +1}
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
	                    {title: t('api-gateway:breadcrumb.convocation.assembly_type_list')}
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
	                        <FormFieldInline htmlFor='name' label={t('convocation.fields.assembly_type')} >
	                            <input id='name' aria-label={t('convocation.fields.assembly_type')} value={search.name} onChange={e => handleSearchChange(this, 'name', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='location' label={t('convocation.admin.modules.convocation.assembly_type_config.place')} >
	                            <input id='location' aria-label={t('convocation.admin.modules.convocation.assembly_type_config.place')} value={search.location} onChange={e => handleSearchChange(this, 'location', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='active' label={t('convocation.admin.modules.convocation.recipient_config.status')}>
	                            <select id='active' aria-label={t('convocation.admin.modules.convocation.recipient_config.status')} onBlur={e => handleSearchChange(this, 'active', e.target.value)}>
	                                <option value=''>{t('api-gateway:form.all')}</option>
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
	                    sortable={true}
	                    metaData={metaData}
	                    data={this.state.assemblyTypes}
	                    keyProperty="uuid"
	                    link={`/${localAuthoritySlug}/admin/convocation/type-assemblee/liste-type-assemblee/`}
	                    linkProperty='uuid'
	                    onHandleToggle={this.handleFieldCheckboxChange}
	                    lineThroughResolver={this.lineThroughResolver}
	                    pagination={pagination}
	                    sort={(clickedColumn) => sortTable(this, clickedColumn, this.loadData)}
	                    direction={this.state.direction}
	                    column={this.state.column}
	                    toggleButton={true}
	                    toogleHeader={t('convocation.admin.modules.convocation.assembly_type_config.status')}
	                    toogleProperty='active'
	                    noDataMessage={t('convocation.admin.modules.convocation.assembly_type_liste.no_assembly_type')}
	                />
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(AssemblyTypeList))