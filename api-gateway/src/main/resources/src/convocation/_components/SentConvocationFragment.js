import React, { Component, Fragment } from 'react'
import { Grid, Button, Segment } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'

import { Field, FieldValue, LinkFile } from '../../_components/UI'

import QuestionsForm from '../QuestionsForm'
import InformationBlockConvocation from './InformationBlock'
import SenderInformation from './SenderInformation'


class SentConvocationFragment extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}

	state = {
	    showAllAnnexes: false,
	}
	sumRecipientsByStatus = (answer) => {
	    return this.props.convocation.recipientResponses.reduce( (acc, curr) => {
	        return curr['responseType'] === answer ? acc + 1: acc
	    }, 0)
	}

	greyResolver = (participant) => {
	    return !participant.opened
	}

	render() {
	    const { t } = this.context
	    const { convocation } = this.props

	    const annexesToDisplay = !this.state.showAllAnnexes && convocation.annexes && convocation.annexes.length > 3 ? convocation.annexes.slice(0,3) : convocation.annexes
	    const annexes = annexesToDisplay.map(annexe => {
	        return (
	            <div key={`div_${convocation.uuid}_${annexe.uuid}`}>
	                <LinkFile
	                    url={`/api/convocation/${convocation.uuid}/file/${annexe.uuid}?stamped=true`}
	                    key={`${convocation.uuid}_${annexe.uuid}`}
	                    text={annexe.filename}/>
	            </div>

	        )
	    })

	    return (
	        <Fragment>
	            <Segment>
	            	<h2>{convocation.subject}</h2>
	                <Grid reversed='mobile tablet vertically'>
	                    <Grid.Column mobile='16' tablet='16' computer='12'>
	                        <Grid>
	                        	{convocation.comment && (
	                            	<Grid.Column computer='16'>
	                                	<Field htmlFor="comment" label={t('convocation.fields.comment')}>
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
	                            	</Grid.Column>
	                        	)}

	                            {convocation.attachment && (
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor="document" label={t('convocation.fields.convocation_document')}>
	                                        <FieldValue id="document">
	                                            <LinkFile url={`/api/convocation/${convocation.uuid}/file/${convocation.attachment.uuid}?stamped=true`} text={convocation.attachment.filename} />
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
	                            {convocation.minutes && (
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor='minutes' label={t('convocation.fields.minutes')}>
	                                        <FieldValue id="document">
	                                            <LinkFile url={`/api/convocation/${convocation.uuid}/file/${convocation.minutes.uuid}?stamped=true`} text={convocation.minutes.filename} />
	                                        </FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            )}
	                            {convocation.questions.length > 0 && (
	                                <Grid.Column computer='16' tablet='16'>
	                                    <Field htmlFor="questions" label={t('convocation.fields.questions')}>
	                                        <QuestionsForm
	                                            editable={false}
	                                            questions={convocation.questions}
	                                        />
	                                    </Field>
	                                </Grid.Column>
	                            )}
	                        </Grid>
	                    </Grid.Column>
	                    <InformationBlockConvocation convocation={convocation}/>
	                </Grid>
	            </Segment>
	            <SenderInformation convocation={convocation}/>
	        </Fragment>
	    )
	}

}

export default translate(['convocation', 'api-gateway'])(SentConvocationFragment)