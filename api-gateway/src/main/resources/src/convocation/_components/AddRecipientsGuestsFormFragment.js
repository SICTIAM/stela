import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Grid, Button, Modal, Segment } from 'semantic-ui-react'

import { withAuthContext } from '../../Auth'

import { FormField } from '../../_components/UI'
import StelaTable from '../../_components/StelaTable'

import RecipientForm from '../RecipientForm'

class AddRecipientdGuestsFormFragment extends Component {
	static contextTypes = {
	    t: PropTypes.func
	}
	static propTypes = {
	    fields: PropTypes.object.isRequired,
	    updateUser: PropTypes.func.isRequired,
	    disabledRecipientsEdit: PropTypes.bool,
	    epci: PropTypes.bool
	}
    static defaultProps = {
        fields: {},
        disabledRecipientsEdit: false,
        epci: false
    }
	state = {
	    modalRecipentsOpened: false,
	    modalGuestsOpened: false
	}

	addUsers = (selectedUser, field) => {
	    const { fields } = this.props
	    fields[field] = selectedUser
	    this.props.updateUser(fields)
	    this.closeModal()
	}
	deleteUsers = (field) => {
	    const { fields } = this.props
	    fields[field] = []
	    this.props.updateUser(fields)
	}

	closeModal = () => {
	    this.setState({modalGuestsOpened: false, modalRecipentsOpened: false})
	}

	render() {
	    const { t } = this.context
	    const { fields, userToDisabled, authContext, disabledRecipientsEdit, epci } = this.props
	    const { modalRecipentsOpened, modalGuestsOpened } = this.state
	    const guestsToDisabled = userToDisabled ? fields.recipients.slice().concat(userToDisabled.recipients, userToDisabled.guests) : fields.recipients.slice()
	    const canCreateUser = authContext.userRights && authContext.userRights.indexOf('CONVOCATION_ADMIN') !== -1
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'lastname', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.lastname') },
	        { property: 'firstname', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.firstname') },
	        { property: 'email', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.email') },
	    ]

	    if(epci) metaData.push({property: 'epciName', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.epci')})
	    return (
	        <Fragment>
	            <Segment>
	                <Grid>
	                    <Grid.Column mobile='16' computer='16'>
	                        <Grid>
	                            <Grid.Column computer='16'>
	                                <FormField htmlFor={`${fields.uuid}_recipient`}
	                                    label={t('convocation.fields.recipient')}>
	                                    <Grid>
	                                        <Grid.Column computer='8'>
	                                            <Modal open={modalRecipentsOpened} trigger={<Button
	                                                onClick={() => this.setState({modalRecipentsOpened: true})}
	                                                type='button'
	                                                disabled={disabledRecipientsEdit}
	                                                id={`${fields.uuid}_recipient`}
	                                                compact basic primary>{t('convocation.new.add_recipients')}
	                                            </Button>}>
	                                                <RecipientForm
	                                                    onCloseModal={this.closeModal}
	                                                    epci={epci}
	                                                    onAdded={(selectedUser) => this.addUsers(selectedUser, 'recipients')}
	                                                    selectedUser={fields.recipients}
	                                                    canCreateUser={canCreateUser}
	                                                    userToDisabled = { userToDisabled && userToDisabled.recipients.concat(userToDisabled.guests) }
	                                                    uuid={fields.assemblyType && fields.assemblyType.uuid}>
	                                                </RecipientForm>
	                                            </Modal>
	                                        </Grid.Column>
	                                        <Grid.Column computer='8'>
	                                            <Button
	                                                type='button'
	                                                id={`${fields.uuid}_deleteRecipient`}
	                                                onClick={() => this.deleteUsers('recipients')}
	                                                compact basic color='red'>{t('convocation.new.delete_all_recipients')}
	                                            </Button>
	                                        </Grid.Column>
	                                    </Grid>
	                                </FormField>
	                                <StelaTable
	                                    header={false}
	                                    click={false}
	                                    data={fields.recipients}
	                                    keyProperty="uuid"
	                                    search={true}
	                                    noDataMessage={t('convocation.new.no_recipient')}
	                                    metaData={metaData}/>
	                            </Grid.Column>
	                        </Grid>
	                    </Grid.Column>
	                </Grid>
	            </Segment>
	            <Segment>
	                <Grid>
	                    <Grid.Column mobile='16' computer='16'>
	                        <Grid>
	                            <Grid.Column computer='16'>
	                                <FormField htmlFor={`${fields.uuid}_guest`}
	                                    label={t('convocation.fields.guest')}>
	                                    <Grid>
	                                        <Grid.Column computer='8'>
	                                            <Modal open={modalGuestsOpened} trigger={<Button
	                                                onClick={() => this.setState({modalGuestsOpened: true})}
	                                                type='button'
	                                                id={`${fields.uuid}_guest`}
	                                                compact basic primary>{t('convocation.new.edit_guest')}
	                                            </Button>}>
	                                                <RecipientForm
	                                                    onCloseModal={this.closeModal}
	                                                    onAdded={(selectedUser) => this.addUsers(selectedUser, 'guests')}
	                                                    selectedUser={fields.guests}
	                                                    epci={epci}
	                                                    userToDisabled={guestsToDisabled}
	                                                    recipient={false}
	                                                    canCreateUser={canCreateUser}
	                                                    uuid={fields.assemblyType && fields.assemblyType.uuid}>
	                                                </RecipientForm>
	                                            </Modal>
	                                        </Grid.Column>
	                                        <Grid.Column computer='8'>
	                                            <Button
	                                                type='button'
	                                                id={`${fields.uuid}_deleteGuest`}
	                                                onClick={() => this.deleteUsers('guests')}
	                                                compact basic color='red'>{t('convocation.new.delete_all_guests')}
	                                            </Button>
	                                        </Grid.Column>
	                                    </Grid>
	                                </FormField>
	                                <StelaTable
	                                    header={false}
	                                    click={false}
	                                    data={fields.guests}
	                                    keyProperty="uuid"
	                                    search={true}
	                                    noDataMessage={t('convocation.new.no_recipient')}
	                                    metaData={metaData}/>
	                            </Grid.Column>
	                        </Grid>
	                    </Grid.Column>
	                </Grid>
	            </Segment>
	        </Fragment>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(AddRecipientdGuestsFormFragment))