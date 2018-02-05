import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Segment, Label, Input, Header, Checkbox } from 'semantic-ui-react'

import { Field, Page } from './_components/UI'
import { notifications } from './_util/Notifications'
import AccordionSegment from './_components/AccordionSegment'
import { fetchWithAuthzHandling, checkStatus } from './_util/utils'
import { modules } from './_util/constants'

class Profile extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        user: PropTypes.object,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        activeProfile: {
            uuid: '',
            email: '',
            notificationValues: [],
            localAuthorityNotifications: [],
            localAuthority: {
                uuid: '',
                name: '',
                slugName: '',
                siren: '',
                activatedModules: []
            }
        },
        agent: {
            uuid: '',
            email: '',
            family_name: '',
            given_name: '',
            profiles: []
        },
        allNotifications: []
    }
    componentDidMount() {
        const { uuid } = this.props
        if (!uuid)
            fetchWithAuthzHandling({ url: '/api/admin/profile' })
                .then(response => response.json())
                .then(json => this.setState({ activeProfile: json }))
        fetchWithAuthzHandling({ url: uuid ? `/api/admin/agent/${uuid}` : '/api/admin/agent' })
            .then(response => response.json())
            .then(json => this.setState({ agent: json }))
        fetchWithAuthzHandling({ url: '/api/api-gateway/profile/all-notifications' })
            .then(response => response.json())
            .then(json => this.setState({ allNotifications: json }))
    }
    onChange = (uuidProfile, id, value) => {
        const { agent } = this.state
        const profile = agent.profiles.find(profile => profile.uuid === uuidProfile)
        profile[id] = value
        this.setState({ agent })
    }
    onLocalAuthorityNotificationsChange = (uuidProfile, module , checked) => {
        const { agent } = this.state
        const profile = agent.profiles.find(profile => profile.uuid === uuidProfile)
        checked ? profile.localAuthorityNotifications.push(module) : profile.localAuthorityNotifications.splice(profile.localAuthorityNotifications.indexOf(module), 1)
        this.setState({ agent })
    }
    onCheckboxChange = (uuidProfile, statusType, checked) => {
        const { agent } = this.state
        const profile = agent.profiles.find(profile => profile.uuid === uuidProfile)
        const notification = profile.notificationValues.find(notification => notification.name === statusType)
        notification ? notification.active = checked : profile.notificationValues.push({ name: statusType, active: checked })
        this.setState({ agent })
    }
    updateProfile = (uuid) => {
        const profile = this.state.agent.profiles.find(profile => profile.uuid === uuid)
        const profileUI = {
            uuid: profile.uuid,
            email: profile.email,
            notificationValues: profile.notificationValues,
            localAuthorityNotifications: profile.localAuthorityNotifications
        }
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/admin/profile/${uuid}`, body: JSON.stringify(profileUI), headers: headers, method: 'PATCH', context: this.context })
            .then(checkStatus)
            .then(() => this.context._addNotification(notifications.profile.updated))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    render() {
        const { t, user } = this.context
        const { activeProfile, agent, allNotifications } = this.state
        const currentLocalAuthorityProfile = agent.profiles.find(profile => profile.localAuthority.uuid === activeProfile.localAuthority.uuid)
        const allLocalAuthorityProfiles = this.props.uuid ? [] : [
            <LocalAuthorityProfile
                key={currentLocalAuthorityProfile ? currentLocalAuthorityProfile.uuid : 'current'}
                profile={currentLocalAuthorityProfile}
                onChange={this.onChange}
                updateProfile={this.updateProfile}
                allNotifications={allNotifications}
                onCheckboxChange={this.onCheckboxChange}
                onLocalAuthorityNotificationsChange={this.onLocalAuthorityNotificationsChange}
            />
        ]
        agent.profiles
            .filter(profile => this.props.uuid || (profile.localAuthority.uuid !== activeProfile.localAuthority.uuid))
            .map(profile => allLocalAuthorityProfiles.push(
                <LocalAuthorityProfile
                    key={profile.uuid}
                    profile={profile}
                    isDefaultOpen={false}
                    onChange={this.onChange}
                    updateProfile={this.updateProfile}
                    allNotifications={allNotifications}
                    onCheckboxChange={this.onCheckboxChange}
                    onLocalAuthorityNotificationsChange={this.onLocalAuthorityNotificationsChange}/>
            ))
        return (
            <Page title={t('profile.title')}>
                <Segment style={{ borderTop: '2px solid #663399' }}>
                    <Field htmlFor='family_name' label={t('agent.family_name')}>
                        <span id='family_name'>{user.family_name}</span>
                    </Field>
                    <Field htmlFor='given_name' label={t('agent.given_name')}>
                        <span id='given_name'>{user.given_name}</span>
                    </Field>
                    <Field htmlFor='email' label={t('agent.email')}>
                        <span id='email'>{user.email}</span>
                    </Field>
                    {!this.props.uuid &&
                        <div style={{ textAlign: 'right' }}>
                            <a href='/api/api-gateway/ozwillo-portal/my/profile' target='_blank' className='ui button'
                                style={{ display: 'inline-flex', alignItems: 'center', color: '#663399', boxShadow: '0 0 0 1px #663399', background: 'transparent none' }}>
                                <img style={{ height: '1.5em', float: 'left', marginRight: '1em' }} src={process.env.PUBLIC_URL + '/img/logo_ozwillo.png'} alt="Ozwillo" />
                                {t('profile.modify_my_profile')}
                            </a>
                        </div>}
                </Segment>

                {allLocalAuthorityProfiles}

            </Page >
        )
    }
}

class LocalAuthorityProfile extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static propTypes = {
        profile: PropTypes.object.isRequired,
        isDefaultOpen: PropTypes.bool,
        allNotifications: PropTypes.array.isRequired,
        onChange: PropTypes.func.isRequired,
        updateProfile: PropTypes.func.isRequired,
        onCheckboxChange: PropTypes.func.isRequired,
        onLocalAuthorityNotificationsChange: PropTypes.func.isRequired 
    }
    static defaultProps = {
        profile: {
            uuid: '',
            email: '',
            notificationValues: [],
            localAuthorityNotifications : [],
            localAuthority: {
                uuid: '',
                name: '',
                slugName: '',
                siren: '',
                activatedModules: []
            }
        },
        isDefaultOpen: true
    }
    render() {
        const { t } = this.context
        const { profile, isDefaultOpen, allNotifications, onChange, updateProfile, onCheckboxChange, onLocalAuthorityNotificationsChange } = this.props
        const modulesRows = modules.map(moduleName =>
            <Label style={{ marginRight: '1em' }} color={profile.localAuthority.activatedModules.includes(moduleName) ? 'green' : 'red'}>{t(`modules.${moduleName}`)}</Label>
        )
        const profileNotifications = profile.localAuthority.activatedModules.map(activatedModule =>
            <div style={{ marginTop: '2em' }} key={activatedModule}>
                <Header size='small'>{t('profile.notifications_title')} {activatedModule}</Header>
                <Field htmlFor={activatedModule} label={t(`profile.localAuthorityNotifications`)}>
                    <Checkbox toggle
                        id={activatedModule} checked={profile.localAuthorityNotifications.includes(activatedModule)}
                        onChange={((e, { id, checked }) => onLocalAuthorityNotificationsChange(profile.uuid, id, checked))} />
                </Field>
                {allNotifications
                    .filter(notification => notification.statusType.startsWith(`${activatedModule}_`))
                    .map(notification => {
                        const notificationValue = profile.notificationValues.find(notif => notif.name === notification.statusType)
                        return (
                            <Field key={`${profile.uuid}-${notification.statusType}`} htmlFor={`${profile.uuid}-${notification.statusType}`}
                                label={t(`profile.notifications.${notification.statusType}`)}>
                                <Checkbox toggle
                                    id={`${profile.uuid}-${notification.statusType}`}
                                    checked={!notification.deactivatable || (notificationValue ? notificationValue.active : notification.defaultValue)}
                                    disabled={!notification.deactivatable}
                                    onChange={((e, { checked }) => onCheckboxChange(profile.uuid, notification.statusType, checked))} />
                            </Field>
                        )
                    })}
            </div>
        )
        const content = (
            <div>
                <Field htmlFor='modules' label={t('agent.modules')}>
                    <span id='modules'>{modulesRows}</span>
                </Field>
                <Field htmlFor='email' label={t('agent.email')}>
                    <Input id='email' value={profile.email || ''} placeholder={t('profile.no_email')}
                        onChange={(e, { id, value }) => onChange(profile.uuid, id, value)} />
                </Field>
                {profile.localAuthority.activatedModules.length > 0 && profileNotifications}
                <div style={{ textAlign: 'right' }}>
                    <Button basic primary onClick={() => updateProfile(profile.uuid)}>{t('form.update')}</Button>
                </div>
            </div >
        )
        return (
            <AccordionSegment title={profile.localAuthority.name} isDefaultOpen={isDefaultOpen} content={content} />
        )
    }
}

const UserProfile = translate('api-gateway')((props) => <Profile {...props} />)
const AdminProfile = translate('api-gateway')((props) => <Profile {...props} />)

module.exports = { UserProfile, AdminProfile }