import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Modal, Tab } from 'semantic-ui-react'

import ConvocationService from '../_util/convocation-service'

import StelaTable from '../_components/StelaTable'
import UserFormFragment from './_components/UserFormFragment'

class RecipientForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	static propTypes = {
	    selectedUser: PropTypes.array,
	    uuid: PropTypes.string,
	    recipient: PropTypes.bool,
	    userToDisabled: PropTypes.array,
	    canCreateUser: PropTypes.bool
	}
	static defaultProps = {
	    selectedUser: [],
	    uuid: null,
	    recipient: true,
	    userToDisabled: [],
	    canCreateUser: false
	}
	state = {
	    selectedUser: [],
	    users: []
	}
	componentDidMount = async() => {
	    this._convocationService = new ConvocationService()
	    this.setState({selectedUser: this.props.selectedUser})
	    await this.fetchRecipients()
	}
	fetchRecipients = async() => {
	    let recipientsResponse = await this._convocationService.getRecipients(this.context, this.props.uuid)
	    if(this.props.userToDisabled.length > 0) {
	        recipientsResponse = recipientsResponse.filter((user) => {
	            return !this.props.userToDisabled.some(userDisabled => {
				    return userDisabled.uuid === user.uuid
	            })
	        })
	    }
	    this.setState({users: recipientsResponse})
	}
	onSubmit = async (newUser) => {
	    await this.fetchRecipients()
	    this.onSelectedRow(newUser.uuid, true)
	}

	cancelAdd = () => {
	    this.setState({selectedUser: this.props.selectedUser})
	    this.props.onCloseModal()
	}
	onSelectedRow = (key, state) => {
	    let selectedUser = this.state.selectedUser.slice()
	    if(key !== 'all') {
	        if(state) {
	            const users = this.state.users.slice()
	            let user = users.find((user) => {
	                return user.uuid === key
	            })
	            selectedUser.push(user)
	            this.setState({ selectedUser })
	        } else {
	            const indexUser = selectedUser.findIndex((user) => {
	                return user.uuid === key
	            })
	            selectedUser.splice(indexUser, 1)
	            this.setState({ selectedUser })
	        }
	    } else {
	        state ? this.setState({ selectedUser: this.state.users}) : this.setState({ selectedUser: []})
	    }

	}
	render() {
	    const { t } = this.context
	    const { selectedUser, users } = this.state
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'firstname', displayed: true, searchable: true },
	        { property: 'lastname', displayed: true, searchable: true },
	        { property: 'email', displayed: true, searchable: true },
	    ]
	    const listContent =
			<StelaTable
			    containerTable='maxh-300 w-100'
			    header={true}
			    searchable={true}
			    sortable={false}
			    metaData={metaData}
			    data={users}
			    selectedRow={selectedUser}
			    keyProperty="uuid"
			    select={true}
			    onSelectedRow={this.onSelectedRow}
			    noDataMessage={t('convocation.new.no_recipient')}
			/>
	    const panes = [
	        { menuItem: t('convocation.new.choose_from_the_list'), render: () => <Tab.Pane>{listContent}</Tab.Pane> },
	        { menuItem: t('convocation.new.add_new_recipients'), render: () => <Tab.Pane>{<UserFormFragment preventParentSubmit={true} onSubmit={this.onSubmit}/>}</Tab.Pane> }
	    ]
	    return (
	        <Fragment>
	            <Modal.Header>
	                {this.props.recipient && t('convocation.new.add_recipients')}
	                {!this.props.recipient && t('convocation.new.edit_guest')}
	            </Modal.Header>
	            <Modal.Content>
	                {!this.props.canCreateUser && (
	                    <Fragment>
	                        {listContent}
	                    </Fragment>
	                )}
	                {this.props.canCreateUser && (
	                    <Tab panes={panes} />
	                )}
	            </Modal.Content>
	            <Modal.Actions>
	                <div className='footerForm'>
	                    <Button color='red' basic style={{ marginRight: '1em' }} onClick={this.cancelAdd}>
	                        {t('api-gateway:form.cancel')}
	                    </Button>
	                    <Button primary basic onClick={() => this.props.onAdded(this.state.selectedUser)}>
	                        {t('api-gateway:form.confirm')}
	                    </Button>
	                </div>
	            </Modal.Actions>
	        </Fragment>
	    )
	}

}
export default translate(['convocation', 'api-gateway'])(RecipientForm)