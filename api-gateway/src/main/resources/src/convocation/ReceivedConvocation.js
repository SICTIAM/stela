import React, { Component, Fragment } from 'react'
import { Segment, Grid, Button, Radio, Form } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import moment from 'moment'

import { getLocalAuthoritySlug, checkStatus } from '../_util/utils'
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
	        assemblyType: {},
	        location: '',
	        comment: '',
	        annexes: [],
	        responses: null,
	        attachment: null,
	        questions: [],
	        recipientResponses: [],
	        sentDate: null,
	        profile: {},
	        subject: '',
	        showAllAnnexes: false
	    }
	}

	componentDidMount() {
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

	handleChangeRadio = (e, value, field) => {
	    const convocation = this.state.convocation
	    convocation[field] = value
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
	            <Segment>
	                <Form>
	                    <h2>{this.state.convocation.subject}</h2>
	                    <Grid reversed='mobile tablet vertically'>
	                        <Grid.Column mobile='16' tablet='16' computer='12'>
	                            <Grid>
	                                <Grid.Column computer='16'>
	                                    <Field htmlFor="comments" label={t('convocation.fields.comment')}>
	                                        <FieldValue id="comments">{this.state.convocation.comment}</FieldValue>
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
	                            </Grid>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='4'>
	                            <div className='block-information'>
	                                <Grid columns='1'>
	                                    <Grid.Column>
	                                        <Field htmlFor="Date" label={t('convocation.fields.date')}>
	                                            <FieldValue id="Date">{moment(this.state.convocation.meetingDate, 'YYYY-MM-DDTHH:mm:ss').format('DD-MM-YYYY à HH:mm')}</FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                    <Grid.Column>
	                                        <Field htmlFor="assemblyType" label={t('convocation.fields.assembly_type')}>
	                                            <FieldValue id="assemblyType">{this.state.convocation.assemblyType && this.state.convocation.assemblyType.name}</FieldValue>
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
	                                <FieldValue id="sendingDate">{moment(this.state.convocation.sentDate, 'YYYY-MM-DDTHH:mm:ss').format('DD-MM-YYYY à HH:mm')}</FieldValue>
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
	                            checked={this.state.convocation.responses === 'PRESENT'}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'responses')}
	                        ></Radio>
	                        <Radio
	                            label={t('convocation.page.absent')}
	                            value='NOT_PRESENT'
	                            name='presentQuestion'
	                            checked={this.state.convocation.responses === 'NOT_PRESENT'}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'responses')}
	                        ></Radio>
	                        <Radio
	                            label={t('convocation.page.subisituted')}
	                            name='presentQuestion'
	                            value='SUBSTITUTED'
	                            checked={this.state.convocation.responses === 'SUBSTITUTED'}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'responses')}
	                        ></Radio>
	                    </FormFieldInline>
	                    {this.state.convocation.questions.length > 0 && (
	                        <Fragment>
	                            <h2>{t('convocation.fields.questions')}</h2>
	                            <Grid column='1'>
	                                <Grid.Column mobile='16' computer='16'>
	                                    <QuestionsAnswerForm questions={this.state.convocation.questions}></QuestionsAnswerForm>
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

export default translate(['convocation', 'api-gateway'])(ReceivedConvocation)