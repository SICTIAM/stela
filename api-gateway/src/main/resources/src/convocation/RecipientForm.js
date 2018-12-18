import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

import { Button, Modal, Tab } from 'semantic-ui-react'

import StelaTable from '../_components/StelaTable'

class RecipientForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	state = {
	    selectedUser: [],
	    users: [
	        { name: 'Julie ALBALADEJO', email: 'julie.albaladejo@avisto.com' },
	        { name: 'Gérald GOLE', email: 'gérald.gole@avisto.com' },
	        { name: 'Anne-Sophie LEVEQUES', email: 'anne-sophie.leveques@sictiam.com' }
	    ]
	}
	cancelAdd = () => {
	    this.setState({selectedUser: []})
	    this.props.onCloseModal()
	}
	onSelectedRow = (key, state) => {
	    let selectedUser = this.state.selectedUser.slice()
	    if(state) {
	        const users = this.state.users.slice()
	        let user = users.find((user) => {
	            return user.email === key
	        })
	        selectedUser.push(user)
	        this.setState({ selectedUser })
	    } else {
	        const indexUser = selectedUser.findIndex((user) => {
	            return user.email === key
	        })
	        selectedUser.splice(indexUser, 1)
	        this.setState({ selectedUser })
	    }
	}
	render() {
	    const { t } = this.context
	    const metaData = [
	        { property: 'name', displayed: true, searchable: true },
	        { property: 'email', displayed: true, searchable: true },
	    ]
	    const listContent =
			<StelaTable
			    header={false}
			    searchable={true}
			    sortable={false}
			    metaData={metaData}
			    data={this.state.users}
			    keyProperty="email"
			    select={true}
			    onSelectedRow={this.onSelectedRow}
			    noDataMessage={t('convocation.new.no_receive')}
			/>
	    const panes = [
	        { menuItem: t('convocation.new.choose_from_the_list'), render: () => <Tab.Pane>{listContent}</Tab.Pane> },
	    ]
	    return (
	        <Fragment>
	            <Modal.Header>{t('convocation.new.add_recipients')}</Modal.Header>
	            <Modal.Content>
	                <Tab panes={panes} />
	            </Modal.Content>
	            <Modal.Actions>
	                <div className='footerForm'>
	                    <Button color='red' basic style={{ marginRight: '1em' }} onClick={this.cancelAdd}>
	                        {t('api-gateway:form.cancel')}
	                    </Button>
	                    <Button primary basic onClick={() => this.props.onAdded(this.state.selectedUser)}>
	                        {t('api-gateway:form.add')}
	                    </Button>
	                </div>
	            </Modal.Actions>
	        </Fragment>
	    )
	}

}
export default translate(['convocation', 'api-gateway'])(RecipientForm)