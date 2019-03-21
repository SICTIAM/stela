import React, { Component } from 'react'
import { translate } from 'react-i18next'
import debounce from 'debounce'
import PropTypes from 'prop-types'
import { Modal, Button, Form, Grid } from 'semantic-ui-react'
import accepts from 'attr-accept'
import Validator from 'validatorjs'

import { acceptFileDocumentConvocation } from '../../_util/constants'
import { notifications } from '../../_util/Notifications'
import ConvocationService from '../../_util/convocation-service'

import { withAuthContext } from '../../Auth'

import { FormField, File, InputFile, ValidationPopup } from '../../_components/UI'

class AddMinutesModale extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _addNotification: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func
	}
	static propTypes = {
	    uuid: PropTypes.string.isRequired,
	    minutes: PropTypes.object
	}
	static defaultProps = {
	    uuid: ''
	}
	state = {
	    fields: {
	        minutes: null
	    },
	    isFormValid: false,
	    allFormErrors: [],
	    modalOpen: false
	}

	validationRules = {
	    minutes: ['required']
	}

	componentDidMount() {
	    this._convocationService = new ConvocationService()
	    this.validateForm(null)
	}

	handleOpen = () => this.setState({ modalOpen: true })

  	handleClose = () => this.setState({ modalOpen: false })

	validateForm = debounce(() => {
	    const { t } = this.context
	    const data = {
	        minutes: this.state.fields.minutes
	    }

	    const attributeNames = {
	        minutes: t('convocation.fields.minutes')
	    }
	    const validationRules = this.validationRules
	    //add validation format file

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const allFormErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, allFormErrors })
	})

	handleFileChange = (field, file, acceptType) => {
	    if(this.acceptsFile(file, acceptType)) {
	        const fields = this.state.fields

	        if (file) {
	            fields[field] = file
	            this.setState({ fields }, this.validateForm)
	        }
	    }
	}

	acceptsFile = (file, acceptType) => {
	    const { _addNotification, t } = this.context
	    if(accepts(file, acceptType)) {
	        return true
	    } else {
	        _addNotification(notifications.defaultError, 'notifications.convocation.title', t('api-gateway:form.validation.bad_extension_file', {name: file.name}))
	        return false
	    }
	}

	deleteFile = (field) => {
	    const fields = this.state.fields
	    fields[field] = null
	    this.setState({ fields }, this.validateForm)
	}

	submit = async() => {
	    const { _addNotification } = this.context
	    const data = new FormData()
	    data.append('minutes', this.state.fields.minutes)
	    await this._convocationService.uploadMinutes(this.context, this.props.uuid, data)
	    this.handleClose()
	    if(this.props.minutes) {
	        _addNotification(notifications.convocation.minutes_updated)
	    } else {
	        _addNotification(notifications.convocation.minutes_sent)
	    }
	}
	render() {
	    const { t } = this.context

	    const submissionButton = <Button type='submit' primary basic disabled={!this.state.isFormValid}>
	            {t('api-gateway:form.send')}
	        </Button>
	    return (
	        <Modal open={this.state.modalOpen}
	            trigger={<Button primary compact type='button' basic className='ml-10' onClick={this.handleOpen}>{t('convocation.page.add_minutes')}</Button>}>
	            <Modal.Header>{t('convocation.page.add_minutes')}</Modal.Header>
	            <Modal.Content>
	                <Form onSubmit={this.submit}>
	                    <Grid>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.props.uuid}_minutes`}
	                                label={t('convocation.fields.minutes')}>
	                                <InputFile labelClassName="primary" htmlFor={`${this.props.uuid}_minutes`}
	                                    label={`${t('api-gateway:form.add_a_file')}`}>
	                                    <input type="file"
	                                        id={`${this.props.uuid}_minutes`}
	                                        accept={acceptFileDocumentConvocation}
	                                        onChange={(e) => this.handleFileChange('minutes', e.target.files[0], acceptFileDocumentConvocation)}
	                                        style={{ display: 'none' }}/>
	                                </InputFile>
	                            </FormField>
	                            {this.state.fields.minutes && (
	                            	<File attachment={{ filename: this.state.fields.minutes.name }} onDelete={() => this.deleteFile('minutes')} />
	                            )}
	                        </Grid.Column>
	                    </Grid>
	                    <div className='footerForm'>
	                        <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.handleClose()} basic color='red'>
	                            {t('api-gateway:form.cancel')}
	                        </Button>

	                        {this.state.allFormErrors.length > 0 &&
								<ValidationPopup errorList={this.state.allFormErrors}>
								    {submissionButton}
								</ValidationPopup>
	                        }
	                        {this.state.allFormErrors.length === 0 && submissionButton}
	                    </div>
	                </Form>
	            </Modal.Content>
	        </Modal>
	    )
	}
}
export default translate(['convocation', 'api-gateway'])(withAuthContext(AddMinutesModale))