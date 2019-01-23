import React, { Component } from 'react'
import { translate } from 'react-i18next'
import { Segment } from 'semantic-ui-react'
import PropTypes from 'prop-types'

import StelaTable from '../_components/StelaTable'
//import Breadcrumb from '../_components/Breadcrumb'
import Pagination from '../_components/Pagination'
import { Page } from '../_components/UI'

class SentConvocation extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	}
	state = {
	    sentConvocation: [],
	    search: {
	        multifield: '',
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
	    // const { _fetchWithAuthzHandling } = this.context
	    // const data = this.getSearchData()

	    /* TEMP */
	    const results = [{
	        uuid: 1,
	        date: '10/12/2019',
	        type: 'assemblée 1',
	        object: 'BLABLA'
	    },{
	        uuid: 2,
	        date: '13/01/2020',
	        type: 'assemblée 1',
	        object: 'Discuter'
	    }]
	    const totalCount = 2
	    this.setState({sentConvocation: results, totalCount})
	    // _fetchWithAuthzHandling({ url: '/api/convocation/assembly-type', query: data, method: 'GET' })
	    //     .then(response => response.json())
	    //     .then((response) => this.setState({receivedConvocation: response.results, totalCount: response.totalCount}))
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
	/** Sort function */
	sort = (clickedColumn) => {
	    const { column, direction } = this.state
	    if (column !== clickedColumn) {
	        this.setState({ column: clickedColumn, direction: 'ASC' }, () => this.loadData())
	        return
	    }
	    this.setState({ direction: direction === 'ASC' ? 'DESC' : 'ASC' }, () => this.loadData())
	}

	render() {
	    const { t } = this.context
	    // const { search } = this.state

	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'date', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.date')},
	        { property: 'type', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.assembly_type')},
	        { property: 'object', displayed: true, searchable: true, sortable: true, displayName: t('convocation.fields.object')},

	        // { property: 'reminder', displayed: true, searchable: false, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.reminder'), displayComponent: checkboxDisplay },
	        // { property: 'useProcuration', displayed: true, searchable: false, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.procuration'), displayComponent: checkboxDisplay },
	        // { property: 'recipients', displayed: true, searchable: false, sortable: false, displayName: t('convocation.admin.modules.convocation.assembly_type_config.recipients'), displayComponent: recipientsDisplay },
	        // { property: 'active', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.assembly_type_config.status'), displayComponent: statusDisplay }
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

	    return (
	        <Page>
	            <Segment>
	                <StelaTable
	                    header={true}
	                    search={false}
	                    sortable={true}
	                    metaData={metaData}
	                    data={this.state.sentConvocation}
	                    keyProperty="uuid"
	                    striped={false}
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
export default translate(['convocation', 'api-gateway'])(SentConvocation)