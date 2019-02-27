/* eslint-disable default-case */
import React, { Component, Fragment } from 'react'
import { Segment, Grid, Button, Radio, Form, Message } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import moment from 'moment'

import { withAuthContext } from '../Auth'
import { getLocalAuthoritySlug, checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'

import { Page, Field, FieldValue, FormFieldInline, LinkFile } from '../_components/UI'
import Breadcrumb from '../_components/Breadcrumb'
import QuestionsAnswerForm from './QuestionsAnswerForm'
import StelaTable from '../_components/StelaTable'
import InformationBlockConvocation from './_components/InformationBlock'
import SenderInformation from './_components/SenderInformation'


class ReceivedConvocation extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	state = {
	    displayListParticipants: false,
	    displayListParticipantsSubstituted: false,
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
	        cancellationDate: '',
	        procuration: null,
	        substitute: null
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
	            if(json.response === 'SUBSTITUTED') {
	                this.setState({displayListParticipantsSubstituted: true})
	            }
	            this.setState({convocation: json})
	        })
	        .catch(response => {
	            response.json().then(json => {
	                _addNotification(notifications.defaultError, 'notifications.title', json.message)
	            })
	        })
	}
	saveAdditionnalQuestionResponse = (url) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = this.context

	    if(url) {
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
	    }

	}

	handleChangeRadio = (e, value, field, question_uuid) => {
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
	        if(value !== 'SUBSTITUTED') {
	            url = `/api/convocation/received/${this.props.uuid}/${value}`
	            this.setState({displayListParticipantsSubstituted: false})
	        } else {
	            this.setState({displayListParticipantsSubstituted: true})
	        }
	        break
	    }
	    this.saveAdditionnalQuestionResponse(url)
	    this.setState({convocation})
	}

	onSelectedUser = (userUuid) => {
	    var url = `/api/convocation/received/${this.props.uuid}/SUBSTITUTED/${userUuid}`
	    this.saveAdditionnalQuestionResponse(url)
	}

	negativeResolver = (recipients) => {
	    return recipients.response === 'NOT_PRESENT' || recipients.response === 'SUBSTITUTED'
	}

	positiveResolver = (recipients) => {
	    return recipients.response === 'PRESENT'
	}

	render() {
	    const { t } = this.context
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const annexesToDisplay = !this.state.showAllAnnexes && this.state.convocation.annexes && this.state.convocation.annexes.length > 3 ? this.state.convocation.annexes.slice(0,3) : this.state.convocation.annexes
	    const annexes = annexesToDisplay.map(annexe => {
	        return (
	            <div key={`div_${this.state.convocation.uuid}_${annexe.uuid}`}>
	                <LinkFile
	                    url={`/api/convocation/${this.state.convocation.uuid}/file/${annexe.uuid}?stamped=true`}
	                    key={`${this.state.convocation.uuid}_${annexe.uuid}`}
	                    text={annexe.filename}/>
	            </div>

	        )
	    })

	    const metaData = [
	        { property: 'uuid', displayed: false, searchable: false },
	        { property: 'firstname', displayed: true, displayName: t('acte.fields.number'), searchable: true, sortable: true, collapsing: true },
	        { property: 'lastname', displayed: true, displayName: t('acte.fields.objet'), searchable: true, sortable: true }
	    ]

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
	                                            <LinkFile url={`/api/convocation/${this.state.convocation.uuid}/file/${this.state.convocation.attachment.uuid}?stamped=true`} text={this.state.convocation.attachment.filename} />
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
	                            {this.state.convocation.procuration && (
	                                <Grid.Column mobile='16' computer='16'>
	                                    <Field htmlFor='procuration' label={t('convocation.page.substituted')}>
	                                        <FieldValue id='procuration'>
	                                            <LinkFile url={`/api/convocation/${this.state.convocation.uuid}/file/${this.state.convocation.procuration.uuid}`} text={this.state.convocation.procuration.filename} />
	                                        </FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            )}
	                        	</Grid>
	                        </Grid.Column>
	                        <InformationBlockConvocation convocation={this.state.convocation}/>
	                    </Grid>
	                    <SenderInformation convocation={this.state.convocation}/>
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
	                    {this.state.displayListParticipantsSubstituted && (
	                        <div>
	                            <p className='text-bold mb-0'>{t('convocation.page.select_user_for_substituted')}</p>
	                            <p className='text-muted'>{t('convocation.page.information_message_substituted')}</p>
	                            <Grid>
	                                <Grid.Column mobile='16' computer='8'>
	                                    <StelaTable
	                                        data={this.state.convocation.recipients}
	                                        metaData={metaData}
	                                        containerTable='maxh-300 w-100'
	                                        header={false}
	                                        search={true}
	                                        noDataMessage={t('convocation.new.no_recipient')}
	                                        keyProperty='uuid'
	                                        uniqueSelect={true}
	                                        selectedRadio={this.state.convocation.substitute && this.state.convocation.substitute.uuid}
	                                        onSelectedRow={this.onSelectedUser}
	                                        negativeResolver={this.negativeResolver}
	                                        positiveResolver={this.positiveResolver}
	                                    />
	                                </Grid.Column>
	                            </Grid>

	                        </div>
	                    )}
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