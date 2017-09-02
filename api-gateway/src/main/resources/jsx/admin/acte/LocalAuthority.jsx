import React, { Component } from 'react'
import PropTypes from 'prop-types'
import renderIf from 'render-if'
import { translate } from 'react-i18next'
import { Grid, Checkbox, Segment } from 'semantic-ui-react'

import { errorNotification } from '../../_components/Notifications'
import InputButton from '../../_components/InputButton'

class LocalAuthority extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        localAuthority: {},
        localAuthorityFetched: false
    }
    componentDidMount() {
        const uuid = this.props.uuid
        if (uuid !== '') {
            fetch('/api/acte/localAuthority/' + uuid, { credentials: 'same-origin' })
                .then(this.checkStatus)
                .then(response => response.json())
                .then(json => {
                    this.setState({ localAuthority: json, localAuthorityFetched: true })
                })
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                    })
                    history.push('/admin/actes/parametrage-collectivite')
                })
        }
    }
    checkStatus = (response) => {
        if (response.status >= 200 && response.status < 300) {
            return response
        } else {
            throw response
        }
    }
    handleCheckboxChange = (field) => {
        const newValue = !this.state.localAuthority[field]
        this.updateChange(field, newValue)
    }
    handleChange = (e) => {
        const { id, value } = e.target
        this.updateChange(id, value)
    }
    updateChange = (field, newValue) => {
        const uuid = this.state.localAuthority.uuid
        fetch('/api/acte/localAuthority/' + uuid, {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                [this.context.csrfTokenHeaderName]: this.context.csrfToken
            },
            method: 'PATCH',
            body: JSON.stringify({ [field]: newValue })
        })
            .then(this.checkStatus)
            .then(response => response.json())
            .then(value => {
                const localAuthority = this.state.localAuthority
                let newFieldValue = value[field]
                localAuthority[field] = newFieldValue
                this.setState({ localAuthority: localAuthority })
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const localAuthorityFetched = renderIf(this.state.localAuthorityFetched)
        return (
            <div>
                {localAuthorityFetched(
                    <Segment>
                        <h1>{this.state.localAuthority.name}</h1>

                        <h2>{t('admin.modules.acte.local_authority_settings.general_informations')}</h2>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="uuid">{t('local_authority.uuid')}</label></Grid.Column>
                            <Grid.Column width={12}><span id="uuid">{this.state.localAuthority.uuid}</span></Grid.Column>
                        </Grid>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="siren">{t('local_authority.siren')}</label></Grid.Column>
                            <Grid.Column width={12}><span id="siren">{this.state.localAuthority.siren}</span></Grid.Column>
                        </Grid>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="department">{t('local_authority.department')}</label></Grid.Column>
                            <Grid.Column width={12}>
                                <InputButton id="department"
                                    value={this.state.localAuthority.department}
                                    handleChange={this.handleInputChange}
                                    validateInput={this.updateChange} />
                            </Grid.Column>
                        </Grid>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="district">{t('local_authority.district')}</label></Grid.Column>
                            <Grid.Column width={12}>
                                <InputButton id="district"
                                    value={this.state.localAuthority.district}
                                    handleChange={this.handleInputChange}
                                    validateInput={this.updateChange} />
                            </Grid.Column>
                        </Grid>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="nature">{t('local_authority.nature')}</label></Grid.Column>
                            <Grid.Column width={12}>
                                <InputButton id="nature"
                                    value={this.state.localAuthority.nature}
                                    handleChange={this.handleInputChange}
                                    validateInput={this.updateChange} />
                            </Grid.Column>
                        </Grid>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="nomenclatureDate">{t('local_authority.nomenclatureDate')}</label></Grid.Column>
                            <Grid.Column width={12}><span id="nomenclatureDate">{this.state.localAuthority.nomenclatureDate}</span></Grid.Column>
                        </Grid>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="canPublishRegistre">{t('local_authority.canPublishRegistre')}</label></Grid.Column>
                            <Grid.Column width={12}><Checkbox id="canPublishRegistre" toggle checked={this.state.localAuthority.canPublishRegistre} onChange={e => this.handleCheckboxChange('canPublishRegistre')} /></Grid.Column>
                        </Grid>
                        <Grid>
                            <Grid.Column width={4}><label htmlFor="canPublishWebSite">{t('local_authority.canPublishWebSite')}</label></Grid.Column>
                            <Grid.Column width={12}><Checkbox id="canPublishWebSite" toggle checked={this.state.localAuthority.canPublishWebSite} onChange={e => this.handleCheckboxChange('canPublishWebSite')} /></Grid.Column>
                        </Grid>
                    </Segment>
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthority)