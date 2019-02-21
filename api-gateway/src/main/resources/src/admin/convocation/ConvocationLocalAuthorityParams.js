import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button, Grid, Radio } from 'semantic-ui-react'
import Validator from 'validatorjs'

import { withAuthContext } from '../../Auth'

import { checkStatus, getLocalAuthoritySlug } from '../../_util/utils'
import { notifications } from '../../_util/Notifications'

import {  Page, FormField, ValidationPopup } from '../../_components/UI'
import Breadcrumb from '../../_components/Breadcrumb'
import DraggablePosition from '../../_components/DraggablePosition'

class ConvocationLocalAuthorityParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    validationRules = {
	    residentThreshold: 'required'
    }
    state = {
        fields: {
            uuid: '',
            residentThreshold: null,
            stampPosition: {
                x: 10,
                y: 10
            },
        },
        isFormValid: false,
        formErrors: []
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({url: '/api/convocation/local-authority'})
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                const fields = this.state.fields
                fields.uuid = json.uuid
                fields.residentThreshold = json.residentThreshold
                fields.stampPosition = json.stampPosition
                this.setState({fields})
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.title', json.message)
                })
            })
    }
    validateForm = () => {
	    const { t } = this.context
	    const data = {
	        residentThreshold: this.state.fields.residentThreshold
	    }
	    const attributeNames = {
	        residentThreshold: t('api-gateway:admin.convocation.fields.residents_threshold')
	    }
	    const validationRules = this.validationRules

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const formErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, formErrors })
    }

    handleChangeRadio = (e, value, field) => {
        const fields = this.state.fields
        fields[field] = value === 'true'
        this.setState({fields}, this.validateForm())
    }
	handleChangeDeltaPosition = (position) => {
	    const { fields } = this.state
	    fields.stampPosition = position
	    this.setState({ fields }, this.validateForm())
	}

    submitForm = (e) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        e.preventDefault()
        const headers = { 'Content-Type': 'application/json' }
        _fetchWithAuthzHandling({url: '/api/convocation/local-authority', method: 'PUT', headers: headers, body: JSON.stringify(this.state.fields), context: this.props.authContext})
            .then(checkStatus)
            .then(() => {
                _addNotification(notifications.admin.moduleConvocationUpdated)
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.title', json.message)
                })
            })
    }
    render() {
        const { t } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        const submissionButton =
			<Button type='submit' primary basic disabled={!this.state.isFormValid}>
			    {t('api-gateway:form.update')}
			</Button>
        return (
            <Page>
                <Breadcrumb
	                data={[
	                    {title: t('api-gateway:breadcrumb.admin_home'), url: `/${localAuthoritySlug}/admin/ma-collectivite`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation')}
	                ]}
	            />
                <Segment>
                    <Form onSubmit={this.submitForm}>
                        <Grid>
                            <Grid.Column mobile="16" computer='8'>
                                <FormField htmlFor='residentThreshold'
                                    label={t('api-gateway:admin.convocation.fields.residents_threshold')}>
                                    <Radio
                                        label={t('api-gateway:yes')}
                                        value='true'
                                        name='residentThreshold'
                                        checked={this.state.fields.residentThreshold === true}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, value, 'residentThreshold')}
                                    ></Radio>
                                    <Radio
                                        label={t('api-gateway:no')}
                                        name='residentThreshold'
                                        value='false'
                                        checked={this.state.fields.residentThreshold === false}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, value, 'residentThreshold')}
                                    ></Radio>
                                </FormField>
                            </Grid.Column>
                            <Grid.Column mobile="16" computer='16'>
                                <FormField htmlFor="positionPad" label={t('api-gateway:stamp_pad.title')}>
                                    <Grid>
                                        <Grid.Row style={{display:'flex', justifyContent:'space-around', alignItems:'center'}}>
                                            <DraggablePosition
                                                label={t('convocation.stamp_pad.pad_label')}
                                                height={300}
                                                width={190}
                                                showPercents={true}
                                                labelColor='#000'
                                                position={this.state.fields.stampPosition}
                                                handleChange={this.handleChangeDeltaPosition} />
                                            <DraggablePosition
                                                label={t('convocation.stamp_pad.pad_label')}
                                                height={190}
                                                width={300}
                                                boxHeight={70}
                                                boxWidth={25}
                                                showPercents={true}
                                                labelColor='#000'
                                                position={{x:this.state.fields.stampPosition.y, y: this.state.fields.stampPosition.x}}
                                                handleChange={() =>  console.error('Not available to proceed changes')}
                                                disabled={true}
                                            />
                                        </Grid.Row>
                                    </Grid>
                                </FormField>
                            </Grid.Column>
                        </Grid>
                        <div className='footerForm'>
                            {this.state.formErrors.length > 0 &&
								<ValidationPopup errorList={this.state.formErrors}>
								    {submissionButton}
								</ValidationPopup>
	                        }
	                        {this.state.formErrors.length === 0 && submissionButton}
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(ConvocationLocalAuthorityParams))