import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Accordion, Icon, Segment, Grid, Button, Header } from 'semantic-ui-react'

import history from '../_util/history'
import { errorNotification, acteSentSuccess, draftDeletedSuccess } from '../_components/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import NewActeForm from './NewActeForm'

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
            lastModified: null
        },
        statuses: {},
        formValid: {},
        isAllFormValid: false,
        shouldUnmount: true
    }
    componentDidMount() {
        const url = this.props.uuid ? '/api/acte/drafts/' + this.props.uuid : '/api/acte/draft/batch'
        fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ fields: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })
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
    setStatusForId = (statusValue, uuid) => {
        const { statuses } = this.state
        statuses[uuid] = statusValue
        this.setState({ statuses }, this.updateStatus)
    }
    updateStatus = () => {
        const statuses = Object.values(this.state.statuses)
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
        Object.values(this.state.formValid).map(bool => isAllFormValid = isAllFormValid && bool)
        this.setState({ isAllFormValid })
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
                    active={this.state.active}
                    setStatus={this.setStatusForId}
                    status={this.state.status}
                    setFormValidForId={this.setFormValidForId}
                    shouldUnmount={this.shouldUnmount}
                />
            </WrappedActeForm>
        )
        return (
            <div>
                <Accordion>
                    {wrappedActes}
                    <Button onClick={this.addBatchedActe} style={{ marginBottom: '1em' }} basic fluid>Ajouter un acte</Button>
                </Accordion>
                <Button onClick={this.submitDraft} disabled={!this.state.isAllFormValid || isFormSaving} loading={isFormSaving}>{t('form.submit')}</Button>
                {renderIf(this.state.fields.uuid)(
                    <Button style={{ marginLeft: '1em' }} onClick={this.initDelete} compact basic color='red' disabled={isFormSaving} loading={isFormSaving}>
                        {t('form.delete_draft')}
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

export default translate(['api-gateway'])(NewActeBatchedForm)