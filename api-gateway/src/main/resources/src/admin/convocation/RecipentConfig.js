import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import moment from 'moment'
import { Segment, Message } from 'semantic-ui-react'

import { withAuthContext } from '../../Auth'

import { getLocalAuthoritySlug } from '../../_util/utils'
import history from '../../_util/history'
import ConvocationService from '../../_util/convocation-service'

import UserFormFragment from '../../convocation/_components/UserFormFragment'
import Breadcrumb from '../../_components/Breadcrumb'
import { Page } from '../../_components/UI'

class RecipentConfig extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _addNotification: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func
	}

	state = {
	    fields: {
	        uuid: null,
	        firstname: '',
	        lastname: '',
	        email: '',
	        phoneNumber: '',
	        active: true,
	        assemblyTypes: [],
	        inactivityDate: null,
	        epciName: ''
	    },
	    localAuthority: {
	        epci: false
	    }
	}
	componentDidMount = async () => {
	    const uuid = this.props.uuid
	    this._convocationService = new ConvocationService()

	    const localAuthorityResponse = await this._convocationService.getConfForLocalAuthority(this.props.authContext)

	    if(uuid) {
	        const recipientResponse = await this._convocationService.getRecipientByUuid(this.props.authContext, uuid)

	        const fields = this.state.fields
	                Object.keys(fields).forEach(function (key) {
	                    fields[key] = recipientResponse[key]
	                })
	        this.setState({fields, localAuthority: localAuthorityResponse}, this.validateForm)
	    } else {
	        this.setState({localAuthority: localAuthorityResponse}, this.validateForm)
	    }
	}

	createUrlApi = () => {
	    return '/api/convocation/recipient' + (this.state.fields.uuid ? `/${this.state.fields.uuid}` : '')
	}
	onCancel = () => {
	    history.goBack()
	}

	onSubmit = () => {
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    history.push(`/${localAuthoritySlug}/admin/convocation/destinataire/liste-destinataires`)
	}

	render () {
	    const { t } = this.context

	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const dataBreadcrumb = this.props.uuid ? [
	        {title: t('api-gateway:breadcrumb.admin_home'), url: `/${localAuthoritySlug}/admin/ma-collectivite`},
	        {title: t('api-gateway:breadcrumb.convocation.convocation'), url: `/${localAuthoritySlug}/admin/ma-collectivite/convocation`},
	        {title: t('api-gateway:breadcrumb.convocation.recipients_list'), url: `/${localAuthoritySlug}/admin/convocation/destinataire/liste-destinataires` },
	        {title: t('api-gateway:breadcrumb.convocation.edit_recipients') }
	    ] : [
	        {title: t('api-gateway:breadcrumb.admin_home'), url: `/${localAuthoritySlug}/admin/ma-collectivite`},
	        {title: t('api-gateway:breadcrumb.convocation.convocation'), url: `/${localAuthoritySlug}/admin/ma-collectivite/convocation`},
	        {title: t('api-gateway:breadcrumb.convocation.add_recipients') }
	    ]
	    return (
	        <Page>
	            <Breadcrumb
	                data={dataBreadcrumb}
	            />
	            {!this.state.fields.active && (
	                <Message warning>
	                    <Message.Header style={{ marginBottom: '0.5em'}}>{t('convocation.admin.modules.convocation.recipient_config.inactive_recipient_title')}</Message.Header>
	                    <p>{t('convocation.admin.modules.convocation.recipient_config.inactive_recipient_content', {date: moment(this.state.fields.inactivityDate).format('DD/MM/YYYY')})}</p>
	                </Message>
	            )}
	            <Segment>
	                <UserFormFragment
	                    onSubmit={this.onSubmit}
	                    onCancel={this.onCancel}
	                    fields={this.state.fields}
	                    epci={this.state.localAuthority.epci}/>
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(RecipentConfig))