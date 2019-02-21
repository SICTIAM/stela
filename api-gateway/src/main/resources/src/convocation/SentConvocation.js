import React, { Component, Fragment } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Segment, Grid, Button, Icon } from 'semantic-ui-react'

import { notifications } from '../_util/Notifications'
import { getLocalAuthoritySlug, checkStatus, convertDateBackFormatToUIFormat } from '../_util/utils'
import history from '../_util/history'

import { withAuthContext } from '../Auth'
import { Page } from '../_components/UI'
import Breadcrumb from '../_components/Breadcrumb'

import StelaTable from '../_components/StelaTable'
import Participants from './Participants'
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
	        assemblyType: {},
	        location: '',
	        comment: '',
	        annexes: [],
	        attachment: null,
	        questions: [],
	        recipientResponses: [],
	        sentDate: null,
	        subject: '',
	        showAllAnnexes: false,
	        cancelled: false
	    }
	}

	componentDidMount() {
	    this.fetchConvocation()
	}

	fetchConvocation = () => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const uuid = this.props.uuid
	    _fetchWithAuthzHandling({ url: `/api/convocation/${uuid}`, method: 'GET' })
	        .then(checkStatus)
	        .then(response => response.json())
	        .then(json => {
	            this.setState({convocation: json})
	        })
	        .catch(response => {
	            response.json().then(json => {
	                _addNotification(notifications.defaultError, 'notifications.title', json.message)
	            })
	        })
	}

	sumRecipientsByStatus = (answer) => {
	    return this.state.convocation.recipientResponses.reduce( (acc, curr) => {
	        return curr['responseType'] === answer ? acc + 1: acc
	    }, 0)
	}

	greyResolver = (participant) => {
	    return !participant.opened
	}

	onCancelConvocation = () => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = this.context

	    _fetchWithAuthzHandling({url: `/api/convocation/${this.props.uuid}/cancel`, method: 'PUT', context: this.props.authContext})
	        .then(checkStatus)
	        .then(() => {
	            _addNotification(notifications.convocation.cancel)
	            this.fetchConvocation()
	        })
	        .catch((error) => {
	            if(error.body) {
	            	error.text().then(text => {
	            		_addNotification(notifications.defaultError, 'notifications.title', t(`${text}`))
	            	})
	            } else {
	            	_addNotification(notifications.defaultError, 'notifications.title', t(`convocation.errors.${error.status}`))
	            }
	        })
	}

	onCompleteConvocation = () => {
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    history.push(`/${localAuthoritySlug}/convocation/liste-envoyees/${this.props.uuid}/completer`)
	}

	render() {
	    const { t } = this.context
	    const { convocation } = this.state

	    const presents = this.sumRecipientsByStatus('PRESENT')
	    const absents = this.sumRecipientsByStatus('NOT_PRESENT')
	    const procurations = this.sumRecipientsByStatus('SUBSTITUTED')
	    const noAnswer = this.sumRecipientsByStatus('DO_NOT_KNOW')

	    const answerDisplay = (answer) => {
	        switch(answer) {
	        case 'PRESENT': return <p className='green text-bold'>{t('convocation.page.present')}</p>
	        case 'NOT_PRESENT': return <p className='red'>{t('convocation.page.absent')}</p>
	        case 'SUBSTITUTED': return <p className='red'>{t('convocation.page.substituted')}</p>
	        default: return ''
	        }
	    }
	    const statutDisplay = (opened) => opened ? <p><Icon name='envelope open'/> {convertDateBackFormatToUIFormat(opened, 'DD/MM/YYYY à HH:mm')}</p>: <Icon name='envelope'/>
	    const recipientDisplay = (recipient) => `${recipient.firstname} ${recipient.lastname}`
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'recipient', displayed: true, searchable: false, displayName: 'Destinataires', displayComponent: recipientDisplay },
	        { property: 'openDate', displayed: true, searchable: false, displayName: 'Statut', displayComponent: statutDisplay },
	        { property: 'responseType', displayed: true, searchable: false, displayName: 'Réponses', displayComponent: answerDisplay },
	    ]

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
	            <Segment>
	                {!this.state.convocation.cancelled && (
	                    <div className='float-right'>
	                        <Button type='button' className='mr-10' basic primary onClick={this.onCompleteConvocation}>{t('convocation.page.to_complete')}</Button>
	                        <Button type='button' basic color={'orange'} onClick={this.onCancelConvocation}>{t('api-gateway:form.cancel')}</Button>
	                    </div>
	                )}
	                <SentConvocationFragment convocation={convocation}/>
	                <h2>{t('convocation.page.participants')}</h2>
	                <Grid columns='1'>
 	                    <Grid.Column>
 	                        <p className='mb-0'>
 	                            {presents > 0 && (
 	                                <Fragment>
 	                                    {t('convocation.page.numberPresents',{'number': presents})}
 	                                </Fragment>
 	                            )}
 	                            {procurations > 0 && (
 	                                <Fragment>
 	                                    {(presents > 0) && (
 	                                        ', '
 	                                    )}
 	                                    {t('convocation.page.numberSubsituted',{'number': procurations})}
 	                                </Fragment>
 	                            )}
 	                            {absents > 0 && (
 	                                <Fragment>
 	                                    {(presents > 0 || procurations > 0) && (
 	                                        ', '
 	                                    )}
 	                                    {t('convocation.page.numberAbsents',{'number': absents})}
 	                                </Fragment>
 	                            )}
 	                        </p>
 	                        <p className='red'>
 	                            {noAnswer > 0 && (
 	                                <Fragment>
 	                                    {t('convocation.page.numberNoAnswer',{'number': noAnswer})}
 	                                </Fragment>
 	                            )}
 	                        </p>
 	                        <p>
 	                            {(presents > 0 || procurations > 0 || absents > 0) && (
 	                                <Button onClick={() => {
 	                                    this.setState({displayListParticipants: !this.state.displayListParticipants})}}
 	                                className="link" primary compact basic>
 	                                    { this.state.displayListParticipants ? t('convocation.page.hide_list') : t('convocation.page.see_list')}
 	                                </Button>
 	                            )}
 	                        </p>
 	                    </Grid.Column>
 	                    {this.state.displayListParticipants && (
 	                    	<Grid.Column className='pt-0'>
 	                            <Participants
 	                                participants={this.state.convocation.recipientResponses}
 	                                questions={this.state.convocation.questions}/>
 	                    	</Grid.Column>
 	                    )}
 	                </Grid>
					 <h2>{t('convocation.page.history')}</h2>
 	                <Grid columns='1'>
 	                    <Grid.Column>
 	                        <StelaTable
 	                            metaData={metaData}
 	                            data={this.state.convocation.recipientResponses}
 	                            header={true}
 	                            sortable={false}
 	                            greyResolver={this.greyResolver}
 	                            striped={false}
 	                            selectable= {false}
 	                            search={false}
 	                            keyProperty="uuid"
 	                            noDataMessage={t('convocation.new.no_recipient')}
 	                        ></StelaTable>
 	                    </Grid.Column>
 	                </Grid>
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(SentConvocation))