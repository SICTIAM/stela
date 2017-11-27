import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Accordion, Icon, Segment, Grid, Button, Header, Form } from 'semantic-ui-react'
import moment from 'moment'
import Validator from 'validatorjs'
import debounce from 'debounce'

import history from '../_util/history'
import { errorNotification, acteSentSuccess, draftDeletedSuccess } from '../_components/Notifications'
import { FormField } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import NewActeForm from './NewActeForm'
import { natures } from '../_util/constants'

class NewActeBatchedForm extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        active: 0,
        fields: {
            uuid: '',
            actes: [],
            lastModified: null,
            decision: '',
            nature: ''
        },
        draftStatus: '',
        draftValid: false,
        statuses: {},
        formValid: {},
        isAllFormValid: false,
        shouldUnmount: true
    }
    validationRules = {
        nature: 'required',
        decision: ['required', 'date']
    }
    componentDidMount() {
        const url = this.props.uuid ? '/api/acte/drafts/' + this.props.uuid : '/api/acte/draft/batch'
        fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.loadDraft(json))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })
    }
    loadDraft = (draft) => {
        // Hacks to prevent affecting `null` values
        if (!draft.nature) draft.nature = ''
        if (!draft.decision) draft.decision = ''
        this.setState({ fields: draft }, this.validateForm)
    }
    getDraftData = () => {
        const draftData = Object.assign({}, this.state.fields)
        if (draftData['nature'] === '') draftData['nature'] = null
        if (draftData['decision'] === '') draftData['decision'] = null
        return draftData
    }
    addBatchedActe = () => {
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/newActe`, method: 'POST', context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                const { fields } = this.state
                fields.actes.push(json)
                this.setState({ fields })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })
    }
    deleteBatchedActe = (uuid) => {
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/${uuid}`, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                fields.actes = fields.actes.filter(acte => acte.uuid !== uuid)
                this.setState({ fields })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })
    }
    handleClick = (uuid) => {
        const newActive = this.state.active !== uuid ? uuid : 0
        this.setState({ active: newActive })
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        this.setState({ fields: fields, draftStatus: '' }, () => {
            this.updateStatus()
            this.validateForm()
            this.saveDraft()
        })
    }
    validateForm = debounce(() => {
        const data = {
            nature: this.state.fields.nature,
            decision: this.state.fields.decision,
        }
        const validation = new Validator(data, this.validationRules)
        const draftValid = validation.passes()
        this.setState({ draftValid }, this.updateAllFormValid)
    }, 500)
    saveDraft = debounce((callback) => {
        this.setState({ draftStatus: 'saving' }, this.updateStatus)
        const draftData = this.getDraftData()
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${draftData.uuid}`, body: JSON.stringify(draftData), headers: headers, method: 'PATCH', context: this.context })
            .then(checkStatus)
            .then(() => {
                this.setState({ draftStatus: 'saved' }, this.updateStatus)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }, 3000)
    setStatusForId = (statusValue, uuid) => {
        const { statuses } = this.state
        statuses[uuid] = statusValue
        this.setState({ statuses }, this.updateStatus)
    }
    updateStatus = () => {
        const statuses = Object.values(this.state.statuses)
        statuses.push(this.state.draftStatus)
        let newStatus = ''
        if (statuses.includes('')) newStatus = ''
        else if (statuses.includes('saving')) newStatus = 'saving'
        else if (statuses.includes('saved')) newStatus = 'saved'
        this.props.setStatus(newStatus)
    }
    setFormValidForId = (isFormValidValue, uuid) => {
        const { formValid } = this.state
        formValid[uuid] = isFormValidValue
        this.setState({ formValid }, this.updateAllFormValid)
    }
    updateAllFormValid = () => {
        let isAllFormValid = true
        const formValid = Object.values(this.state.formValid)
        formValid.push(this.state.draftValid)
        formValid.map(bool => isAllFormValid = isAllFormValid && bool)
        this.setState({ isAllFormValid })
    }
    setObjet = (uuid, newObjet) => {
        const { fields } = this.state
        const acte = fields.actes.find(acte => acte.uuid === uuid)
        acte.objet = newObjet
        this.setState({ fields })
    }
    submitDraft = () => {
        const { fields } = this.state
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.uuid}`, method: 'POST', context: this.context })
            .then(checkStatus)
            .then(response => response.text())
            .then(acteUuid => {
                this.context._addNotification(acteSentSuccess(this.context.t))
                history.push('/actes')
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    initDelete = () => this.setState({ shouldUnmount: false }, this.deleteDraft)
    deleteDraft = () => {
        const { fields } = this.state
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.uuid}`, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(response => response.text())
            .then(acteUuid => {
                this.context._addNotification(draftDeletedSuccess(this.context.t))
                history.push('/actes')
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const isFormSaving = this.props.status === 'saving'
        const draftUuid = this.state.fields.uuid ? this.state.fields.uuid : this.props.uuid
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const wrappedActes = this.state.fields.actes.map(acte =>
            <WrappedActeForm
                key={acte.uuid}
                acte={acte}
                isActive={this.state.active === acte.uuid}
                handleClick={this.handleClick}
                deleteBatchedActe={this.deleteBatchedActe}>
                <NewActeForm
                    uuid={acte.uuid}
                    draftUuid={draftUuid}
                    mode='ACTE_BATCH'
                    nature={this.state.fields.nature}
                    decision={this.state.fields.decision}
                    active={this.state.active}
                    setStatus={this.setStatusForId}
                    status={this.state.status}
                    setFormValidForId={this.setFormValidForId}
                    setObjet={this.setObjet}
                    shouldUnmount={this.shouldUnmount}
                />
            </WrappedActeForm>
        )
        return (
            <div>
                <Segment>
                    <Header size='medium'>{t('acte.drafts.common_fields')}</Header>
                    <Form>
                        <FormField htmlFor={'decision'} label={t('acte.fields.decision')}>
                            <InputValidation id={'decision'}
                                type='date'
                                placeholder='aaaa-mm-jj'
                                value={this.state.fields.decision}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.decision}
                                fieldName={t('acte.fields.decision')}
                                max={moment().format('YYYY-MM-DD')} />
                        </FormField>
                        <FormField htmlFor={'nature'} label={t('acte.fields.nature')}>
                            <InputValidation id={'nature'}
                                type='select'
                                value={this.state.fields.nature}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.nature}
                                fieldName={t('acte.fields.nature')}>
                                <option value='' disabled>{t('acte.new.choose')}</option>
                                {natureOptions}
                            </InputValidation>
                        </FormField>
                    </Form>
                </Segment>
                <Accordion>
                    {wrappedActes}
                    <Button onClick={this.addBatchedActe} style={{ marginBottom: '1em' }} basic fluid>{t('acte.drafts.add_an_acte')}</Button>
                </Accordion>
                <Button onClick={this.submitDraft} disabled={!this.state.isAllFormValid || isFormSaving} loading={isFormSaving}>{t('api-gateway:form.submit')}</Button>
                {renderIf(this.state.fields.uuid)(
                    <Button style={{ marginLeft: '1em' }} onClick={this.initDelete} compact basic color='red' disabled={isFormSaving} loading={isFormSaving}>
                        {t('api-gateway:form.delete_draft')}
                    </Button>
                )}
            </div>
        )
    }
}

const WrappedActeForm = ({ children, isActive, handleClick, acte, deleteBatchedActe }) =>
    <Segment style={{ marginBottom: '1em', paddingBottom: '1em' }}>
        <Accordion.Title active={isActive} >
            <Grid>
                <Grid.Column style={{ display: 'flex', alignItems: 'center' }} width={15} onClick={() => handleClick(acte.uuid)}>
                    <Header size='small'><Icon name='dropdown' /> {acte.objet}</Header>
                </Grid.Column>
                <Grid.Column width={1} style={{ textAlign: 'right' }}>
                    <Button color='red' icon onClick={() => deleteBatchedActe(acte.uuid)}>
                        <Icon name='remove' />
                    </Button>
                </Grid.Column>
            </Grid>
        </Accordion.Title>
        <Accordion.Content active={isActive}>
            {children}
        </Accordion.Content>
    </Segment>

export default translate(['acte', 'api-gateway'])(NewActeBatchedForm)