import React, { Component } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Segment, Grid, Button, Form, TextArea, Dropdown } from 'semantic-ui-react'
import accepts from 'attr-accept'

import { notifications } from '../_util/Notifications'
import { getLocalAuthoritySlug, handleFieldChange, extractFieldNameFromId } from '../_util/utils'
import { acceptFileDocumentConvocation } from '../_util/constants'
import history from '../_util/history'
import ConvocationService from '../_util/convocation-service'

import AddRecipientsGuestsFormFragment from './_components/AddRecipientsGuestsFormFragment'

import { withAuthContext } from '../Auth'
import QuestionsForm from './QuestionsForm'
import { Page, FormField, File, InputFile,InputTextControlled } from '../_components/UI'
import Breadcrumb from '../_components/Breadcrumb'

import SentConvocationFragment from './_components/SentConvocationFragment'

class CompleteSentConvocation extends Component {

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
	        cancelled: false
	    },
	    initialRank: null,
	    showAllAnnexes: false,
	    defaultPart: {
	        guests: [],
	        recipients: []
	    },
	    fields: {
	        annexes: [],
	        questions: [],
	        comment: '',
	        guests: [],
	        recipients: [],
	        annexesTags: []
	    },
	    tagsList: []
	}

	componentDidMount = async() => {
	    this._convocationService = new ConvocationService()
	    await this.fetchConvocation()
	}

	fetchConvocation = async() => {
	    const convocationResponse = await this._convocationService.getSentConvocation(this.props.authContext, this.props.uuid)
	    const initialRank = convocationResponse.questions.length > 0 ? convocationResponse.questions[convocationResponse.questions.length-1].rank + 1 : 0
	    const { fields, defaultPart }  = this.state
	    fields.comment = convocationResponse.comment
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

	    defaultPart.recipients = convocationResponse.recipientResponses.map(response => {
	        return response.recipient
	    })

	    defaultPart.guests = convocationResponse.guestResponses.map(response => {
	        return response.recipient
	    })

	    const tagsListResponse = (await this._convocationService.getAllTags(this.props.authContext)).map(item => {
	        return {key: item.uuid, text: item.name, uuid: item.uuid, value: item.uuid}
	    })

	    this.setState({convocation: convocationResponse, initialRank, fields, defaultPart, tagsList: tagsListResponse })
	}

	submit = async() => {
	    const { _addNotification } = this.context
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    const parameters = Object.assign({}, this.state.fields)
	    delete parameters.annexes

	    parameters.guests.forEach((guest) => {
	        guest.guest = true
	        parameters.recipients.push(guest)
	    })

	    await this._convocationService.updateConvocation(this.props.authContext, this.props.uuid, parameters)

	    if(this.state.fields.annexes.length > 0) {
	        _addNotification(notifications.convocation.complet_with_document)
	        const data = new FormData()
	        this.state.fields.annexes.forEach(annexe => {
	            data.append('annexes', annexe)
	        })
	        if(this.state.fields.annexesTags.length > 0) {
	            const annexesTags = this.state.fields.annexesTags.map((annexe) => {
	                const tags = annexe.tags.join('/')
	                return `${annexe.fileName}/${tags}`
	            })
	            data.append('tags', annexesTags)
	        }
	        await this._convocationService.updateDocumentsConvocation(this.props.authContext, this.props.uuid, data)
	        _addNotification(notifications.convocation.complet)
	        history.push(`/${localAuthoritySlug}/convocation/liste-envoyees`)
	    } else {
	        _addNotification(notifications.convocation.complet)
	        history.push(`/${localAuthoritySlug}/convocation/liste-envoyees`)
	    }
	}

	updateQuestions = (questions) => {
	    const fields = this.state.fields
	    fields['questions'] = questions
	    this.setState({fields})
	}

	acceptsFile = (file, acceptType) => {
	    const { _addNotification, t } = this.context
	    if(accepts(file, acceptType)) {
	        return true
	    } else {
	        _addNotification(notifications.defaultError, 'notifications.convocation.title', t('api-gateway:form.validation.bad_extension_file', {name: file.name}))
	        return false
	    }
	}

	handleAnnexeChange = (file, acceptType) => {
	    const fields = this.state.fields
	    for(let i = 0; i < file.length; i++) {
	        if(this.acceptsFile(file[i], acceptType)) {
	            fields['annexes'].push(file[i])
	        }
	    }

	    this.setState({ fields })
	}

	deleteAnnexe = (index, annexeName) => {
	    const fields = this.state.fields
	    fields['annexes'].splice(index, 1)
	    const idAnnexeTags = fields['annexesTags'].findIndex((annexes) => {
	        return annexes.fileName === annexeName
	    })
	    fields['annexesTags'].splice(idAnnexeTags, 1)
	    this.setState({ fields })
	}

	updateUser = (fields) => {
	    this.setState({fields})
	}

	goBack = () => {
	    history.goBack()
	}

	handleTagChange = (fileName, tags) => {
	    const { fields } = this.state

	    const idAnnexeTags = fields['annexesTags'].findIndex((annexe) => {
	        return annexe.fileName === fileName
	    })
	    if(idAnnexeTags === -1) {
	        fields['annexesTags'].push({fileName: fileName, tags: tags})
	    } else {
	        fields['annexesTags'][idAnnexeTags]['tags'] = tags
	    }
	    this.setState({ fields })
	}

	render() {
	    const { t } = this.context
	    const { convocation, fields, defaultPart, tagsList } = this.state

	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const annexesToDisplay = !this.state.showAllAnnexes && this.state.fields.annexes.length > 3 ? this.state.fields.annexes.slice(0,3) : this.state.fields.annexes
	    const annexes = annexesToDisplay.map((annexe, index)=> {
	        return (
	            <File
	                key={`${this.state.convocation.uuid}_${annexe.name}`}
	                attachment={{ filename: annexe.name }}
	                onDelete={() => this.deleteAnnexe(index, annexe.name)}
	                extraContent={<Dropdown
	                    placeholder={t('convocation.fields.pick_tag')}
	                    fluid
	                    multiple
	                    search
	                    selection
	                    options={tagsList}
	                    onChange={(e, { value }) => this.handleTagChange(annexe.name, value)}/>}/>
	        )
	    })

	    const fieldsToUpdateRecipients = Object.assign(fields, {uuid: convocation.uuid})

	    const submissionButton =
	        <Button type='submit' primary basic>
	            {t('api-gateway:form.send')}
	        </Button>

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
	            <SentConvocationFragment convocation={convocation}/>
	                <Form onSubmit={this.submit}>
	                {/* Comments */}
	                <Segment>
	                    <h2>{t('convocation.page.additional_information')}</h2>
	                    <Grid>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_comment`}
	                                label={t('convocation.fields.comment')}>
	                                <InputTextControlled component={TextArea}
	                                    id={`${this.state.fields.uuid}_comment`}
	                                    maxLength={250}
	                                    style={{ minHeight: '3em' }}
	                                    placeholder={`${t('convocation.fields.comment')}...`}
	                                    value={this.state.fields.comment}
	                                    onChange={(id, value) => handleFieldChange(this, extractFieldNameFromId(id), value)} />
	                            </FormField>
	                        </Grid.Column>
	                    </Grid>
	                </Segment>
	                {/* Questions */}
	                <Segment>
	                    <Grid>
	                        <Grid.Column mobile='16' computer='16'>
	                            {this.state.initialRank !== null && (
	                                <QuestionsForm
	                                    editable={true}
	                                    questions={this.state.fields.questions}
	                                    initialRank={this.state.initialRank}
	                                    onUpdateQuestions={this.updateQuestions}
	                                    uuid={this.state.convocation.uuid}/>
	                            )}
	                        </Grid.Column>
	                    </Grid>
	                </Segment>
	                {/* Documents */}
	                <Segment>
	                    <Grid>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.convocation.uuid}_annexes`}
	                                label={t('convocation.page.additional_documents')}>
	                                <InputFile labelClassName="primary" htmlFor={`${this.state.convocation.uuid}_annexes`}
	                                    label={`${t('api-gateway:form.add_a_file')}`}>
	                                    <input type="file"
	                                        id={`${this.state.convocation.uuid}_annexes`}
	                                        accept={acceptFileDocumentConvocation}
	                                        multiple
	                                        onChange={(e) => this.handleAnnexeChange(e.target.files, acceptFileDocumentConvocation)}
	                                        style={{ display: 'none' }}/>
	                                </InputFile>
	                            </FormField>
	                            {this.state.fields.annexes.length > 0 && (
	                                <div>
	                                    {annexes}
	                                    {this.state.fields.annexes.length > 3 && (
	                                        <div className='mt-15'>
	                                            <Button onClick={() => this.setState({showAllAnnexes: !this.state.showAllAnnexes})} className="link" primary compact basic>
	                                                {this.state.showAllAnnexes && (
	                                                    <span>{t('convocation.new.show_less_annexes')}</span>
	                                                )}
	                                                {!this.state.showAllAnnexes && (
	                                                    <span>{t('convocation.new.show_all_annexes', {number: this.state.fields.annexes.length})}</span>
	                                                )}
	                                            </Button>
	                                        </div>
	                                    )}

	                                </div>
	                            )}
	                        </Grid.Column>
	                    </Grid>
	                </Segment>
	                {/* Recipients and Guests */}
	                <AddRecipientsGuestsFormFragment fields={fieldsToUpdateRecipients} updateUser={this.updateUser} userToDisabled={defaultPart}/>
	                <div className='footerForm'>
	                    <Button type="button" style={{ marginRight: '1em' }} onClick={this.goBack} basic color='red'>
	                        {t('api-gateway:form.cancel')}
	                    </Button>
	                    {submissionButton}
	                </div>
	            </Form>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(CompleteSentConvocation))