import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Accordion, Icon, Segment, Grid, Button, Header, Form, Dropdown, Popup } from 'semantic-ui-react'
import moment from 'moment'
import Validator from 'validatorjs'
import debounce from 'debounce'

import history from '../_util/history'
import { notifications } from '../_util/Notifications'
import { FormField, ValidationPopup } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import {bytesToSize, getLocalAuthoritySlug, toUniqueArray} from '../_util/utils'
import NewActeForm from './NewActeForm'
import {maxArchiveSize, natures} from '../_util/constants'
import { withAuthContext } from '../Auth'
import AdminService from '../_util/admin-service'

class NewActeBatchedForm extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        active: null,
        fields: {
            uuid: '',
            actes: [],
            lastModified: null,
            decision: '',
            nature: '',
            groupUuid: null
        },
        errors:{},
        groups: [],
        draftStatus: null,
        draftValid: false,
        statuses: {},
        formValid: {},
        formErrors: {},
        isAllFormValid: false,
        shouldUnmount: true,
        totalActesSize: {}
    }
    validationRules = {
        nature: 'required',
        decision: ['required', 'date']
    }
    componentDidMount = async () => {
        this._adminService = new AdminService()
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const url = this.props.uuid ? '/api/acte/drafts/' + this.props.uuid : '/api/acte/draft/batch'

        const groups = await this._adminService.getGroups(this.context, 'ACTES_DEPOSIT')
        this.setState({ groups })

        _fetchWithAuthzHandling({ url })
            .then(response => response.json())
            .then(json =>
                this.loadDraft(json, () => {
                    this.updateGroup()
                    this.validateForm()
                    this.setState({ active: this.state.fields.actes[0].uuid })
                })
            )
            .catch(response => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', response.message)
            })
    }
    componentWillUnmount() {
        this.validateForm.clear()
        this.saveDraft.clear()
    }
    loadDraft = (draft, callback) => {
        // Hacks to prevent affecting `null` values
        if (!draft.nature) draft.nature = ''
        if (!draft.decision) {
            draft.decision = ''
        } else {
            draft.decision = moment(draft)
        }
        this.setState({ fields: draft }, callback)
    }
    getDraftData = () => {
        const draftData = Object.assign({}, this.state.fields)
        if (draftData['nature'] === '') draftData['nature'] = null
        if (!draftData['decision']) {
            draftData['decision'] = null
        } else {
            draftData['decision'] = moment(draftData['decision']).format('YYYY-MM-DD')
        }
        return draftData
    }
    addBatchedActe = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/newActe`, method: 'POST', context: this.props.authContext })
            .then(response => response.json())
            .then(json => {
                const { fields, formValid } = this.state
                fields.actes.push(json)
                formValid[json.uuid] = false
                this.setState({ fields: fields, active: json.uuid, formValid: formValid }, this.updateAllFormValid)
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    deleteBatchedActe = (uuid) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/${uuid}`, method: 'DELETE', context: this.props.authContext })
            .then(() => {
                const { fields, statuses, formValid, formErrors, totalActesSize } = this.state
                fields.actes = fields.actes.filter(acte => acte.uuid !== uuid)
                delete statuses[uuid]
                delete formValid[uuid]
                delete formErrors[uuid]
                delete totalActesSize[uuid]
                this.setState({ fields, statuses, formValid, formErrors, totalActesSize }, this.updateAllFormValid)
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    removeAttachmentTypes = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/types`, method: 'DELETE', context: this.props.authContext })
            .then(() => this.setState({ draftStatus: 'saved' }, this.updateStatus()))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
                this.setState({ draftStatus: '' }, this.updateStatus)
                this.validateForm()
            })
    }
    handleClick = (uuid) => {
        const newActive = this.state.active !== uuid ? uuid : null
        this.setState({ active: newActive })
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        this.setState({ fields: fields, draftStatus: '' }, () => {
            this.updateStatus()
            this.validateForm()
            this.saveDraft()
            if (field === 'nature') this.removeAttachmentTypes()
        })
    }
    validateForm = debounce(() => {
        const data = {
            nature: this.state.fields.nature,
            decision: this.state.fields.decision,
        }
        const validationRules = this.validationRules
        const validation = new Validator(data, validationRules)
        const draftValid = validation.passes()
        this.setState({ draftValid }, this.updateAllFormValid)
    }, 500)
    saveDraft = debounce((callback) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        this.setState({ draftStatus: 'saving' }, this.updateStatus)
        const draftData = this.getDraftData()
        const headers = { 'Content-Type': 'application/json' }
        const url= `/api/acte/drafts/${draftData.uuid}`
        _fetchWithAuthzHandling({ url, body: JSON.stringify(draftData), headers: headers, method: 'PATCH', context: this.props.authContext })
            .then(() => this.setState({ draftStatus: 'saved' }, this.updateStatus))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
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
    setFormValidForId = (isFormValidValue, uuid, formErrorsValue) => {
        const { formValid, formErrors } = this.state
        formValid[uuid] = isFormValidValue
        formErrors[uuid] = formErrorsValue
        this.setState({ formValid, formErrors }, this.updateAllFormValid)
    }
    updateGroup = () => {
        if (this.state.fields.groupUuid === null) {
            const { fields } = this.state
            fields.groupUuid = this.state.groups[0].uuid
            this.setState({ fields })
        }
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
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const { fields } = this.state
        _fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.uuid}`, method: 'POST', context: this.props.authContext })
            .then(response => response.text())
            .then(acteUuid => {
                this.setState({ shouldUnmount: false }, () => {
                    _addNotification(notifications.acte.sent)
                    history.push('/actes')
                })
            })
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    deleteDraft = () => {
        const localAuthoritySlug = getLocalAuthoritySlug()
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const { fields } = this.state
        const regex = /brouillons/g
        _fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.uuid}`, method: 'DELETE', context: this.props.authContext })
            .then(response => response.text())
            .then(acteUuid => {
                this.setState({ shouldUnmount: false }, () => {
                    _addNotification(notifications.acte.draftDeleted)
                    if(this.props.path.match(regex)) {
                        history.push(`/${localAuthoritySlug}/actes/brouillons`)
                    } else {
                        history.push(`/${localAuthoritySlug}/actes/liste`)
                    }
                })
            })
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }

    updateErrors = (acteUuid, newErrors) => {
        const {errors} = this.state
        errors[acteUuid] = newErrors
        this.setState({errors})
    }

    updateActesSizes = (acteUuid, size) => {
        const {totalActesSize} = this.state
        totalActesSize[acteUuid] = size
        this.setState({totalActesSize})
    }

    _sumActesSize = () => {
        const {totalActesSize} = this.state
        return Object.keys(totalActesSize).reduce((accumulator, key) => accumulator + totalActesSize[key], 0)
    }

    render() {
        const { t } = this.context
        const isFormSaving = this.props.status === 'saving'
        const draftUuid = this.state.fields.uuid ? this.state.fields.uuid : this.props.uuid
        const natureOptions = natures.map(nature =>
            ({ key: nature, value: nature, text: t(`acte.nature.${nature}`) })
        )
        const groupOptions = this.state.groups.map(group =>
            ({ key: group.uuid, value: group.uuid, text: group.name })
        )
        let errorList = []
        Object.values(this.state.formErrors).forEach(acteErrors => errorList = toUniqueArray([...errorList, ...acteErrors]))
        const wrappedActes = this.state.fields.actes.map(acte =>
            <WrappedActeForm
                key={acte.uuid}
                acte={acte}
                formValid={this.state.formValid[acte.uuid]}
                errors={this.state.errors[acte.uuid]}
                isActive={this.state.active === acte.uuid}
                handleClick={this.handleClick}
                labelDelete={t('acte.new.batch_title_placeholder')}
                titlePlaceholder={t('acte.new.batch_title_placeholder')}
                deleteBatchedActe={this.deleteBatchedActe}
                size={this.state.totalActesSize[acte.uuid]}>
                <NewActeForm
                    uuid={acte.uuid}
                    draftUuid={draftUuid}
                    mode='ACTE_BATCH'
                    nature={this.state.fields.nature}
                    decision={this.state.fields.decision}
                    groupeUuid={this.state.fields.groupeUuid}
                    active={this.state.active}
                    setStatus={this.setStatusForId}
                    status={this.state.status}
                    setFormValidForId={this.setFormValidForId}
                    setField={this.setField}
                    shouldUnmount={this.state.shouldUnmount}
                    callBackErrorMessages={(errors) => this.updateErrors(acte.uuid, errors)}
                    callBackActeFilesSize={(size) => this.updateActesSizes(acte.uuid, size)}
                />
            </WrappedActeForm>
        )
        const submissionButton =
            <Button primary basic onClick={this.submitDraft} disabled={!this.state.isAllFormValid || isFormSaving || wrappedActes.length === 0} loading={isFormSaving}>
                <Icon name={'paper plane'}/>
                {t('api-gateway:form.submit')}
            </Button>
        return (
            <div>
                <Segment>
                    <Header size='medium'>{t('acte.new.common_fields')}</Header>
                    <Form>
                        <Grid columns={2} style={{ marginBottom: 'auto' }}>
                            <Grid.Column>
                                <FormField htmlFor={'decision'} label={t('acte.fields.decision')} helpText={t('acte.help_text.decision')} required>
                                    <InputValidation id={'decision'}
                                        type='date'
                                        placeholder={t('acte:acte.fields.date')}
                                        value={this.state.fields.decision}
                                        onChange={this.handleFieldChange}
                                        validationRule={this.validationRules.decision}
                                        fieldName={t('acte.fields.decision')}
                                        isValidDate={(current) => current.isBefore(new moment())} />
                                </FormField>
                            </Grid.Column>
                            <Grid.Column>
                                <FormField htmlFor={'groupUuid'} label={t('acte.fields.group')} helpText={t('acte.help_text.group')} required>
                                    <Dropdown id='groupUuid'
                                        value={this.state.fields.groupUuid}
                                        onChange={(event, { id, value }) => this.handleFieldChange(id, value)}
                                        options={groupOptions}
                                        fluid selection />
                                </FormField>
                            </Grid.Column>
                        </Grid>
                        <FormField htmlFor={'nature'} label={t('acte.fields.nature')} helpText={t('acte.help_text.nature')} required>
                            <InputValidation id={'nature'}
                                type='dropdown'
                                value={this.state.fields.nature}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.nature}
                                fieldName={t('acte.fields.nature')}
                                options={natureOptions} />
                        </FormField>
                    </Form>
                </Segment>
                <Accordion>
                    {wrappedActes}
                    <Button onClick={this.addBatchedActe} style={{ marginBottom: '1em' }} basic fluid>
                        <Icon size={'large'} name={'plus'}/>
                    </Button>
                </Accordion>
                <div style={{ textAlign: 'right' }}>
                    {(this._sumActesSize()) > 0  && (
                        <label style={{ fontSize: '1em', color: 'rgba(0,0,0,0.4)', marginRight: '10px'}}>
                            {this.context.t('acte.help_text.annexes_size')} {bytesToSize(this._sumActesSize())} / {bytesToSize(maxArchiveSize)}
                        </label>
                    )}
                    {this.state.fields.uuid && (
                        <Button style={{ marginRight: '1em' }} onClick={this.deleteDraft} basic color='red'
                            disabled={isFormSaving} loading={isFormSaving}>
                            <Icon name={'trash'}/>
                            {t('api-gateway:form.delete_draft')}
                        </Button>
                    )}
                    {errorList.length > 0 &&
                    <ValidationPopup errorList={errorList}>
                        {submissionButton}
                    </ValidationPopup>
                    }
                    {errorList.length === 0 && submissionButton}

                </div>
            </div>
        )
    }
}

const styles = {
    overflow: {
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        fontWeight: 800
    },
    textNumber:{
        fontWeight: 800
    },
    centered: {
        display: 'flex',
        alignItems: 'center'
    }
}

const WrappedActeForm = ({ children, isActive, handleClick, acte, deleteBatchedActe, titlePlaceholder, errors, formValid, labelDelete, size}) =>
    <Segment style={{ paddingTop: '0', paddingBottom: '0' }}>
        <Accordion.Title active={isActive}>
            <Grid stretched verticalAlign={'middle'}>
                <Grid.Column width={1} onClick={() => handleClick(acte.uuid)}>
                    <Icon name='dropdown' size={'large'} />
                </Grid.Column>
                <Grid.Column width={1}>
                    {
                        formValid ?
                            <Icon color='green' name='checkmark' size={'large'}/>
                            :
                            <ValidationPopup errorList= {errors}>
                                <Icon color='orange' name='warning sign' size={'large'}/>
                            </ValidationPopup>
                    }
                </Grid.Column>
                <Grid.Column width={3} onClick={() => handleClick(acte.uuid)}>
                    <p style={{...styles.overflow, ...styles.textNumber}}>
                        {!acte.number && !acte.objet ? titlePlaceholder
                            : acte.number ? `N° ${acte.number}` : ''}
                    </p>
                </Grid.Column>
                <Grid.Column width={8} onClick={() => handleClick(acte.uuid)}>
                    <p style={styles.overflow}>{acte.objet}</p>
                </Grid.Column>
                <Grid.Column width={2} onClick={() => handleClick(acte.uuid)} textAlign={'right'}>
                    {size > 0  && (
                        <label style={{ fontSize: '1em', color: 'rgba(0,0,0,0.4)'}}>
                            {bytesToSize(size)}
                        </label>
                    )}
                </Grid.Column>
                <Grid.Column width={1} textAlign={'right'} floated={'right'}>
                    <Popup trigger={<Icon name='close' color='red' aria-label={labelDelete} onClick={() => deleteBatchedActe(acte.uuid)} size={'large'}/>} content={'Delete Acte'}/>
                </Grid.Column>
            </Grid>
        </Accordion.Title>
        <Accordion.Content style={{ marginBottom: '1em' }} active={isActive}>
            {children}
        </Accordion.Content>
    </Segment>

export default translate(['acte', 'api-gateway'])(withAuthContext(NewActeBatchedForm))
