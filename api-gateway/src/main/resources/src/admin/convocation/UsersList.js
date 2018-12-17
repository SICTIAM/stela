import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import AdvancedSearch from '../../_components/AdvancedSearch'
import { Page } from '../../_components/UI'

class UsersList extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	state = {
	    users:[
	        {firstName: 'Anne-Sophie', name:'LEVEQUES'},
	        {firstName: 'Julie', name:'ALBALADEJO'}
	    ],
	    search: {
	        multifield: '',
	        name: ''
	    },
	    limit: 25,
	    offset: 0,
	    currentPage: 0,
	}
	render() {
	    const { t } = this.context
	    const { search } = this.state
	    const metaData = [
	        { property: 'firstName', displayed: true, searchable: true,  displayName: 'Prénom' },
	        { property: 'name', displayed: true, searchable: true,  displayName: 'Nom' }
	    ]
	    return (
	        <Page>
	            <Segment>
	                <AdvancedSearch
	                    isDefaultOpen={false}
	                    fieldId='multifield'
	                    fieldValue={search.multifield}>
	                </AdvancedSearch>
	                <StelaTable
	                    header={true}
	                    select={true}
	                    search={false}
	                    sortable={true}
	                    metaData={metaData}
	                    data={this.state.users}
	                    keyProperty="name"
	                    noDataMessage={t('convocation.admin.assembly_type_liste.no_assembly_type')}
	                />
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(UsersList)