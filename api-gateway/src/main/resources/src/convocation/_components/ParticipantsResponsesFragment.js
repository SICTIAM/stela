import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { Tab, Popup, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class ParticipantsResponsesFragment extends Component {
	static contextTypes = {
	    t: PropTypes.func
	}
	static propTypes = {
	    questions: PropTypes.array.isRequired,
	    participants: PropTypes.array.isRequired,
	}
	/**
	 * @param {string} answer - answer we want: 'PRESENT', 'NOT_PRESENT' or 'SUBSTITUTION'
	 * @return {array}
	 * @description return the participants who answered PRESENT
	 */
	filterRecipientsByResponses = (answer) => {
	    return this.props.participants.filter((part) => {
	        return part.responseType === answer
	    })
	}
	/**
	 * @param {object} part - object contains participant response:
	 * exemple: {"uuid":"","recipient":{"uuid":"","firstname":"Julie","lastname":"","email":"","phoneNumber":"","active":true/false,"additionalContact":true/false,"inactivityDate":null}
	 * @param {object} question - question object with responses for each participant
	 * @return {number}
	 */
	getIndexResponseParticipantByQuestion = (part, question) => {
	    return question.responses.findIndex((response) => {
	        return response.recipient.uuid === part.recipient.uuid
	    })
	}

	/**
	 * @param {object} part - object contains participant responses
	 * @return {array}
	 * @description return array with HTML for participants responses
	 */
	getResponsesByParticipant = (part) => {
	    return this.props.questions.map((question) => {
	        const indexQuestion = this.getIndexResponseParticipantByQuestion(part, question)
	        if(indexQuestion !== -1) {
	            return (
	                <Fragment key={`${part.recipient.uuid}_${question.uuid}`}>
	                    {question.responses[indexQuestion].response === true && (
	                        <td style={{textAlign: 'center', padding: '5px 0'}}>
	                            <Icon style={{color: '#419443'}} name='check'/>
	                        </td>
	                    )}
	                    {question.responses[indexQuestion].response === false && (
	                        <td style={{textAlign: 'center', padding: '5px 0'}}>
	                            <Icon style={{color: '#c73f3f'}} name='cancel'/>
	                        </td>
	                    )}
	                </Fragment>
	            )
	        } else {
	            return (
	                <td key={`empty_question_${question.uuid}`}/>
	            )
	        }
	    })
	}

	createResponsesContent = (participants) => {
	    return participants.map(part => {
	        const responseHTML = this.getResponsesByParticipant(part)
	        return (
	            <tr key={`participant_${part.recipient.uuid}`}>
	                <td key={`info_participant_${part.recipient.uuid}`}>{`${part.recipient.firstname} ${part.recipient.lastname}`}</td>
	                {responseHTML}
	            </tr>
	        )
	    })
	}
	render() {
	    const { t } = this.context

	    const questions = this.props.questions.map((question) => {
	        return (
	            <Popup key={question.uuid} trigger={<th className='text-ellipsis mw-200 cursor-default' id={question.uuid}>{question.question}</th>}  content={question.question}/>
	        )
	    })
	    let present = this.filterRecipientsByResponses('PRESENT')
	    let presentContent
	    if(present.length > 0) {
	        presentContent =
				<table style={{borderSpacing: '10px'}}>
				    <thead>
				        <tr>
				            <th></th>
				            {questions}
				        </tr>
				    </thead>
				    <tbody>
				        {this.createResponsesContent(present)}
				    </tbody>
				</table>
	    } else {
	        presentContent = <div>{t('convocation.page.no_present')}</div>
	    }

	    const absent = this.filterRecipientsByResponses('NOT_PRESENT').map(part => {
	        return (
	            <p key={part.recipient.uuid}>
	                {`${part.recipient.firstname} ${part.recipient.lastname}`}
	            </p>
	        )
	    })
	    const absentContent =
			<div>
			    {absent.length === 0 && (
			        t('convocation.page.no_absent')
			    )}
			    {absent.length > 0 && (
			        absent
			    )}
			</div>
	    const substitution = this.filterRecipientsByResponses('SUBSTITUTED').map(part => {
	        return(
	            <p key={part.recipient.uuid}>
	                {`${part.recipient.firstname} ${part.recipient.lastname} ${t('convocation.page.substitution_of')} ${part.substituteRecipient.firstname} ${part.substituteRecipient.lastname}`}
	            </p>
	        )
	    })
	    const substitutionContent =
			<div>
			    {substitution.length === 0 && (
			        t('convocation.page.no_substitution')
			    )}
			    {substitution.length > 0 && (
			        substitution
			    )}
			</div>

	    const noReponse = this.filterRecipientsByResponses('DO_NOT_KNOW')
	    let noResponseContent
	    if(noReponse.length > 0) {
	        noResponseContent =
				<table style={{borderSpacing: '10px'}}>
				    <thead>
				        <tr>
				            <th></th>
				            {questions}
				        </tr>
				    </thead>
				    <tbody>
				        {this.createResponsesContent(noReponse)}
				    </tbody>
				</table>
	    }

	    const panes = [
	        { menuItem: t('convocation.page.presents'), render: () => <Tab.Pane>{presentContent}</Tab.Pane> },
	        { menuItem: t('convocation.page.not_presents'), render: () => <Tab.Pane>{absentContent}</Tab.Pane> },
	        { menuItem: t('convocation.page.substitution'), render: () => <Tab.Pane>{substitutionContent}</Tab.Pane> }
		  ]
	    if(noReponse.length > 0) {
	        panes.push({ menuItem: t('convocation.page.no_response'), render: () => <Tab.Pane>{noResponseContent}</Tab.Pane> })
	    }
	    return (
	        <Tab panes={panes} />
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(ParticipantsResponsesFragment)