import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Grid, Button, Modal } from 'semantic-ui-react'

import { withAuthContext } from '../../Auth'

import { FormField } from '../../_components/UI'
import ChipsList from '../../_components/ChipsList'

import RecipientForm from '../RecipientForm'

class AddRecipientdGuestsFormFragment extends Component {
	static contextTypes = {
	    t: PropTypes.func
	}
	static propTypes = {
	    fields: PropTypes.object.isRequired,
	    updateUser: PropTypes.func.isRequired,
	    disabledRecipientsEdit: PropTypes.bool
	}
    static defaultProps = {
        fields: {},
        disabledRecipientsEdit: false
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
	    const { fields, userToDisabled } = this.props
	    const guestsToDisabled = userToDisabled ? fields.recipients.slice().concat(userToDisabled.recipients, userToDisabled.guests) : fields.recipients.slice()

	    return (
	        <Fragment>
	            <Grid.Column mobile='16' computer='16'>
	                <Grid>
	                    <Grid.Column computer='16'>
	                        <FormField htmlFor={`${fields.uuid}_recipient`}
	                            label={t('convocation.fields.recipient')}>
	                            <Grid>
	                                <Grid.Column computer='8'>
	                                    <Modal open={this.state.modalRecipentsOpened} trigger={<Button
	                                        onClick={() => this.setState({modalRecipentsOpened: true})}
	                                        type='button'
	                                        disabled={this.props.disabledRecipientsEdit}
	                                        id={`${fields.uuid}_recipient`}
	                                        compact basic primary>{t('convocation.new.add_recipients')}
	                                    </Button>}>
	                                        <RecipientForm
	                                            onCloseModal={this.closeModal}
	                                            onAdded={(selectedUser) => this.addUsers(selectedUser, 'recipients')}
	                                            selectedUser={fields.recipients}
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
	                        <ChipsList
	                            list={fields.recipients}
	                            labelText='email'
	                            removable={false}
	                            viewMoreText={t('convocation.new.view_more_recipients', {number: fields.recipients.length})}
	                            viewLessText={t('convocation.new.view_less_recipients')}/>
	                    </Grid.Column>
	                </Grid>
	            </Grid.Column>

	            <Grid.Column mobile='16' computer='16'>
	                <Grid>
	                    <Grid.Column computer='16'>
	                        <FormField htmlFor={`${fields.uuid}_guest`}
	                            label={t('convocation.fields.guest')}>
	                            <Grid>
	                                <Grid.Column computer='8'>
	                                    <Modal open={this.state.modalGuestsOpened} trigger={<Button
	                                        onClick={() => this.setState({modalGuestsOpened: true})}
	                                        type='button'
	                                        id={`${fields.uuid}_guest`}
	                                        compact basic primary>{t('convocation.new.edit_guest')}
	                                    </Button>}>
	                                        <RecipientForm
	                                            onCloseModal={this.closeModal}
	                                            onAdded={(selectedUser) => this.addUsers(selectedUser, 'guests')}
	                                            selectedUser={fields.guests}
	                                            userToDisabled={guestsToDisabled}
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
	                        <ChipsList
	                            list={fields.guests}
	                            labelText='email'
	                            removable={false}
	                            viewMoreText={t('convocation.new.view_more_guests', {number: fields.guests.length})}
	                            viewLessText={t('convocation.new.view_less_guests')}/>
	                    </Grid.Column>
	                </Grid>
	            </Grid.Column>
	        </Fragment>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(AddRecipientdGuestsFormFragment))