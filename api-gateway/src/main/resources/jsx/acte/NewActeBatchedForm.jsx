import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Accordion, Icon, Segment, Grid, Button, Header, Form } from 'semantic-ui-react'
import moment from 'moment'
import Validator from 'validatorjs'
import debounce from 'debounce'

import history from '../_util/history'
import { notifications } from '../_util/Notifications'
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
        attachmentTypes: [],
        draftStatus: null,
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
            .then(json =>
                this.loadDraft(json, () => {
                    this.validateForm()
                    if (json.nature) this.fetchAttachmentTypes()
                    this.setState({ active: this.state.fields.actes[0].uuid })
                })
            )
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    componentWillUnmount() {
        this.validateForm.clear()
        this.saveDraft.clear()
    }
    loadDraft = (draft, callback) => {
        // Hacks to prevent affecting `null` values
        if (!draft.nature) draft.nature = ''
        if (!draft.decision) draft.decision = ''
        this.setState({ fields: draft }, callback)
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
                const { fields, formValid } = this.state
                fields.actes.push(json)
                formValid[json.uuid] = false
                this.setState({ fields: fields, active: json.uuid, formValid: formValid }, this.updateAllFormValid)
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    deleteBatchedActe = (uuid) => {
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/${uuid}`, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                const { fields, statuses, formValid } = this.state
                fields.actes = fields.actes.filter(acte => acte.uuid !== uuid)
                delete statuses[uuid]
                delete formValid[uuid]
                this.setState({ fields: fields, statuses: statuses, formValid: formValid }, this.updateAllFormValid)
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    fetchAttachmentTypes = () => {
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/acte/attachment-types/${this.state.fields.nature}`, headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ attachmentTypes: json }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    removeAttachmentTypes = () => {
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/types`, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                this.setState({ draftStatus: 'saved' }, this.updateStatus())
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
                this.setState({ draftStatus: '' }, this.updateStatus)
                this.validateForm()
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
            if (field === 'nature') {
                this.fetchAttachmentTypes()
                this.removeAttachmentTypes()
            }
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
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
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
    setField = (uuid, field, value) => {
        const { fields } = this.state
        const acte = fields.actes.find(acte => acte.uuid === uuid)
        acte[field] = value
        this.setState({ fields })
    }
    submitDraft = () => {
        const { fields } = this.state
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.uuid}`, method: 'POST', context: this.context })
            .then(checkStatus)
            .then(response => response.text())
            .then(acteUuid => {
                this.context._addNotification(notifications.acte.sent)
                history.push('/actes')
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    initDelete = () => this.setState({ shouldUnmount: false }, this.deleteDraft)
    deleteDraft = () => {
        const { fields } = this.state
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.uuid}`, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(response => response.text())
            .then(acteUuid => {
                this.context._addNotification(notifications.acte.draftDeleted)
                history.push('/actes')
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
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
                formValid={this.state.formValid[acte.uuid]}
                isActive={this.state.active === acte.uuid}
                handleClick={this.handleClick}
                titlePlaceholder={t('acte.new.batch_title_placeholder')}
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
                    setField={this.setField}
                    shouldUnmount={this.shouldUnmount}
                    attachmentTypes={this.state.attachmentTypes}
                />
            </WrappedActeForm>
        )
        return (
            <div>
                <Segment>
                    <Header size='medium'>{t('acte.new.common_fields')}</Header>
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
                    <Button onClick={this.addBatchedActe} style={{ marginBottom: '1em' }} basic fluid>{t('acte.new.add_an_acte')}</Button>
                </Accordion>
                <div style={{ textAlign: 'right' }}>
                    {renderIf(this.state.fields.uuid)(
                        <Button style={{ marginRight: '1em' }} onClick={this.initDelete} compact basic color='red' disabled={isFormSaving} loading={isFormSaving}>
                            {t('api-gateway:form.delete_draft')}
                        </Button>
                    )}
                    <Button primary basic onClick={this.submitDraft} disabled={!this.state.isAllFormValid || isFormSaving} loading={isFormSaving}>{t('api-gateway:form.submit')}</Button>
                </div>
            </div>
        )
    }
}

const styles = {
    overflow: {
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis'
    },
    centered: {
        display: 'flex',
        alignItems: 'center'
    }
}

const WrappedActeForm = ({ children, isActive, handleClick, acte, deleteBatchedActe, titlePlaceholder, formValid }) =>
    <Segment style={{ paddingTop: '0', paddingBottom: '0' }}>
        <Accordion.Title active={isActive}>
            <Grid>
                <Grid.Column style={styles.centered} width={1} onClick={() => handleClick(acte.uuid)}>
                    <Header size='small'><Icon name='dropdown' /></Header>
                </Grid.Column>
                <Grid.Column style={styles.centered} width={3} onClick={() => handleClick(acte.uuid)}>
                    <Header size='small' style={styles.overflow}>
                        {!acte.number && !acte.objet ? titlePlaceholder
                            : acte.number ? `N° ${acte.number}` : ''}
                    </Header>
                </Grid.Column>
                <Grid.Column style={styles.centered} width={10} onClick={() => handleClick(acte.uuid)}>
                    <Header size='small' style={styles.overflow}>
                        {acte.objet}
                    </Header>
                </Grid.Column>
                <Grid.Column width={1} style={styles.centered}>
                    {formValid ? <Icon color='green' name='checkmark' size='large' />
                        : <Icon color='red' name='warning circle' size='large' />}
                </Grid.Column>
                <Grid.Column width={1}>
                    <Button color='red' basic size='tiny' icon onClick={() => deleteBatchedActe(acte.uuid)}>
                        <Icon name='remove' />
                    </Button>
                </Grid.Column>
            </Grid>
        </Accordion.Title>
        <Accordion.Content style={{ marginBottom: '1em' }} active={isActive}>
            {children}
        </Accordion.Content>
    </Segment>

export default translate(['acte', 'api-gateway'])(NewActeBatchedForm)