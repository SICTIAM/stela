import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Segment, Label, Input, Header, Checkbox, Dropdown } from 'semantic-ui-react'

import { FieldInline, Page, FieldValue } from './_components/UI'
import { notifications } from './_util/Notifications'
import AccordionSegment from './_components/AccordionSegment'
import CertificateInfos from './_components/CertificateInfos'
import { checkStatus } from './_util/utils'
import { modules, sesileVisibility } from './_util/constants'
import AgentProfile from './admin/localAuthority/AgentProfile'

import { withAuthContext } from './Auth'


const BRAND_COLOR = '#2C55A2'

class Profile extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
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
            profiles: [],
            certificate: {}
        },
        allNotifications: []
    }
    componentDidMount() {
        const { uuid } = this.props
        const { _fetchWithAuthzHandling } = this.context
        this.setState({activeProfile: this.props.authContext.profile})
        if(uuid) {
            _fetchWithAuthzHandling({ url: `/api/admin/agent/${uuid}` })
                .then(response => response.json())
                .then(json => this.setState({ agent: json }))
        } else {
            this.setState({agent: this.props.authContext.user})
        }
        _fetchWithAuthzHandling({ url: '/api/api-gateway/profile/all-notifications' })
            .then(response => response.json())
            .then(json => this.setState({ allNotifications: json }))
    }
    componentDidUpdate() {
        // QuickFix
        // context sometimes doen't load in ComponentDidMount
        const { uuid } = this.props
        if(!uuid && this.props.authContext.user && this.props.authContext.user !== this.state.agent) {
            this.setState({agent: this.props.authContext.user})
        }
    }
    onChange = (uuidProfile, id, value, callback) => {
        const { agent } = this.state
        const profile = agent.profiles.find(profile => profile.uuid === uuidProfile)
        profile[id] = value
        this.setState({ agent }, callback)
    }
    onPairCertification = () => {
        const { uuid, authContext } = this.props
        const { _fetchWithAuthzHandling } = this.context
        if(!uuid) {
            authContext.getUser()
        } else {
            _fetchWithAuthzHandling({ url: `/api/admin/agent/${uuid}` })
                .then(response => response.json())
                .then(json => this.setState({ agent: json }))
        }
    }
    onLocalAuthorityNotificationsChange = (uuidProfile, module, checked) => {
        const { agent } = this.state
        const profile = agent.profiles.find(profile => profile.uuid === uuidProfile)
        checked ? profile.localAuthorityNotifications.push(module)
            : profile.localAuthorityNotifications.splice(profile.localAuthorityNotifications.indexOf(module), 1)
        this.setState({ agent })
    }
    onCheckboxChange = (uuidProfile, type, checked) => {
        const { agent } = this.state
        const profile = agent.profiles.find(profile => profile.uuid === uuidProfile)
        const notification = profile.notificationValues.find(notification => notification.name === type)
        notification ? (notification.active = checked) : profile.notificationValues.push({ name: type, active: checked })
        this.setState({ agent })
    }
    updateProfile = uuid => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const profile = this.state.agent.profiles.find(profile => profile.uuid === uuid)
        const profileUI = {
            uuid: profile.uuid,
            email: profile.email,
            notificationValues: profile.notificationValues,
            localAuthorityNotifications: profile.localAuthorityNotifications
        }
        const headers = { 'Content-Type': 'application/json' }
        _fetchWithAuthzHandling({ url: `/api/admin/profile/${uuid}`, body: JSON.stringify(profileUI), headers, method: 'PATCH', context: this.props.authContext })
            .then(checkStatus)
            .then(() => _addNotification(notifications.profile.updated))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    render() {
        const { t } = this.context
        const { activeProfile, agent, allNotifications } = this.state
        const currentLocalAuthorityProfile = agent && activeProfile && agent.profiles && agent.profiles.find(profile => profile.localAuthority.uuid === activeProfile.localAuthority.uuid)
        const allLocalAuthorityProfiles = []
        if(agent && agent.profiles) {
            agent.profiles.forEach((profile) => {
                if (profile.localAuthority.uuid === activeProfile.localAuthority.uuid) {
                    allLocalAuthorityProfiles.unshift(
                        /** Refactor 'current' is active only when profiles doesn't load */
                        <LocalAuthorityProfile
                            key={currentLocalAuthorityProfile ? currentLocalAuthorityProfile.uuid : 'current'}
                            profile={currentLocalAuthorityProfile}
                            isDefaultOpen={true}
                            authContext={this.props.authContext}
                            onChange={this.onChange}
                            updateProfile={this.updateProfile}
                            allNotifications={allNotifications}
                            onCheckboxChange={this.onCheckboxChange}
                            onLocalAuthorityNotificationsChange={this.onLocalAuthorityNotificationsChange}
                        />
                    )
                }
                else {
                    allLocalAuthorityProfiles.push(
                        <LocalAuthorityProfile
                            key={profile.uuid}
                            profile={profile}
                            isDefaultOpen={false}
                            onChange={this.onChange}
                            authContext={this.props.authContext}
                            updateProfile={this.updateProfile}
                            allNotifications={allNotifications}
                            onCheckboxChange={this.onCheckboxChange}
                            onLocalAuthorityNotificationsChange={this.onLocalAuthorityNotificationsChange}
                        />
                    )
                }
            })
        }

        return (
            <Page title={t('profile.title')}>
                {agent && (
                    <Fragment>
                        <Segment style={{ borderTop: `2px solid ${BRAND_COLOR}` }}>
                            <FieldInline htmlFor="family_name" label={t('agent.family_name')}>
                                <FieldValue id="family_name">{agent.family_name}</FieldValue>
                            </FieldInline>
                            <FieldInline htmlFor="given_name" label={t('agent.given_name')}>
                                <FieldValue id="given_name">{agent.given_name}</FieldValue>
                            </FieldInline>
                            <FieldInline htmlFor="email" label={t('agent.email')}>
                                <FieldValue id="email">{agent.email}</FieldValue>
                            </FieldInline>
                            {!this.props.uuid && (
                                <div style={{ textAlign: 'right' }}>
                                    <a href="/api/api-gateway/ozwillo-portal/my/profile" target="_blank" className="ui button"
                                        style={{
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            color: BRAND_COLOR,
                                            boxShadow: `0 0 0 1px ${BRAND_COLOR}`,
                                            background: 'transparent none'
                                        }}
                                    >
                                        <img style={{ height: '1.5em', float: 'left', marginRight: '1em' }} src={process.env.PUBLIC_URL + '/img/logo_sictiam.ico'}
                                            alt="SICTIAM" />
                                        {t('profile.modify_my_profile')}
                                    </a>
                                </div>
                            )}
                        </Segment>

                        {this.props.uuid &&
                            <Segment style={{ borderTop: `2px solid ${BRAND_COLOR}` }}>
                                <AgentProfile uuid={this.props.uuid}/>
                            </Segment>
                        }
                        {allLocalAuthorityProfiles}

                        {!this.props.uuid && (
                            <CertificateInfos pairedCertificate={agent.certificate}
                                onPairCertification={this.onPairCertification}/>
                        )}
                    </Fragment>
                )}
            </Page>
        )
    }
}

class LocalAuthorityProfile extends Component {
    static contextTypes = {
        t: PropTypes.func,
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        _fetchWithAuthzHandling: PropTypes.func,
        _addNotification: PropTypes.func
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
            daysToValidated: 15
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
        const { _fetchWithAuthzHandling } = this.context
        const { profile } = this.props
        if (profile.uuid && profile.localAuthority.activatedModules.includes('PES')) {
            const headers = { 'Content-Type': 'application/json' }
            const sesileConf = this.state.sesileConfiguration
            sesileConf.profileUuid = profile.uuid
            const url = '/api/pes/sesile/configuration'
            _fetchWithAuthzHandling({ url, body: JSON.stringify(sesileConf), headers, method: 'POST', context: this.props.authContext })
                .then(checkStatus)
                .catch(response => {
                    response.text().then(text =>
                        this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text)
                    )
                })
        }
    }
    fetchSesileInformation = () => {
        const { _fetchWithAuthzHandling } = this.context
        const { profile } = this.props
        _fetchWithAuthzHandling({ url: `/api/pes/sesile/organisations/${profile.localAuthority.uuid}/${profile.uuid}` })
            .then(response => response.json())
            .then(json => {
                this.setState({ serviceOrganisationAvailable: json })
            })
            .catch(response => {
                response.text().then(text =>
                    this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text)
                )
            })
        _fetchWithAuthzHandling({ url: `/api/pes/sesile/configuration/${profile.uuid}` })
            .then(response => response.json())
            .then(json => {
                this.setState({ sesileConfiguration: json })
            })
            .catch(response => {
                response.text().then(text =>
                    this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text)
                )
            })
    }
    fetchSesileSubscription = (profile) => {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: `/api/pes/sesile/subscription/${profile.localAuthority.uuid}` })
            .then(response => response.text())
            .then(text => {
                const sesileSub = text === 'true'
                this.setState({ sesileSubscription: sesileSub })
                if (sesileSub) this.fetchSesileInformation()
            })
            .catch(response => {
                this.props.authContext._addNotification(notifications.defaultError, 'notifications.pes.title', response.message)
            })
    }
    componentDidMount() {
        const { profile } = this.props
        if(profile && profile.localAuthority && profile.localAuthority.uuid) {
            this.fetchSesileSubscription(profile)
        }
    }
    componentDidUpdate(prevProps, prevState) {
        // QuickFix
        // context sometimes doen't load in ComponentDidMount
        const { profile } = this.props
        if(profile && profile.uuid && !Object.is(profile, prevProps.profile)) {
            this.fetchSesileSubscription(profile)
        }
    }
    render() {
        const { t } = this.context
        const { profile, isDefaultOpen, allNotifications, onChange, updateProfile, onCheckboxChange, onLocalAuthorityNotificationsChange } = this.props
        const modulesRows = modules.map(moduleName => (
            <Label key={moduleName} style={{ marginRight: '1em' }} color={profile.localAuthority.activatedModules.includes(moduleName) ? 'green' : 'red'}>
                {t(`modules.${moduleName}`)}
            </Label>
        ))
        const visibilities = sesileVisibility.map(visibility => {
            return {
                key: visibility,
                value: visibility,
                text: t(`profile.sesile.visibilities.${visibility}`)
            }
        })
        const serviceOrganisations = this.state.serviceOrganisationAvailable.map(service => {
            return {
                key: service.id,
                value: parseInt(service.id, 10),
                text: service.nom
            }
        })
        const currentService = this.state.sesileConfiguration.serviceOrganisationNumber
            ? this.state.serviceOrganisationAvailable.find(service => parseInt(service.id, 10) === this.state.sesileConfiguration.serviceOrganisationNumber)
            : undefined
        const typesAvailable = currentService
            ? currentService.types.map(type => {
                return { key: type.id, value: type.id, text: type.nom }
            })
            : []
        const sesileConnection = profile.localAuthority.activatedModules.map(
            activatedModule =>
                (activatedModule === 'PES' && this.state.sesileSubscription && this.state.sesileConfiguration) && (
                    <div key={activatedModule} style={{ marginTop: '1em' }}>
                        <Header as="h3" dividing>
                            {t('profile.sesile.title')}
                        </Header>
                        <FieldInline htmlFor="serviceOrganisationNumber" label={t('profile.sesile.serviceOrganisationNumber')}>
                            <Dropdown
                                compact
                                search
                                selection
                                id="serviceOrganisationNumber"
                                className="simpleInput"
                                placeholder={t('profile.sesile.serviceOrganisationNumber')}
                                options={serviceOrganisations}
                                value={this.state.sesileConfiguration.serviceOrganisationNumber}
                                onChange={this.sesileConfigurationChange} />
                        </FieldInline>
                        <FieldInline htmlFor="type" label={t('profile.sesile.type')}>
                            <Dropdown
                                compact
                                search
                                selection
                                id="type"
                                placeholder={t('profile.sesile.type')}
                                className="simpleInput"
                                options={typesAvailable}
                                value={this.state.sesileConfiguration.type}
                                onChange={this.sesileConfigurationChange} />
                        </FieldInline>
                        <FieldInline htmlFor="visibility" label={t('profile.sesile.visibility')}>
                            <Dropdown
                                compact
                                search
                                selection
                                id="visibility"
                                placeholder={t('profile.sesile.visibility')}
                                className="simpleInput"
                                options={visibilities}
                                value={this.state.sesileConfiguration.visibility}
                                onChange={this.sesileConfigurationChange} />
                        </FieldInline>
                        <FieldInline htmlFor="daysToValidated" label={t('profile.sesile.validationLimit')}>
                            <Input
                                id="daysToValidated"
                                type="number"
                                placeholder={t('profile.sesile.validationLimit')}
                                value={this.state.sesileConfiguration.daysToValidated}
                                onChange={this.sesileConfigurationChange} />
                        </FieldInline>
                    </div>
                )
        )
        const profileNotifications = profile.localAuthority.activatedModules.map(
            activatedModule => (
                <div style={{ marginTop: '2em' }} key={activatedModule}>
                    <Header as="h3" dividing>
                        {t('profile.notifications_title')} {activatedModule}
                    </Header>
                    <FieldInline htmlFor={activatedModule} label={t(`profile.localAuthorityNotifications${activatedModule}`)}>
                        <Checkbox toggle id={activatedModule} checked={profile.localAuthorityNotifications.includes(activatedModule)}
                            onChange={(e, { id, checked }) => onLocalAuthorityNotificationsChange(profile.uuid, id, checked)} />
                    </FieldInline>
                    {allNotifications
                        .filter(notification => notification.type.startsWith(`${activatedModule}_`))
                        .map(notification => {
                            const notificationValue = profile.notificationValues.find(notif => notif.name === notification.type)
                            return (
                                <FieldInline key={`${profile.uuid}-${notification.type}`} htmlFor={`${profile.uuid}-${notification.type}`}
                                    label={t(`profile.notifications.${notification.type}`)}>
                                    <Checkbox toggle id={`${profile.uuid}-${notification.type}`}
                                        checked={!notification.deactivatable || (
                                            notificationValue ? notificationValue.active : notification.defaultValue
                                        )}
                                        disabled={!notification.deactivatable}
                                        onChange={(e, { checked }) => onCheckboxChange(profile.uuid, notification.type, checked)} />
                                </FieldInline>
                            )
                        })
                    }
                </div>
            )
        )
        const content = (
            <div>
                <FieldInline htmlFor="modules" label={t('agent.modules')}>
                    <span id="modules">{modulesRows}</span>
                </FieldInline>
                <FieldInline htmlFor="email" label={t('agent.email')}>
                    <Input id="email" fluid style={{ maxWidth: '25em' }} value={profile.email || ''} placeholder={t('profile.no_email')}
                        onChange={(e, { id, value }) => onChange(profile.uuid, id, value)} />
                </FieldInline>
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
            </div>
        )
        return (
            <AccordionSegment
                title={profile.localAuthority.name}
                isDefaultOpen={isDefaultOpen}
                content={content}
            />
        )
    }
}

const UserProfile = translate(['api-gateway'])(withAuthContext(Profile))

const AdminProfile = translate(['api-gateway'])(withAuthContext(Profile))

export { UserProfile, AdminProfile }
