import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, TextArea, Button } from 'semantic-ui-react'
import Validator from 'validatorjs'
import debounce from 'debounce'

import { Page, FormField, File } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import { notifications } from '../_util/Notifications'
import history from '../_util/history'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'

class NewPes extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        fields: {
            objet: '',
            comment: '',
        },
        attachment: null,
        isFormValid: false
    }
    validationRules = {
        objet: 'required|max:500',
        comment: 'max:250',
        attachment: 'required'
    }
    handleFieldChange = (field, value) => {
        if (field === 'comment' && value.length > 250) return
        const { fields } = this.state
        fields[field] = value
        this.setState({ fields: fields }, this.validateForm)
    }
    handleFileChange = (file) => {
        if (file) this.setState({ attachment: file }, this.validateForm)
    }
    deleteFile = () => this.setState({ attachment: null }, this.validateForm)
    validateForm = debounce(() => {
        const data = {
            objet: this.state.fields.objet,
            comment: this.state.fields.comment,
            attachment: this.state.attachment
        }
        const validation = new Validator(data, this.validationRules)
        const isFormValid = validation.passes()
        this.setState({ isFormValid })
    }, 500)
    submit = () => {
        if (this.state.isFormValid) {
            const data = new FormData()
            data.append('pesAller', JSON.stringify(this.state.fields))
            data.append('file', this.state.attachment)
            fetchWithAuthzHandling({ url: '/api/pes', method: 'POST', body: data, context: this.context })
                .then(checkStatus)
                .then(response => response.text())
                .then(pesUuid => {
                    this.context._addNotification(notifications.pes.sent)
                    history.push('/pes/' + pesUuid)
                })
                .catch(response => {
                    response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
                })
        }
    }
    render() {
        const { t } = this.context
        return (
            <Page title={t('pes.new.title')}>
                <Segment>
                    <Form onSubmit={this.submit}>
                        <FormField htmlFor='objet' label={t('pes.fields.objet')}>
                            <InputValidation id='objet'
                                placeholder={t('pes.fields.objet') + '...'}
                                value={this.state.fields.objet}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.objet}
                                fieldName={t('pes.fields.objet')} />
                        </FormField>
                        <FormField htmlFor='attachment' label={t('pes.fields.attachment')}>
                            <InputValidation id='attachment'
                                type='file'
                                accept='.xml'
                                onChange={this.handleFileChange}
                                value={this.state.fields.attachment}
                                validationRule={this.validationRules.attachment}
                                label={t('api-gateway:form.add_a_file')}
                                fieldName={t('pes.fields.attachment')} />
                        </FormField>
                        {this.state.attachment &&
                            <File attachment={{ filename: this.state.attachment.name }} onDelete={this.deleteFile} />
                        }
                        <FormField htmlFor='comment' label={t('pes.fields.comment')}>
                            <TextArea id='comment'
                                style={{ minHeight: '3em' }}
                                placeholder={t('pes.fields.comment') + '...'}
                                value={this.state.fields.comment}
                                onChange={e => this.handleFieldChange('comment', e.target.value)} />
                            <p style={{ fontStyle: 'italic' }}>{t('api-gateway:form.max_string_length', { length: 250, remaining: 250 - (this.state.fields.comment ? this.state.fields.comment.length : 0) })}</p>
                        </FormField>
                        <div style={{ textAlign: 'right' }}>
                            <Button type='submit' primary basic disabled={!this.state.isFormValid}>{t('api-gateway:form.submit')}</Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(NewPes)