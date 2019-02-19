/* eslint-disable default-case */
import React, { Component, Fragment } from 'react'
import { Segment, Grid, Button, Radio, Form, Message } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import moment from 'moment'

import { withAuthContext } from '../Auth'
import { getLocalAuthoritySlug, checkStatus, convertDateBackFormatToUIFormat } from '../_util/utils'
import { notifications } from '../_util/Notifications'

import { Page, Field, FieldValue, FormFieldInline, LinkFile } from '../_components/UI'
import Breadcrumb from '../_components/Breadcrumb'
import QuestionsAnswerForm from './QuestionsAnswerForm'


class ReceivedConvocation extends Component {
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
	        assemblyType: '',
	        location: '',
	        comment: '',
	        annexes: [],
	        response: null,
	        attachment: null,
	        questions: [],
	        sentDate: null,
	        profile: {},
	        subject: '',
	        showAllAnnexes: false,
	        cancelled: false,
	        cancellationDate: ''
	    }
	}

	componentDidMount() {
	    this.fetchConvocation()
	}

	fetchConvocation = () => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const uuid = this.props.uuid
	    _fetchWithAuthzHandling({ url: `/api/convocation/received/${uuid}`, method: 'GET' })
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

	handleChangeRadio = (e, value, field, question_uuid) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = this.context

	    const { convocation } = this.state
	    var url = ''
	    /*
			response: for presence or not
			additional_questions: for additional questions responses. field name -> questions (contains questions list and answer)
		*/
	    switch(field) {
	    case 'additional_questions':
	        var questionIndex = convocation.questions.findIndex((question) => {
	            return question.uuid === question_uuid
	        })
	        convocation.questions[questionIndex].response = value === 'true'
	        url = `/api/convocation/received/${this.props.uuid}/question/${question_uuid}/${value}`
	        break
	    case 'response':
	        convocation.response = value
	        url = `/api/convocation/received/${this.props.uuid}/${value}`
	        break
	    }

	    _fetchWithAuthzHandling({url: url, method: 'PUT', context: this.props.authContext})
	        .then(checkStatus)
	        .then(() => {
	            _addNotification(notifications.convocation.reponseSent)
	        })
	        .catch((error) => {
	            if(error.body) {
	            	error.text().then(text => {
	            		_addNotification(notifications.defaultError, 'notifications.title', t(`${text}`))
	            	})
	            } else {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`convocation.errors.${error.status}`))
	            }
	            this.fetchConvocation()
	        })
	    this.setState({convocation})
	}

	sumParticipantsByStatus = (answer) => {
	    return this.state.convocation.participants.reduce( (acc, curr) => {
	        return curr['answer'] === answer ? acc + 1: acc
	    }, 0)
	}

	greyResolver = (participant) => {
	    return !participant.opened
	}

	render() {
	    const { t } = this.context
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
	                    {title: t('api-gateway:breadcrumb.convocation.reveived_convocations_list'), url: `/${localAuthoritySlug}/convocation/liste-recues`},
	                    {title: t('api-gateway:breadcrumb.convocation.reveived_convocation')},
	                ]}
	            />
	            {this.state.convocation.cancelled && (
	                <Message warning>
	                    <Message.Header style={{ marginBottom: '0.5em'}}>{t('convocation.page.cancelled_convocation_title')}</Message.Header>
	                    <p>{t('convocation.page.cancelled_convocation_text', {date: moment(this.state.convocation.cancellationDate).format('DD/MM/YYYY')})}</p>
	                </Message>
	            )}
	            <Segment>
	                <Form>
	                    <h2>{this.state.convocation.subject}</h2>
	                    <Grid reversed='mobile tablet vertically'>
	                        <Grid.Column mobile='16' tablet='16' computer='12'>
	                            <Grid>
	                                {this.state.convocation.comment && (
	                                <Grid.Column computer='16'>
	                                    <Field htmlFor="comments" label={t('convocation.fields.comment')}>
	                                        <FieldValue id="comments">{this.state.convocation.comment}</FieldValue>
	                                    </Field>
	                                </Grid.Column>)}
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
	                                                    <Button type='button' onClick={() => this.setState({showAllAnnexes: !this.state.showAllAnnexes})} className="link" primary compact basic>
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
	                            </Grid>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='4'>
	                            <div className='block-information'>
	                                <Grid columns='1'>
	                                    <Grid.Column>
	                                        <Field htmlFor="Date" label={t('convocation.fields.date')}>
	                                            <FieldValue id="Date">{convertDateBackFormatToUIFormat(this.state.convocation.meetingDate, 'DD/MM/YYYY à HH:mm')}</FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                    <Grid.Column>
	                                        <Field htmlFor="assemblyType" label={t('convocation.fields.assembly_type')}>
	                                            <FieldValue id="assemblyType">{this.state.convocation.assemblyType}</FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                    <Grid.Column>
	                                        <Field htmlFor="assemblyPlace" label={t('convocation.fields.assembly_place')}>
	                                            <FieldValue id="assemblyPlace">{this.state.convocation.location}</FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                </Grid>
	                            </div>
	                        </Grid.Column>
	                    </Grid>
	                    <h2>{t('convocation.page.sent')}</h2>
	                    <Grid columns='2'>
	                        <Grid.Column mobile='16' tablet='8' computer='6'>
	                            <Field htmlFor="sendBy" label={t('convocation.page.send_by')}>
	                                <FieldValue id="sendBy">{this.state.convocation.profile.firstname} {this.state.convocation.profile.lastname}</FieldValue>
	                            </Field>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='4'>
	                            <Field htmlFor="sendingDate" label={t('convocation.list.sent_date')}>
	                                <FieldValue id="sendingDate">{convertDateBackFormatToUIFormat(this.state.convocation.sentDate, 'DD/MM/YYYY à HH:mm')}</FieldValue>
	                            </Field>
	                        </Grid.Column>
	                    </Grid>
	                    <h2>{t('convocation.page.my_answer')}</h2>
	                    <FormFieldInline htmlFor='presentQuestion'
	                        label={t('convocation.page.present_question')}>
	                        <Radio
	                            label={t('convocation.page.present')}
	                            value='PRESENT'
	                            name='presentQuestion'
	                            checked={this.state.convocation.response === 'PRESENT'}
	                            disabled={this.state.convocation.cancelled}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'response')}
	                        ></Radio>
	                        <Radio
	                            label={t('convocation.page.absent')}
	                            value='NOT_PRESENT'
	                            name='presentQuestion'
	                            checked={this.state.convocation.response === 'NOT_PRESENT'}
	                            disabled={this.state.convocation.cancelled}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'response')}
	                        ></Radio>
	                        <Radio
	                            label={t('convocation.page.substituted')}
	                            name='presentQuestion'
	                            value='SUBSTITUTED'
	                            checked={this.state.convocation.response === 'SUBSTITUTED'}
	                            disabled={this.state.convocation.cancelled}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'response')}
	                        ></Radio>
	                    </FormFieldInline>
	                    {this.state.convocation.questions.length > 0 && (
	                        <Fragment>
	                            <h2>{t('convocation.fields.questions')}</h2>
	                            <Grid column='1'>
	                                <Grid.Column mobile='16' computer='16'>
	                                    <QuestionsAnswerForm
	                                        disabled={this.state.convocation.cancelled}
	                                        questions={this.state.convocation.questions}
	                                        handleChangeRadio={(e, value, uuid) => this.handleChangeRadio(e, value, 'additional_questions', uuid)}></QuestionsAnswerForm>
	                                </Grid.Column>
	                            </Grid>
	                        </Fragment>
	                    )}

	                </Form>
	            </Segment>
	        </Page>
	    )
	}

}

export default translate(['convocation', 'api-gateway'])(withAuthContext(ReceivedConvocation))