import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Label, Icon, Dropdown, Form, Button } from 'semantic-ui-react'

import { notifications } from '../../_util/Notifications'
import { Field, Page } from '../../_components/UI'
import InputValidation from '../../_components/InputValidation'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class Group extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    static defaultProps = {
        uuid: '',
        localAuthorityUuid: ''
    }
    state = {
        fields: {
            uuid: '',
            name: '',
            rights: []
        },
        allRights: [],
        activatedModules: []
    }
    validationRules = {
        name: 'required|max:250',
    }
    componentDidMount() {
        const { localAuthorityUuid, uuid } = this.props
        if (localAuthorityUuid && uuid) {
            fetchWithAuthzHandling({ url: `/api/admin/local-authority/${localAuthorityUuid}/group/${uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ fields: json }))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                    })
                })
        }
        fetchWithAuthzHandling({ url: '/api/admin/profile' })
            .then(response => response.json())
            .then(profile =>
                this.setState({ activatedModules: profile.localAuthority.activatedModules }, this.fetchAllRights)
            )
    }
    fetchAllRights = () => {
        fetchWithAuthzHandling({ url: '/api/api-gateway/rights' })
            .then(response => response.json())
            .then(rights => this.setState({ allRights: rights.filter(right => this.state.activatedModules.includes(right.split('_')[0])) }))
    }
    handleFieldChange = (field, value) => {
        const { fields } = this.state
        fields[field] = value
        this.setState({ fields })
    }
    handleRightChange = (event, { value }) => {
        const { allRights, fields } = this.state
        const right = allRights.find(right => right === value)
        fields.rights.push(right)
        this.setState({ fields })
    }
    removeRight = (right) => {
        const { fields } = this.state
        fields.rights = fields.rights.filter(rightItem => rightItem !== right)
        this.setState({ fields })
    }
    submitForm = () => {
        const { uuid, localAuthorityUuid } = this.props
        const body = JSON.stringify(this.state.fields)
        const headers = { 'Content-Type': 'application/json' }
        const url = `/api/admin/local-authority/${localAuthorityUuid}/group${uuid ? `/${uuid}` : ''}`
        const method = uuid ? 'PATCH' : 'POST'
        fetchWithAuthzHandling({ url, method, body, headers, context: this.context })
            .then(checkStatus)
            .then(() => {
                const { uuid } = this.props
                this.context._addNotification(uuid ? notifications.admin.groupUpdated : notifications.admin.groupCreated)
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    render() {
        const { t } = this.context
        const group = this.state.fields
        const rightsOptions = this.state.allRights
            .filter(right => !this.state.fields.rights.some(profileRight => profileRight === right))
            .map(right => {
                return { key: right, value: right, text: t(`right.${right}`) }
            })
        const rights = this.state.fields.rights.map(right =>
            <Label basic key={right}>{t(`right.${right}`)} <Icon name='delete' onClick={() => this.removeRight(right)} /></Label>
        )
        return (
            <Page title='Groupe'>
                <Segment>
                    <Form>
                        <Field htmlFor='name' label={t('group.name')}>
                            <InputValidation id='name'
                                placeholder={t('group.name') + '...'}
                                value={group.name}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.name}
                                fieldName={t('group.name')} />
                        </Field>
                        <Field htmlFor='rights' label={t('group.rights')}>
                            <div style={{ marginBottom: '0.5em' }}>{rights.length > 0 ? rights : t('admin.group.no_rights')}</div>
                            <div style={{ marginBottom: '1em' }}>
                                <Dropdown id='groups' value='' placeholder={t('admin.group.add_right') + '...'} fluid selection options={rightsOptions} onChange={this.handleRightChange} />
                            </div>
                        </Field>
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary onClick={this.submitForm} type='submit'>{t('api-gateway:form.update')}</Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(Group)