import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Loader, Button } from 'semantic-ui-react'

import history from '../../_util/history'
import CustomDropzone from '../../_components/CustomDropzone'
import { notifications } from '../../_util/Notifications'
import { checkStatus, getLocalAuthoritySlug } from '../../_util/utils'
import { Page, LoadingContent } from '../../_components/UI'
import CertificateInfosUI from '../../_components/CertificateInfosUI'
import { withAuthContext } from '../../Auth'

class LocalAuthorityCertificate extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        certificate: {},
        fetchStatus: '',
        fetchOnGoing: false
    }
    componentDidMount() {
        const { uuid } = this.props
        if(uuid) {
            this.setState({ fetchStatus: 'loading' })
            const url = '/api/admin/local-authority/certificates/' + uuid
            const { _fetchWithAuthzHandling } = this.context
            _fetchWithAuthzHandling({ url: url })
                .then(checkStatus)
                .then(response => response.json())
                .then(certificate => this.setState({ certificate, fetchStatus: 'fetched' }))
        }
    }
    onDrop = (acceptedFiles, rejectedFiles) => {
        this.setState({ fetchOnGoing: true })
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const data = new FormData()
        data.append('file', acceptedFiles[0])
        const localAuthoritySlug = getLocalAuthoritySlug()
        const url = `/api/admin/local-authority/${this.props.localAuthorityUuid || 'current'}/certificates`
        _fetchWithAuthzHandling({ url: url, body: data, method: 'POST', context: this.props.authContext })
            .then(checkStatus)
            .then(() => {
                history.push(`/${localAuthoritySlug}/admin/${this.props.localAuthorityUuid ? `collectivite/${this.props.localAuthorityUuid}` : 'ma-collectivite'}/`)
                _addNotification(notifications.admin.certificateCreated)
            })
            .catch(response => {
                response.json().then(json => _addNotification(notifications.defaultError, 'notifications.admin.title', json.message))
                this.setState({ fetchOnGoing: false })
            })
    }
    delete = () => {
        const { uuid } = this.props
        if(this.props.uuid) {
            const { _fetchWithAuthzHandling, _addNotification } = this.context
            const url = `/api/admin/local-authority/${this.props.localAuthorityUuid || 'current'}/certificates/` + uuid
            const localAuthoritySlug = getLocalAuthoritySlug()
            _fetchWithAuthzHandling({ url: url, method: 'DELETE', context: this.props.authContext })
                .then(checkStatus)
                .then(() => {
                    history.push(`/${localAuthoritySlug}/admin/${this.props.localAuthorityUuid ? `collectivite/${this.props.localAuthorityUuid}` : 'ma-collectivite'}/`)
                    _addNotification(notifications.admin.certificateDeleted)
                })
                .catch(response => {
                    response.json().then(json => _addNotification(notifications.defaultError, 'notifications.admin.title', json.message))
                })
        }
    }
    render() {
        const { t } = this.context
        const { fetchOnGoing } = this.state
        return (
            <Page title={t(`admin.local_authority.${this.props.uuid ? 'certificate' : 'new_certificate'}`)}>

                {this.props.uuid && (
                    <LoadingContent fetchStatus={this.state.fetchStatus}>
                        <Segment>
                            <CertificateInfosUI certificate={this.state.certificate} />
                            <div style={{ textAlign: 'right' }}>
                                <Button onClick={this.delete} compact basic color='red'>
                                    {t('form.delete')}
                                </Button>
                            </div>
                        </Segment>
                    </LoadingContent>
                )}
                {!this.props.uuid && (
                    <Segment>
                        {fetchOnGoing ?
                            <div style={{ marginTop: '1em', marginBottom: '1em' }}><Loader active /></div>
                            : <CustomDropzone title={t('admin.local_authority.drop_new_certificate')} onDrop={this.onDrop} multiple={false} />
                        }
                    </Segment>
                )}
            </Page>
        )
    }
}

export default translate(['api-gateway'])(withAuthContext(LocalAuthorityCertificate))