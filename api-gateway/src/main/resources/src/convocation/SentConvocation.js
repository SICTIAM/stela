import React, { Component, Fragment } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Segment, Button, Message, Dropdown } from 'semantic-ui-react'
import moment from 'moment'

import { notifications } from '../_util/Notifications'
import { getLocalAuthoritySlug } from '../_util/utils'
import history from '../_util/history'
import ConvocationService from '../_util/convocation-service'

import { withAuthContext } from '../Auth'
import { Page } from '../_components/UI'
import Breadcrumb from '../_components/Breadcrumb'
import ConfirmModal from '../_components/ConfirmModal'

import ParticipantsFragment from './_components/ParticipantsFragment'
import SentConvocationFragment from './_components/SentConvocationFragment'

class SentConvocation extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	state = {
	    displayListParticipants: false,
	    convocation: {
	        uuid: '',
	        meetingDate: '',
	        assemblyType: null,
	        location: '',
	        comment: '',
	        annexes: [],
	        attachment: null,
	        questions: [],
	        recipientResponses: [],
	        guestResponses: [],
	        sentDate: null,
	        subject: '',
	        showAllAnnexes: false,
	        cancelled: false
	    }
	}

	componentDidMount = async() => {
	    this._convocationService = new ConvocationService()
	    await this.fetchConvocation()
	}

	fetchConvocation = async () => {
	    const uuid = this.props.uuid
	    const convocationResponse = await this._convocationService.getSentConvocation(this.context, uuid)

	    //clone recipientResponses (contains guest and recipient response)
	    const recipientResponse =  convocationResponse.recipientResponses.slice()
	    //filter on recipient
	    convocationResponse.recipientResponses = recipientResponse.filter((response) => {
	        return response.guest === false
	    })
	    //filter on guest
	    convocationResponse.guestResponses = recipientResponse.filter((response) => {
	        return response.guest === true
	    })
	    this.setState({convocation: convocationResponse})
	}

	onCancelConvocation = async () => {
	    const {  _addNotification } = this.context
	    await this._convocationService.cancelConvocation(this.context, this.props.uuid)
	    _addNotification(notifications.convocation.cancel)
	    this.fetchConvocation()
	}

	onCompleteConvocation = () => {
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    history.push(`/${localAuthoritySlug}/convocation/liste-envoyees/${this.props.uuid}/completer`)
	}

	render() {
	    const { t } = this.context
	    const { convocation } = this.state

	    const localAuthoritySlug = getLocalAuthoritySlug()
	    return (
	        <Page>
	            <Breadcrumb
	                    data={[
	                        {title: t('api-gateway:breadcrumb.home'), url: `/${localAuthoritySlug}`},
	                        {title: t('api-gateway:breadcrumb.convocation.convocation')},
	                        {title: t('api-gateway:breadcrumb.convocation.sent_convocations_list'), url: `/${localAuthoritySlug}/convocation/liste-envoyees`},
	                        {title: t('api-gateway:breadcrumb.convocation.sent_convocation')},
	                    ]}
	            />
	            {this.state.convocation.cancelled && (
	                <Message warning>
	                    <Message.Header style={{ marginBottom: '0.5em'}}>{t('convocation.page.cancelled_convocation_title')}</Message.Header>
	                    <p>{t('convocation.page.cancelled_convocation_text', {date: moment(this.state.convocation.cancellationDate).format('DD/MM/YYYY')})}</p>
	                </Message>
	            )}
	            <Segment>
	                <div className='float-right'>
	                    <Dropdown basic direction='left' trigger={dropdownButton} icon={false} className='mr-10'>
	                        <Dropdown.Menu>
	                            <Dropdown.Item>
	                                <a className='item' aria-label={t('convocation.page.download_pdf')} href={`/api/convocation/${this.state.convocation.uuid}/presence.pdf`} target='_blank'>
	                                    {t('convocation.page.download_pdf')}
	                                </a>
	                            </Dropdown.Item>
	                            <Dropdown.Item>
	                                <a className='item' aria-label={t('convocation.page.download_csv')} href={`/api/convocation/${this.state.convocation.uuid}/presence.csv`} target='_blank'>
	                                    {t('convocation.page.download_csv')}
	                                </a>
	                            </Dropdown.Item>
	                        </Dropdown.Menu>
	                    </Dropdown>
	                    {!this.state.convocation.cancelled && (
	                        <Fragment>
	                            <Button type='button' className='mr-10' basic primary onClick={this.onCompleteConvocation}>{t('convocation.page.to_complete')}</Button>
	                            <ConfirmModal onConfirm={this.onCancelConvocation} text={t('convocation.page.cancel_convocation', {number: this.state.delay})}>
	                                <Button type='button' basic color={'orange'}>{t('api-gateway:form.cancel')}</Button>
	                            </ConfirmModal>
	                        </Fragment>
	                    )}
	                </div>
	                <SentConvocationFragment convocation={convocation}/>
	                <ParticipantsFragment
	                    title={t('convocation.fields.recipient')}
	                    participantResponses={convocation.recipientResponses}
	                    questions={convocation.questions}
	                />
	                <ParticipantsFragment
	                    title={t('convocation.fields.guest')}
	                    participantResponses={convocation.guestResponses}
	                    questions={convocation.questions}
	                />
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(SentConvocation))