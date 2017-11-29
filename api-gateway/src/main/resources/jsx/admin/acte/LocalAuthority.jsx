import React, { Component } from 'react'
import PropTypes from 'prop-types'
import renderIf from 'render-if'
import { translate } from 'react-i18next'
import { Checkbox, Form, Button, Segment, Popup } from 'semantic-ui-react'
import Validator from 'validatorjs'

import InputValidation from '../../_components/InputValidation'
import DraggablePosition from '../../_components/DraggablePosition'
import { errorNotification, localAuthorityUpdateSuccess } from '../../_components/Notifications'
import { Field } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling, handleFieldCheckboxChange } from '../../_util/utils'

class LocalAuthority extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        constantFields: {
            uuid: '',
            name: '',
            siren: '',
            nomenclatureDate: null
        },
        fields: {
            department: '',
            district: '',
            nature: '',
            canPublishRegistre: false,
            canPublishWebSite: false,
            stampPosition: {
                x: 10,
                y: 10
            }
        },
        localAuthorityFetched: false,
        isFormValid: false
    }
    validationRules = {
        department: 'required|digits:3',
        district: 'required|digits:1',
        nature: 'required|digits:2'
    }
    componentDidMount() {
        const uuid = this.props.uuid
        if (uuid !== '') {
            fetchWithAuthzHandling({ url: '/api/acte/localAuthority/' + uuid })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.updateState(json))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                    })
                    history.push('/admin/actes/parametrage-collectivite')
                })
        }
    }
    updateState = ({ uuid, name, siren, department, district, nature, nomenclatureDate, canPublishRegistre, canPublishWebSite, stampPosition }) => {
        const constantFields = { uuid, name, siren, nomenclatureDate }
        const fields = { department, district, nature, canPublishRegistre, canPublishWebSite, stampPosition }
        this.setState({ constantFields: constantFields, fields: fields, localAuthorityFetched: true }, this.validateForm)
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        this.setState({ fields: fields }, this.validateForm)
    }
    handleChangeDeltaPosition = (position) => {
        const { fields } = this.state
        fields.stampPosition = position
        this.setState({ fields })
    }
    validateForm = () => {
        const data = {
            department: this.state.fields.department,
            district: this.state.fields.district,
            nature: this.state.fields.nature
        }
        const validation = new Validator(data, this.validationRules)
        this.setState({ isFormValid: validation.passes() })
    }
    submitForm = (event) => {
        event.preventDefault()
        const data = JSON.stringify(this.state.fields)
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        fetchWithAuthzHandling({ url: '/api/acte/localAuthority/' + this.state.constantFields.uuid, method: 'PATCH', body: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                this.context._addNotification(localAuthorityUpdateSuccess(this.context.t))
                this.updateState(json)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    cancelSubmit = (event) => event.preventDefault()
    render() {
        const { t } = this.context
        const localAuthorityFetched = renderIf(this.state.localAuthorityFetched)
        const stampPosition = (
            <DraggablePosition
                label={t('acte.stamp_pad.pad_label')}
                height={300}
                width={190}
                labelColor='#000'
                position={this.state.fields.stampPosition}
                handleChange={this.handleChangeDeltaPosition} />
        )
        return (
            localAuthorityFetched(
                <Segment>
                    <h1>{this.state.constantFields.name}</h1>

                    <h2>{t('admin.modules.acte.local_authority_settings.general_informations')}</h2>

                    <Form onSubmit={this.submitForm}>
                        <Field htmlFor="uuid" label={t('api-gateway:local_authority.uuid')}>
                            <span id="uuid">{this.state.constantFields.uuid}</span>
                        </Field>
                        <Field htmlFor="siren" label={t('api-gateway:local_authority.siren')}>
                            <span id="siren">{this.state.constantFields.siren}</span>
                        </Field>
                        <Field htmlFor="nomenclatureDate" label={t('api-gateway:local_authority.nomenclatureDate')}>
                            <span id="nomenclatureDate">{this.state.constantFields.nomenclatureDate}</span>
                        </Field>
                        <Field htmlFor="department" label={t('api-gateway:local_authority.department')}>
                            <InputValidation id='department'
                                value={this.state.fields.department}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.department}
                                fieldName={t('api-gateway:local_authority.department')}
                                className='simpleInput' />
                        </Field>
                        <Field htmlFor="district" label={t('api-gateway:local_authority.district')}>
                            <InputValidation id='district'
                                value={this.state.fields.district}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.district}
                                fieldName={t('api-gateway:local_authority.district')}
                                className='simpleInput' />
                        </Field>
                        <Field htmlFor="nature" label={t('api-gateway:local_authority.nature')}>
                            <InputValidation id='nature'
                                value={this.state.fields.nature}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.nature}
                                fieldName={t('api-gateway:local_authority.nature')}
                                className='simpleInput' />
                        </Field>
                        <Field htmlFor="positionPad" label={t('acte.stamp_pad.title')}>
                            <Popup
                                trigger={<Button id={'positionPad'} content={t('acte.stamp_pad.choose_position')} onClick={this.cancelSubmit} />}
                                content={stampPosition} on='click' position='top right'
                            />
                        </Field>
                        <Field htmlFor="canPublishRegistre" label={t('api-gateway:local_authority.canPublishRegistre')}>
                            <Checkbox id="canPublishRegistre" toggle checked={this.state.fields.canPublishRegistre} onChange={e => handleFieldCheckboxChange(this, 'canPublishRegistre')} />
                        </Field>
                        <Field htmlFor='canPublishWebSite' label={t('api-gateway:local_authority.canPublishWebSite')}>
                            <Checkbox id="canPublishWebSite" toggle checked={this.state.fields.canPublishWebSite} onChange={e => handleFieldCheckboxChange(this, 'canPublishWebSite')} />
                        </Field>
                        <Button style={{ marginTop: '1em' }} disabled={!this.state.isFormValid} type='submit'>{t('api-gateway:form.update')}</Button>
                    </Form>
                </Segment>
            )
        )
    }
}

export default translate(['acte', 'api-gateway'])(LocalAuthority)