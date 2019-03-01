import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Modal, /*Tab, Form, Grid*/ } from 'semantic-ui-react'

import { checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'

// import { FormField } from '../_components/UI'
// import InputValidation from '../_components/InputValidation'
import StelaTable from '../_components/StelaTable'

class RecipientForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	static defaultProps = {
	    selectedUser: [],
	    uuid: null,
	    recipient: true,
	    userToDisabled: []
	}
	state = {
	    selectedUser: [],
	    users: [],
	    // newUser: {
	    //     email: '',
	    //     firstName: '',
	    //     lastName: ''
	    // }
	}
	componentDidMount() {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    if(!this.props.uuid && this.props.recipient) {
	        _fetchWithAuthzHandling({ url: '/api/convocation/local-authority/recipients'})
	        .then(checkStatus)
	        .then(response => response.json())
	        .then(json => {
	                let users = json
	                if(this.props.userToDisabled.length > 0) {
	                    users = json.filter((user) => {
	                        return !this.props.userToDisabled.some(userDisabled => {
	                            return userDisabled.uuid === user.uuid
	                        })
	                    })
	                }
	            this.setState({users: users})
	        })
	    } else if(this.props.uuid && this.props.recipient){
	        _fetchWithAuthzHandling({url: `/api/convocation/assembly-type/${this.props.uuid}/recipients`, method: 'GET'})
	            .then(checkStatus)
	            .then(response => response.json())
	            .then(json => this.setState({users: json}))
	            .catch(response => {
	                response.json().then(json => {
	                    _addNotification(notifications.defaultError, 'notifications.title', json.message)
	                })
	            })
	    } else {
	        // _fetchWithAuthzHandling({ url: '/api/convocation/local-authority/recipients'})
	        // .then(checkStatus)
	        // .then(response => response.json())
	        // .then(json => {
	        //     this.setState({users: json})
	        // })
	        // .catch(response => {
	        //     response.json().then(json => {
	        //         _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        //     })
	        // })
	    }
	    this.setState({selectedUser: this.props.selectedUser})
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
			    data={this.state.users}
			    selectedRow={this.props.selectedUser}
			    keyProperty="uuid"
			    select={true}
			    onSelectedRow={this.onSelectedRow}
			    noDataMessage={t('convocation.new.no_recipient')}
			/>
	    // const emailForm = <Form>
	    //     <Grid>
	    //         <Grid.Column mobile='16' computer='8'>
	    //             <FormField htmlFor='recipient_firstname'
	    //                 label={t('convocation.admin.modules.convocation.recipient_config.firstname')} required={true}>
	    //                 <InputValidation
	    //                     id='recipient_firstname'
	    //                     onChange={this.handleFieldChange}
	    //                     value={this.state.newUser.firstName}
	    //                 />
	    //             </FormField>
	    //         </Grid.Column>
	    //         <Grid.Column mobile='16' computer='8'>
	    //             <FormField htmlFor='recipient_lastname'
	    //                 label={t('convocation.admin.modules.convocation.recipient_config.lastname')} required={true}>
	    //                 <InputValidation
	    //                     id='lastname'
	    //                     onChange={this.handleFieldChange}
	    //                     value={this.state.newUser.lastName}
	    //                 />
	    //             </FormField>
	    //         </Grid.Column>
	    //         <Grid.Column mobile='16' computer='8'>
	    //             <FormField htmlFor='recipient_email'
	    //                 label={t('convocation.admin.modules.convocation.recipient_config.email')} required={true}>
	    //                 <InputValidation
	    //                     id='recipient_email'
	    //                     onChange={this.handleFieldChange}
	    //                     value={this.state.newUser.email}
	    //                 />
	    //             </FormField>
	    //         </Grid.Column>
	    //     </Grid>
	    // </Form>
	    // const panes = [
	    //     { menuItem: t('convocation.new.choose_from_the_list'), render: () => <Tab.Pane>{listContent}</Tab.Pane> },
	    //     { menuItem: t('convocation.new.free_email'), render: () => <Tab.Pane>{emailForm}</Tab.Pane> }
	    // ]
	    return (
	        <Fragment>
	            <Modal.Header>
	                {this.props.recipient && t('convocation.new.add_recipients')}
	                {/* {!this.props.recipient && t('convocation.new.edit_guest')} */}
	            </Modal.Header>
	            <Modal.Content>
	                {this.props.recipient && (
	                    <Fragment>
	                        {listContent}
	                    </Fragment>
	                )}
	                {/* {!this.props.recipient && (
	                    <Tab panes={panes} />
	                )} */}
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