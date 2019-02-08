import React, { Component, Fragment } from 'react'
import { Segment, Grid, Button, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import moment from 'moment'

import { getLocalAuthoritySlug, checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'

import { Page, Field, FieldValue, LinkFile } from '../_components/UI'
import Breadcrumb from '../_components/Breadcrumb'

import QuestionsForm from './QuestionsForm'
import StelaTable from '../_components/StelaTable'
import Participants from './Participants'


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
	        showAllAnnexes: false
	    }
	}

	componentDidMount() {
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

	render() {
	    const { t } = this.context

	    const presents = this.sumRecipientsByStatus('PRESENT')
	    const absents = this.sumRecipientsByStatus('NOT_PRESENT')
	    const procurations = this.sumRecipientsByStatus('SUBSTITUTED')
	    const noAnswer = this.sumRecipientsByStatus('DO_NOT_KNOW')

	    const answerDisplay = (answer) => {
	        return answer === 'PRESENT' ? <p className='green text-bold'>{t('convocation.page.present')}</p> : answer === 'NOT_PRESENT' ? <p className='red'>{t('convocation.page.absent')}</p> : answer === 'SUBSITUTED' ? <p className='red'>{t('convocation.page.subisituted')}</p> : ''
	    }
	    const statutDisplay = (opened) => opened ? <p><Icon name='envelope open'/> {moment(opened, 'YYYY-MM-DDTHH:mm:ss').format('DD-MM-YYYY à HH:mm')}</p>: <Icon name='envelope'/>
	    const recipientDisplay = (recipient) => `${recipient.firstname} ${recipient.lastname}`
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'recipient', displayed: true, searchable: false, displayName: 'Destinataires', displayComponent: recipientDisplay },
	        { property: 'openDate', displayed: true, searchable: false, displayName: 'Statut', displayComponent: statutDisplay },
	        { property: 'responseType', displayed: true, searchable: false, displayName: 'Réponses', displayComponent: answerDisplay },
	    ]
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const annexesToDisplay = !this.state.showAllAnnexes && this.state.convocation.annexes && this.state.convocation.annexes.length > 3 ? this.state.convocation.annexes.slice(0,3) : this.state.convocation.annexes
	    const annexes = annexesToDisplay.map(annexe => {
	        return (
	            <div key={`div_${this.state.convocation.uuid}_${annexe.uuid}`}>
	                <LinkFile
	                    url={`/api/convocation/${this.state.convocation.uuid}/file/${annexe.uuid}`}
	                    key={`${this.state.convocation.uuid}_${annexe.uuid}`}
	                    text={annexe.filename}/>
	            </div>

	        )
	    })

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
	                <h2>{this.state.convocation.subject}</h2>
	                <Grid reversed='mobile tablet vertically'>
	                    <Grid.Column mobile='16' tablet='16' computer='12'>
	                        <Grid>
	                            <Grid.Column computer='16'>
	                                <Field htmlFor="comment" label={t('convocation.fields.comment')}>
	                                    <FieldValue id="comment">{this.state.convocation.comment}</FieldValue>
	                                </Field>
	                            </Grid.Column>
	                            {this.state.convocation.attachment && (
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor="document" label={t('convocation.fields.convocation_document')}>
	                                        <FieldValue id="document">
	                                            <LinkFile url={`/api/convocation/${this.state.convocation.uuid}/file/${this.state.convocation.attachment.uuid}`} text={this.state.convocation.attachment.filename} />
	                                        </FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            )}
	                            {this.state.convocation.annexes && this.state.convocation.annexes.length > 0 && (
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor='annexes' label={t('convocation.fields.annexes')}>
	                                        <FieldValue id='annexes'>
	                                            {annexes}
	                                            {this.state.convocation.annexes.length > 3 && (
	                                                <div className='mt-15'>
	                                                    <Button onClick={() => this.setState({showAllAnnexes: !this.state.showAllAnnexes})} className="link" primary compact basic>
	                                                        {this.state.showAllAnnexes && (
	                                                            <span>{t('convocation.new.show_less_annexes')}</span>
	                                                        )}
	                                                        {!this.state.showAllAnnexes && (
	                                                            <span>{t('convocation.new.show_all_annexes', {number: this.state.convocation.annexes.length})}</span>
	                                                        )}
	                                                    </Button>
	                                                </div>
	                                            )}
	                                        </FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            )}
	                            {this.state.convocation.questions.length > 0 && (
	                                <Grid.Column computer='16' tablet='16'>
	                                    <Field htmlFor="questions" label={t('convocation.fields.questions')}>
	                                        <QuestionsForm
	                                            editable={false}
	                                            questions={this.state.convocation.questions}
	                                        />
	                                    </Field>
	                                </Grid.Column>
	                            )}
	                        </Grid>
	                    </Grid.Column>
	                    <Grid.Column mobile='16' computer='4'>
						 	<div className='block-information'>
	                            <Grid columns='1'>
	                                <Grid.Column>
	                                    <Field htmlFor="meetingDate" label={t('convocation.fields.date')}>
	                                        <FieldValue id="meetingDate">{moment(this.state.convocation.meetingDate, 'YYYY-MM-DDTHH:mm:ss').format('DD-MM-YYYY à HH:mm')}</FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                                <Grid.Column>
	                                    <Field htmlFor="assemblyType" label={t('convocation.fields.assembly_type')}>
	                                        <FieldValue id="assemblyType">{this.state.convocation.assemblyType && this.state.convocation.assemblyType.name}</FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                                <Grid.Column>
	                                    <Field htmlFor="location" label={t('convocation.fields.assembly_place')}>
	                                        <FieldValue id="location">{this.state.convocation.location}</FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            </Grid>
	                        </div>
	                    </Grid.Column>
	                </Grid>
	                <h2>{t('convocation.page.sent')}</h2>
	                <Grid columns='3'>
	                    <Grid.Column mobile='16' tablet='8' computer='6'>
	                        <Field htmlFor="transmitterGroup" label={t('convocation.page.group_sender')}>
	                            <FieldValue id="transmitterGroup">{this.state.convocation.transmitterGroup}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                    <Grid.Column mobile='16' tablet='8' computer='6'>
	                        <Field htmlFor="sendBy" label={t('convocation.page.send_by')}>
	                            <FieldValue id="sendBy">{this.state.convocation.sendBy}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                    <Grid.Column mobile='16' computer='4'>
	                        <Field htmlFor="sentDate" label={t('convocation.list.sent_date')}>
	                            <FieldValue id="sentDate">{moment(this.state.convocation.sentDate, 'YYYY-MM-DDTHH:mm:ss').format('DD-MM-YYYY à HH:mm')}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                </Grid>
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
	                                    { this.state.displayListParticipants ? 'Masquer la liste...' : 'Voir la liste'}
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

export default translate(['convocation', 'api-gateway'])(SentConvocation)