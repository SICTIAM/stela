import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Segment, Label, Input, Header, Checkbox, Dropdown } from 'semantic-ui-react'

import { Field, Page } from './_components/UI'
import { notifications } from './_util/Notifications'
import AccordionSegment from './_components/AccordionSegment'
import CertificateInfos from './_components/CertificateInfos'
import { fetchWithAuthzHandling, checkStatus } from './_util/utils'
import { modules, sesileVisibility } from './_util/constants'

class Profile extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
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
    onChange = (uuidProfile, id, value, callback) => {
        const { agent } = this.state
        const profile = agent.profiles.find(profile => profile.uuid === uuidProfile)
        profile[id] = value
        this.setState({ agent }, callback)
    }
    onLocalAuthorityNotificationsChange = (uuidProfile, module, checked) => {
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
        const { t } = this.context
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
                    onLocalAuthorityNotificationsChange={this.onLocalAuthorityNotificationsChange} />
            ))
        return (
            <Page title={t('profile.title')}>
                <Segment style={{ borderTop: '2px solid #663399' }}>
                    <Field htmlFor='family_name' label={t('agent.family_name')}>
                        <span id='family_name'>{agent.family_name}</span>
                    </Field>
                    <Field htmlFor='given_name' label={t('agent.given_name')}>
                        <span id='given_name'>{agent.given_name}</span>
                    </Field>
                    <Field htmlFor='email' label={t('agent.email')}>
                        <span id='email'>{agent.email}</span>
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

                <CertificateInfos />

            </Page >
        )
    }
}

class LocalAuthorityProfile extends Component {
    static contextTypes = {
        t: PropTypes.func,
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string
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
            localAuthorityNotifications: [],
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

    state = {
        sesileConfiguration: {
            profileUuid: '',
            serviceOrganisationNumber: 1,
            type: 2,
            visibility: 3,
            secret: '',
            token: '',
            daysToValidated: 15,
        },
        serviceOrganisationAvailable: [],
        sesileSubscription: false
    }
    sesileConfigurationChange = (e, { id, value }) => {
        const sesileConf = this.state.sesileConfiguration
        sesileConf[id] = value
        this.setState({ sesileConfiguration: sesileConf })
    }
    updateSesileConfiguration = () => {
        const { profile } = this.props
        if (profile.uuid && profile.localAuthority.activatedModules.includes('PES')) {
            const headers = { 'Content-Type': 'application/json' }
            const sesileConf = this.state.sesileConfiguration
            sesileConf.profileUuid = profile.uuid;
            fetchWithAuthzHandling({ url: `/api/pes/sesile/configuration`, body: JSON.stringify(sesileConf), headers: headers, method: 'POST', context: this.context })
                .then(checkStatus)
                .catch(response => {
                    response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
                })
        }

    }

    fetchSesileInformation = () => {
        const { profile } = this.props
        fetchWithAuthzHandling({ url: `/api/pes/sesile/organisations/${profile.localAuthority.uuid}/${profile.uuid}` })
            .then(response => response.json())
            .then(json => {
                this.setState({ serviceOrganisationAvailable: json })
            }).catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
        fetchWithAuthzHandling({ url: `/api/pes/sesile/configuration/${profile.uuid}` })
            .then(response => response.json())
            .then(json => {
                this.setState({ sesileConfiguration: json })
            }).catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }

    componentDidMount() {
        const { profile } = this.props
        if (profile.uuid && profile.localAuthority.activatedModules.includes('PES')) {
            fetchWithAuthzHandling({ url: `/api/pes/sesile/subscription/${profile.localAuthority.uuid}` })
                .then(response => response.text())
                .then(text => {
                    const sesileSub = text === 'true'
                    this.setState({ sesileSubscription: sesileSub })
                    if (sesileSub) this.fetchSesileInformation()
                }).catch(response => {
                    response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
                })
        }
    }
    render() {
        const { t } = this.context
        const { profile, isDefaultOpen, allNotifications, onChange, updateProfile, onCheckboxChange, onLocalAuthorityNotificationsChange } = this.props
        const modulesRows = modules.map(moduleName =>
            <Label key={moduleName} style={{ marginRight: '1em' }} color={profile.localAuthority.activatedModules.includes(moduleName) ? 'green' : 'red'}>{t(`modules.${moduleName}`)}</Label>
        )

        const visibilities = sesileVisibility.map(visibility => {
            return { key: visibility, value: visibility, text: t(`profile.sesile.visibilities.${visibility}`) }
        })

        const serviceOrganisations = this.state.serviceOrganisationAvailable.map(service => {
            return { key: service.id, value: parseInt(service.id, 10), text: service.nom }
        })

        const currentService = this.state.sesileConfiguration.serviceOrganisationNumber ?
            this.state.serviceOrganisationAvailable.find(service => parseInt(service.id, 10) === this.state.sesileConfiguration.serviceOrganisationNumber) : undefined

        const typesAvailable = currentService ? currentService.types.map(type => { return { key: type.id, value: type.id, text: type.nom } }) : []

        const sesileConnection = profile.localAuthority.activatedModules.map(activatedModule =>
            (activatedModule === 'PES' && this.state.sesileSubscription && this.state.sesileConfiguration) &&

            <div key={activatedModule} style={{ marginTop: '1em' }}>
                <Header size='small'>{t('profile.sesile.title')}</Header>
                <Field htmlFor='serviceOrganisationNumber' label={t('profile.sesile.serviceOrganisationNumber')}>
                    <Dropdown compact search selection
                        id='serviceOrganisationNumber'
                        className='simpleInput'
                        placeholder={t('profile.sesile.serviceOrganisationNumber')}
                        options={serviceOrganisations}
                        value={this.state.sesileConfiguration.serviceOrganisationNumber}
                        onChange={this.sesileConfigurationChange} />
                </Field>
                <Field htmlFor='type' label={t('profile.sesile.type')}>
                    <Dropdown compact search selection
                        id='type'
                        placeholder={t('profile.sesile.type')}
                        className='simpleInput'
                        options={typesAvailable}
                        value={this.state.sesileConfiguration.type}
                        onChange={this.sesileConfigurationChange} />
                </Field>
                <Field htmlFor='visibility' label={t('profile.sesile.visibility')}>
                    <Dropdown compact search selection
                        id='visibility'
                        placeholder={t('profile.sesile.visibility')}
                        className='simpleInput'
                        options={visibilities}
                        value={this.state.sesileConfiguration.visibility}
                        onChange={this.sesileConfigurationChange} />
                </Field>
                <Field htmlFor='daysToValidated' label={t('profile.sesile.validationLimit')}>
                    <Input id='daysToValidated'
                        type='number'
                        placeholder={t('profile.sesile.validationLimit')}
                        value={this.state.sesileConfiguration.daysToValidated}
                        onChange={this.sesileConfigurationChange} />
                </Field>
            </div>

        );
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
                    <Input id='email' fluid style={{ maxWidth: '25em' }} value={profile.email || ''} placeholder={t('profile.no_email')}
                        onChange={(e, { id, value }) => onChange(profile.uuid, id, value)} />
                </Field>
                {sesileConnection}
                {profile.localAuthority.activatedModules.length > 0 && profileNotifications}
                <div style={{ textAlign: 'right' }}>
                    <Button basic primary onClick={() => {
                        this.updateSesileConfiguration()
                        updateProfile(profile.uuid)
                    }}>
                        {t('form.update')}
                    </Button>
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