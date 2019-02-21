import React, { Component } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Segment, Grid, Button, Form, TextArea } from 'semantic-ui-react'
import accepts from 'attr-accept'

import { notifications } from '../_util/Notifications'
import { getLocalAuthoritySlug, checkStatus, handleFieldChange, extractFieldNameFromId } from '../_util/utils'
import { acceptFileDocumentConvocation } from '../_util/constants'
import history from '../_util/history'

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
	        assemblyType: {},
	        location: '',
	        comment: '',
	        annexes: [],
	        attachment: null,
	        questions: [],
	        recipientResponses: [],
	        sentDate: null,
	        subject: '',
	        cancelled: false
	    },
	    initialRank: null,
	    showAllAnnexes: false,
	    fields: {
	        annexes: [],
	        questions: [],
	        comment: ''
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
	            const initialRank = json.questions.length > 0 ? json.questions[json.questions.length-1].rank + 1 : 0
	            const fields = this.state.fields
	            fields.comment = json.comment
	            this.setState({convocation: json, initialRank, fields })
	        })
	        .catch(response => {
	            response.json().then(json => {
	                _addNotification(notifications.defaultError, 'notifications.title', json.message)
	            })
	        })
	}

	submit = () => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    const parameters = Object.assign({}, this.state.fields)
	    delete parameters.annexes

	    const headers = { 'Content-Type': 'application/json;charset=UTF-8', 'Accept': 'application/json, */*' }
	    _fetchWithAuthzHandling({url: `/api/convocation/${this.props.uuid}`, method: 'PUT', body: JSON.stringify(parameters), context: this.props.authContext, headers: headers})
	        .then(checkStatus)
	        .then(response => response.json())
	        .then(() => {
	            const data = new FormData()
	            if(this.state.fields.annexes.length > 0) {
	                _addNotification(notifications.convocation.complet_with_document)
	                this.state.fields.annexes.forEach(annexe => {
	                    data.append('annexes', annexe)
	                })
	                _fetchWithAuthzHandling({url: `/api/convocation/${this.props.uuid}/upload`, method: 'PUT', body: data, context: this.props.authContext})
	                	.then(checkStatus)
	                	.then(() => {
	                		_addNotification(notifications.convocation.complet)
	                		history.push(`/${localAuthoritySlug}/convocation/liste-envoyees`)
	                	})
	                .catch((error) => {
	                	error.json().then(json => {
	                		_addNotification(notifications.defaultError, 'notifications.title', json.message)
	                	})
	                })
	            } else {
	                _addNotification(notifications.convocation.complet)
	                history.push(`/${localAuthoritySlug}/convocation/liste-envoyees`)
	            }
	        })
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

	deleteAnnexe = (index) => {
	    const fields = this.state.fields
	    fields['annexes'].splice(index, 1)
	    this.setState({ fields })
	}

	goBack = () => {
	    history.goBack()
	}

	render() {
	    const { t } = this.context
	    const { convocation } = this.state

	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const annexesToDisplay = !this.state.showAllAnnexes && this.state.fields.annexes.length > 3 ? this.state.fields.annexes.slice(0,3) : this.state.fields.annexes
	    const annexes = annexesToDisplay.map((annexe, index)=> {
	        return (
	            <File
	                key={`${this.state.convocation.uuid}_${annexe.name}`}
	                attachment={{ filename: annexe.name }}
	                onDelete={() => this.deleteAnnexe(index)} />
	        )
	    })

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
	            <Segment>
	                <SentConvocationFragment convocation={convocation}/>
	                <Form onSubmit={this.submit}>
	                    <h2>{t('convocation.page.additional_information')}</h2>
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
	                    <div className='footerForm'>
	                        <Button type="button" style={{ marginRight: '1em' }} onClick={this.goBack} basic color='red'>
	                            {t('api-gateway:form.cancel')}
	                        </Button>
	                        {submissionButton}
	                    </div>
	                </Form>
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(CompleteSentConvocation))