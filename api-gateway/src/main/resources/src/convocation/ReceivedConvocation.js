/* eslint-disable default-case */
import React, { Component, Fragment } from 'react'
import { Segment, Grid, Button, Radio, Form, Message, Confirm } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import moment from 'moment'

import { withAuthContext } from '../Auth'
import { getLocalAuthoritySlug } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import ConvocationService from '../_util/convocation-service'

import { Page, Field, FieldValue, FormFieldInline, LinkFile } from '../_components/UI'
import Timeline from './_components/Timeline'
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
	    subsitutionModalOpened: false,
	    substitute: null,
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
	        substitute: null,
	        guest: true,
	        localAuthority: {}
	    }
	}

	componentDidMount = async() => {
	    this._convocationService = new ConvocationService()
	    await this.fetchConvocation()
	}

	fetchConvocation = async() => {
	    const uuid = this.props.uuid
	    const convocationResponse = await this._convocationService.getReceivedConvocation(this.context, uuid, this.props.location.search)
	    if(convocationResponse.response === 'SUBSTITUTED') {
	        this.setState({displayListParticipantsSubstituted: true, substitute: convocationResponse.substitute, convocation: convocationResponse})
	    } else {
	        this.setState({convocation: convocationResponse})
	    }
	}

	handleChangeRadio = async (e, value, field, question_uuid) => {
	    const { _addNotification } = this.context
	    const { convocation } = this.state
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
	        await this._convocationService.saveAdditionnalQuestionResponse(this.context, this.props.uuid, value, question_uuid, this.props.location.search)
	        _addNotification(notifications.convocation.reponseSent)
	        break
	    case 'response':
	        convocation.response = value
	        if(value !== 'SUBSTITUTED') {
	            await this._convocationService.savePresentResponse(this.context, this.props.uuid, {response: value}, this.props.location.search)
	            this.setState({displayListParticipantsSubstituted: false, substitute: null})
	            _addNotification(notifications.convocation.reponseSent)
	            convocation.substitute = null
	        } else {
	            this.setState({displayListParticipantsSubstituted: true})
	        }
	        break
	    }
	    this.setState({convocation})
	}

	onSelectedUser = async () => {
	    const { _addNotification } = this.context
	    await this._convocationService.savePresentResponse(this.context, this.props.uuid, {response: 'SUBSTITUTED', userUuid: this.state.substitute.uuid}, this.props.location.search)
	    await this.fetchConvocation()
	    this.setState({subsitutionModalOpened: false})
	    _addNotification(notifications.convocation.reponseSent)
	}
	openSubstituteModal = (user) => {
	    this.setState({substitute: user}, () => {this.setState({subsitutionModalOpened: true})})
	}
	closeSubstituteModal = () => {
	    this.setState({subsitutionModalOpened: false, substitute: this.state.convocation.substitute})
	}

	downloadAllDocuments = async() => {
	    await this._convocationService.downloadAllDocuments(this.props.authContext, this.props.uuid)
	}

	negativeResolver = (recipients) => {
	    return recipients.response === 'NOT_PRESENT' || recipients.response === 'SUBSTITUTED'
	}

	positiveResolver = (recipients) => {
	    return recipients.response === 'PRESENT'
	}

	render() {
	    const { t } = this.context
	    const { convocation, subsitutionModalOpened, substitute } = this.state
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const token = this._convocationService && this._convocationService.getTokenInUrl(this.props.location.search)
	    const annexesToDisplay = !this.state.showAllAnnexes && this.state.convocation.annexes && this.state.convocation.annexes.length > 3 ? this.state.convocation.annexes.slice(0,3) : this.state.convocation.annexes
	    const annexes = annexesToDisplay.map(annexe => {
	        const url = token ? `/api/convocation/${this.state.convocation.uuid}/file/${annexe.uuid}?stamped=true&token=${token.token}`:`/api/convocation/${this.state.convocation.uuid}/file/${annexe.uuid}?stamped=true`
	        return (
	            <div key={`div_${this.state.convocation.uuid}_${annexe.uuid}`}>
	                <LinkFile
	                    url={url}
	                    key={`${this.state.convocation.uuid}_${annexe.uuid}`}
	                    text={annexe.filename}/>
	            </div>

	        )
	    })
	    let urlDocument = null
	    if(convocation.attachment || convocation.minutes) {
	        urlDocument = token ? `/api/convocation/${convocation.uuid}/file/${convocation.attachment.uuid}?stamped=true&token=${token.token}` : `/api/convocation/${convocation.uuid}/file/${convocation.attachment.uuid}?stamped=true`
	    }

	    let urlMinutes = null
	    if (convocation.minutes) {
	        urlMinutes = token ? `/api/convocation/${convocation.uuid}/file/${convocation.minutes.uuid}?stamped=true&token=${token.token}` : `/api/convocation/${convocation.uuid}/file/${convocation.minutes.uuid}?stamped=true`
	    }

	    let urlProcuration = null
	    //if this convocation have procuration
	    if(convocation.procuration && convocation.procuration.uuid) {
	        urlProcuration = `/api/convocation/${convocation.uuid}/file/${this.state.convocation.procuration.uuid}`
	    } //else check if local authority have default procuration (defined in admin config)
	    else if (convocation.localAuthority && convocation.localAuthority.defaultProcuration) {
	        urlProcuration =  '/api/convocation/local-authority/procuration'
	    }
	    //if token, add in url
	    if(token && urlProcuration) {
	        urlProcuration = `${urlProcuration}?token=${token.token}`
	    }

	    const archiveUrl = token ? `/api/convocation/${convocation.uuid}/archive?token=${token.token}` : `/api/convocation/${convocation.uuid}/archive`

	    const metaData = [
	        { property: 'uuid', displayed: false, searchable: false },
	        { property: 'firstname', displayed: true, displayName: t('acte.fields.number'), searchable: true, sortable: true, collapsing: true },
	        { property: 'lastname', displayed: true, displayName: t('acte.fields.objet'), searchable: true, sortable: true }
	    ]

	    const now = moment()
	    const disabledResponses = convocation.cancellation || now > moment(convocation.meetingDate)

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
	            {convocation.cancelled && (
	                <Message warning>
	                    <Message.Header style={{ marginBottom: '0.5em'}}>{t('convocation.page.cancelled_convocation_title')}</Message.Header>
	                    <p>{t('convocation.page.cancelled_convocation_text', {date: moment(convocation.cancellationDate).format('DD/MM/YYYY')})}</p>
	                </Message>
	            )}
	            <Form className='mt-14'>
	            	<Segment>
	                    <h2>{convocation.subject}</h2>
	                    <Grid reversed='mobile tablet vertically'>
	                        <Grid.Column mobile='16' tablet='16' computer='12'>
	                            <Grid>
	                                {convocation.comment && (
	                                <Grid.Column computer='16'>
	                                    <Field htmlFor="comments" label={t('convocation.fields.comment')}>
	                                            <FieldValue id="comments">
	                                                {convocation.comment.split('\n').map((item, index) => {
	                                                    return (
	                                                        <span key={`comment_line_${index}`}>
	                                                            {item}
	                                                            <br/>
	                                                        </span>
	                                                    )
	                                                })}
	                                            </FieldValue>
	                                    </Field>
	                                </Grid.Column>)}
	                            	{convocation.attachment && (
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor="document" label={t('convocation.fields.convocation_document')}>
	                                        <FieldValue id="document">
	                                            <LinkFile url={urlDocument} text={convocation.attachment.filename} />
	                                        </FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            	)}
	                            	{convocation.annexes && convocation.annexes.length > 0 && (
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor='annexes' label={t('convocation.fields.annexes')}>
	                                        <FieldValue id='annexes'>
	                                            {annexes}
	                                            {convocation.annexes.length > 3 && (
	                                                <div className='mt-15'>
	                                                    <Button type='button' onClick={() => this.setState({showAllAnnexes: !this.state.showAllAnnexes})} className="link" primary compact basic>
	                                                        {this.state.showAllAnnexes && (
	                                                            <span>{t('convocation.new.show_less_annexes')}</span>
	                                                        )}
	                                                        {!this.state.showAllAnnexes && (
	                                                            <span>{t('convocation.new.show_all_annexes', {number: convocation.annexes.length})}</span>
	                                                        )}
	                                                    </Button>
	                                                </div>
	                                            )}
	                                        </FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                                )}
	                                {(convocation.attachment || (convocation.annexes && convocation.annexes.length > 0)) && (
	                                    <Grid.Column mobile='16' computer='16'>
	                                        <a className='ui basic compact primary button' href={archiveUrl}>{t('convocation.page.download_all_documents')}</a>
	                                    </Grid.Column>
	                                )}
	                                {convocation.minutes && (
	                                    <Grid.Column mobile='16' computer='8'>
	                                        <Field htmlFor='minutes' label={t('convocation.fields.minutes')}>
	                                            <FieldValue id="document">
	                                                <LinkFile url={urlMinutes} text={convocation.minutes.filename} />
	                                            </FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                )}
	                            	{(convocation.procuration || convocation.localAuthority.defaultProcuration) && !convocation.guest && convocation.useProcuration && (
	                                <Grid.Column mobile='16' computer='16'>
	                                    <Field htmlFor='procuration' label={t('convocation.page.substituted')}>
	                                        <FieldValue id='procuration'>
	                                            <LinkFile url={urlProcuration} text={t('convocation.page.download_procuration')} />
	                                        </FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            	)}
	                        	</Grid>
	                        </Grid.Column>
	                        <InformationBlockConvocation convocation={convocation}/>
	                    </Grid>
	                </Segment>
	                <SenderInformation convocation={convocation}/>
	                <Segment>
	                    <h2>{t('convocation.page.my_answer')}</h2>
	                    <FormFieldInline htmlFor='presentQuestion'
	                        label={t('convocation.page.present_question')}>
	                        <Radio
	                            label={t('convocation.page.present')}
	                            value='PRESENT'
	                            name='presentQuestion'
	                            checked={convocation.response === 'PRESENT'}
	                            disabled={disabledResponses}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'response')}
	                        ></Radio>
	                        <Radio
	                            label={t('convocation.page.absent')}
	                            value='NOT_PRESENT'
	                            name='presentQuestion'
	                            checked={convocation.response === 'NOT_PRESENT'}
	                            disabled={disabledResponses}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'response')}
	                        ></Radio>
	                        {!convocation.guest && convocation.useProcuration && (
	                            <Radio
	                                label={t('convocation.page.substituted')}
	                                name='presentQuestion'
	                                value='SUBSTITUTED'
	                                checked={convocation.response === 'SUBSTITUTED'}
	                                disabled={disabledResponses}
	                                onChange={(e, {value}) => this.handleChangeRadio(e, value, 'response')}
	                        	></Radio>
	                        )}
	                    </FormFieldInline>
	                    {this.state.displayListParticipantsSubstituted && (
	                        <div>
	                            {(this.state.convocation.procuration || this.state.convocation.localAuthority.defaultProcuration) && (
	                                <Fragment>
	                                    <p className='warning text-bold mb-0'>{t('convocation.page.print_and_complete_procuration')}</p>
	                                    <p className='warning text-bold'><LinkFile url={urlProcuration} text={t('convocation.page.download_procuration')} /></p>
	                                </Fragment>
	                            )}
	                            <p className='text-bold mb-0'>{t('convocation.page.select_user_for_substituted')}</p>
	                            <p className='text-muted'>{t('convocation.page.information_message_substituted')}</p>
	                            <Grid>
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Confirm
	                                        open={subsitutionModalOpened}
	                                        onCancel={this.closeSubstituteModal}
	                                        onConfirm={this.onSelectedUser}
	                                        content={t('convocation.page.substitute_confirmation',
	                                            {firstname: substitute && substitute.firstname,
	                                                lastname: substitute && substitute.lastname,
	                                                date: moment(convocation.meetingDate).format('DD/MM/YYYY à hh:mm'),
	                                                title: convocation.subject})}/>
	                                    <StelaTable
	                                        data={this.state.convocation.recipients}
	                                        metaData={metaData}
	                                        containerTable='maxh-300 w-100'
	                                        header={false}
	                                        search={true}
	                                        noDataMessage={t('convocation.new.no_recipient')}
	                                        keyProperty='uuid'
	                                        uniqueSelect={true}
	                                        selectedRadio={substitute && substitute.uuid}
	                                        onSelectedRow={this.openSubstituteModal}
	                                        negativeResolver={this.negativeResolver}
	                                        positiveResolver={this.positiveResolver}
	                                    />
	                                </Grid.Column>
	                            </Grid>

	                        </div>
	                    )}
	                </Segment>
	                    {this.state.convocation.questions.length > 0 && (
	                        <Segment>
	                            <h2>{t('convocation.fields.questions')}</h2>
	                            <Grid column='1'>
	                                <Grid.Column mobile='16' computer='16'>
	                                    <QuestionsAnswerForm
	                                        disabled={disabledResponses}
	                                        questions={this.state.convocation.questions}
	                                        handleChangeRadio={(e, value, uuid) => this.handleChangeRadio(e, value, 'additional_questions', uuid)}></QuestionsAnswerForm>
	                                </Grid.Column>
	                            </Grid>
	                        </Segment>
	                )}
	                {convocation.histories && (
	                    <Timeline
	                            title={t('convocation.page.history')}
	                            emptyMessage={t('convocation.page.no_history')}
	                            history={convocation.histories} />
	                )}
	            </Form>
	        </Page>
	    )
	}

}

export default translate(['convocation', 'api-gateway'])(withAuthContext(ReceivedConvocation))