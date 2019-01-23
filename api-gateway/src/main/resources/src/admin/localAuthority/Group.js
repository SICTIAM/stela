import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Label, Icon, Dropdown, Form, Button } from 'semantic-ui-react'

import history from '../../_util/history'
import { notifications } from '../../_util/Notifications'
import { FieldInline, Page } from '../../_components/UI'
import InputValidation from '../../_components/InputValidation'
import { checkStatus } from '../../_util/utils'
import { withAuthContext } from '../../Auth'

class Group extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
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
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const { uuid } = this.props
        if (uuid) {
            _fetchWithAuthzHandling({ url: `/api/admin/local-authority/group/${uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ fields: json }))
                .catch(response => {
                    response.json().then(json => {
                        _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                    })
                })
        }
        // Sometimes componentDidUpdate doesn't trigger
        if(this.props.authContext.profile && this.props.authContext.profile.localAuthority) {
            this.setState({activatedModules: this.props.authContext.profile.localAuthority.activatedModules}, this.fetchAllRights)
        }
    }
    componentDidUpdate() {
        // QuickFix
        // context sometimes doen't load in ComponentDidMount
        if (this.props.authContext.profile && this.props.authContext.profile.localAuthority && !Object.is(this.props.authContext.profile.localAuthority.activatedModules, this.state.activatedModules)) {
            this.setState({activatedModules: this.props.authContext.profile.localAuthority.activatedModules}, this.fetchAllRights)
        }
    }
    fetchAllRights = () => {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/api-gateway/rights' })
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
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const { uuid, localAuthorityUuid } = this.props
        const body = JSON.stringify(this.state.fields)
        const headers = { 'Content-Type': 'application/json' }
        const url = uuid
            ? `/api/admin/local-authority/group/${uuid}`
            : `/api/admin/local-authority/${localAuthorityUuid || 'current'}/group`
        const method = uuid ? 'PATCH' : 'POST'
        _fetchWithAuthzHandling({ url, method, body, headers, context: this.props.authContext })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                if (!uuid) history.push(this.props.location.pathname + '/' + json.uuid)
                _addNotification(uuid ? notifications.admin.groupUpdated : notifications.admin.groupCreated)
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
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
                        <FieldInline htmlFor='name' label={t('group.name')}>
                            <InputValidation id='name'
                                placeholder={t('group.name') + '...'}
                                value={group.name}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.name}
                                fieldName={t('group.name')} />
                        </FieldInline>
                        <FieldInline htmlFor='rights' label={t('group.rights')}>
                            <div style={{ marginBottom: '0.5em' }}>{rights.length > 0 ? rights : t('admin.group.no_rights')}</div>
                            <div style={{ marginBottom: '1em' }}>
                                <Dropdown id='groups' value='' placeholder={t('admin.group.add_right') + '...'}
                                    fluid selection options={rightsOptions} onChange={this.handleRightChange} />
                            </div>
                        </FieldInline>
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary onClick={this.submitForm} type='submit'>
                                {t(this.props.uuid ? 'form.update' : 'form.create')}
                            </Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(withAuthContext(Group))
