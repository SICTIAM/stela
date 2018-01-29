import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Segment, Grid, Label, Input, Header, Checkbox } from 'semantic-ui-react'

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
            notifications: [],
            localAuthority: {
                uuid: '',
                name: '',
                slugName: '',
                siren: '',
                activatedModules: []
            }
        },
        profiles: [],
        allNotifications: []
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/profile' })
            .then(response => response.json())
            .then(json => this.setState({ activeProfile: json }))
        fetchWithAuthzHandling({ url: '/api/admin/agent/profiles' })
            .then(response => response.json())
            .then(json => this.setState({ profiles: json }))
        fetchWithAuthzHandling({ url: '/api/admin/profile/all-notifications' })
            .then(response => response.json())
            .then(json => this.setState({ allNotifications: json }))
    }
    onChange = (uuidProfile, id, value) => {
        const { profiles } = this.state
        const profile = profiles.find(profile => profile.uuid === uuidProfile)
        profile[id] = value
        this.setState({ profiles })
    }
    onCheckboxChange = (uuidProfile, id) => {
        const { profiles } = this.state
        const profile = profiles.find(profile => profile.uuid === uuidProfile)
        const index = profile.notifications.indexOf(id)
        index === -1 ? profile.notifications.push(id) : profile.notifications.splice(index, 1)
        this.setState({ profiles })
    }
    updateProfile = (uuid) => {
        const profile = this.state.profiles.find(profile => profile.uuid === uuid)
        const profileUI = {
            uuid: profile.uuid,
            email: profile.email,
            notifications: profile.notifications
        }
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `api/admin/profile/${uuid}`, body: JSON.stringify(profileUI), headers: headers, method: 'PATCH', context: this.context })
            .then(checkStatus)
            .then(() => this.context._addNotification(notifications.profile.updated))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    render() {
        const { t, user } = this.context
        const { activeProfile, profiles, allNotifications } = this.state
        const currentLocalAuthorityProfile = profiles.find(profile => profile.localAuthority.uuid === activeProfile.localAuthority.uuid)
        const allLocalAuthorityProfiles = [
            <LocalAuthorityProfile
                key={currentLocalAuthorityProfile ? currentLocalAuthorityProfile.uuid : 'current'}
                profile={currentLocalAuthorityProfile}
                onChange={this.onChange}
                updateProfile={this.updateProfile}
                allNotifications={allNotifications}
                onCheckboxChange={this.onCheckboxChange} />
        ]
        profiles
            .filter(profile => profile.localAuthority.uuid !== activeProfile.localAuthority.uuid)
            .map(profile => allLocalAuthorityProfiles.push(
                <LocalAuthorityProfile
                    key={profile.uuid}
                    profile={profile}
                    isDefaultOpen={false}
                    onChange={this.onChange}
                    updateProfile={this.updateProfile}
                    allNotifications={allNotifications}
                    onCheckboxChange={this.onCheckboxChange} />
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
                    <div style={{ textAlign: 'right' }}>
                        <Button style={{ color: '#663399', boxShadow: '0 0 0 1px #663399', background: 'transparent none' }}>{t('profile.modify_my_profile')}</Button>
                    </div>
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
        onCheckboxChange: PropTypes.func.isRequired
    }
    static defaultProps = {
        profile: {
            uuid: '',
            email: '',
            notifications: [],
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
        const { profile, isDefaultOpen, allNotifications, onChange, updateProfile, onCheckboxChange } = this.props
        const modulesRows = modules.map(moduleName =>
            <Grid.Column key={moduleName} textAlign='center'>
                <Label color={profile.localAuthority.activatedModules.includes(moduleName) ? 'green' : 'red'}>{t(`modules.${moduleName}`)}</Label>
            </Grid.Column>
        )
        const profileNotifications = allNotifications.map(notification =>
            <Field key={notification} htmlFor={notification} label={t(`profile.notifications.${notification}`)}>
                <Checkbox toggle
                    id={notification}
                    checked={profile.notifications.includes(notification)}
                    onChange={((e, { id }) => onCheckboxChange(profile.uuid, id))} />
            </Field>
        )
        const content = (
            <div>
                <Field htmlFor='modules' label={t('agent.modules')}>
                    <Grid id='modules' columns={modules.length}>
                        <Grid.Row>{modulesRows}</Grid.Row>
                    </Grid>
                </Field>
                <Field htmlFor='email' label={t('agent.email')}>
                    <Input id='email' value={profile.email || ''} placeholder={t('profile.no_email')}
                        onChange={(e, { id, value }) => onChange(profile.uuid, id, value)} />
                </Field>
                <Header size='small'>Notifications</Header>
                {profileNotifications}
                <div style={{ textAlign: 'right' }}>
                    <Button basic primary onClick={() => updateProfile(profile.uuid)}>{t('form.update')}</Button>
                </div>
            </div>
        )
        return (
            <AccordionSegment title={profile.localAuthority.name} isDefaultOpen={isDefaultOpen} content={content} />
        )
    }
}

export default translate('api-gateway')(Profile)