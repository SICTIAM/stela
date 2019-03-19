/* eslint-disable default-case */
import React, { Component } from 'react'
import { Segment, Grid, Button, Radio, Form, Message } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import moment from 'moment'

import { withAuthContext } from '../Auth'
import { getLocalAuthoritySlug } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import ConvocationService from '../_util/convocation-service'

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
	        this.setState({displayListParticipantsSubstituted: true})
	    }
	    this.setState({convocation: convocationResponse})
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
	        } else {
	            this.setState({displayListParticipantsSubstituted: true})
	        }
	        break
	    }
	    this.setState({convocation})
	}

	onSelectedUser = async (userUuid) => {
	    const { _addNotification } = this.context
	    await this._convocationService.savePresentResponse(this.context, this.props.uuid, {response: 'SUBSTITUTED', userUuid: userUuid}, this.props.location.search)
	    _addNotification(notifications.convocation.reponseSent)
	}

	negativeResolver = (recipients) => {
	    return recipients.response === 'NOT_PRESENT' || recipients.response === 'SUBSTITUTED'
	}

	positiveResolver = (recipients) => {
	    return recipients.response === 'PRESENT'
	}

	render() {
	    const { t } = this.context
	    const { convocation } = this.state
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
	    if(convocation.attachment) {
	        urlDocument = token ? `/api/convocation/${convocation.uuid}/file/${convocation.attachment.uuid}?stamped=true&token=${token.token}` : `/api/convocation/${convocation.uuid}/file/${convocation.attachment.uuid}?stamped=true`
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
	            <Form className='mt-14'>
	            	<Segment>
	                    <h2>{this.state.convocation.subject}</h2>
	                    <Grid reversed='mobile tablet vertically'>
	                        <Grid.Column mobile='16' tablet='16' computer='12'>
	                            <Grid>
	                                {this.state.convocation.comment && (
	                                <Grid.Column computer='16'>
	                                    <Field htmlFor="comments" label={t('convocation.fields.comment')}>
	                                            <FieldValue id="comments">
	                                                {convocation.comment.split('\n').map((item) => {
	                                                    return (
	                                                        <span>
	                                                            {item}
	                                                            <br/>
	                                                        </span>
	                                                    )
	                                                })}
	                                            </FieldValue>
	                                    </Field>
	                                </Grid.Column>)}
	                                {this.state.convocation.attachment && (
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor="document" label={t('convocation.fields.convocation_document')}>
	                                        <FieldValue id="document">
	                                            <LinkFile url={urlDocument} text={this.state.convocation.attachment.filename} />
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
	                            {(this.state.convocation.procuration || this.state.convocation.localAuthority.defaultProcuration) && !this.state.convocation.guest && this.state.convocation.useProcuration && (
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
	                        <InformationBlockConvocation convocation={this.state.convocation}/>
	                    </Grid>
	                </Segment>
	                <SenderInformation convocation={this.state.convocation}/>
	                <Segment>
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
	                        {!this.state.convocation.guest && this.state.convocation.useProcuration && (
	                            <Radio
	                                label={t('convocation.page.substituted')}
	                                name='presentQuestion'
	                                value='SUBSTITUTED'
	                                checked={this.state.convocation.response === 'SUBSTITUTED'}
	                                disabled={this.state.convocation.cancelled}
	                                onChange={(e, {value}) => this.handleChangeRadio(e, value, 'response')}
	                        	></Radio>
	                        )}
	                    </FormFieldInline>
	                    {this.state.displayListParticipantsSubstituted && (
	                        <div>
	                            {(this.state.convocation.procuration || this.state.convocation.localAuthority.defaultProcuration) && (
	                                <p className='warning text-bold'>{t('convocation.page.print_and_complete_procuration')} <LinkFile url={urlProcuration} text={t('convocation.page.download_procuration')} /></p>
	                            )}
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
	                </Segment>
	                    {this.state.convocation.questions.length > 0 && (
	                        <Segment>
	                            <h2>{t('convocation.fields.questions')}</h2>
	                            <Grid column='1'>
	                                <Grid.Column mobile='16' computer='16'>
	                                    <QuestionsAnswerForm
	                                        disabled={this.state.convocation.cancelled}
	                                        questions={this.state.convocation.questions}
	                                        handleChangeRadio={(e, value, uuid) => this.handleChangeRadio(e, value, 'additional_questions', uuid)}></QuestionsAnswerForm>
	                                </Grid.Column>
	                            </Grid>
	                        </Segment>
	                    )}
	            </Form>
	        </Page>
	    )
	}

}

export default translate(['convocation', 'api-gateway'])(withAuthContext(ReceivedConvocation))