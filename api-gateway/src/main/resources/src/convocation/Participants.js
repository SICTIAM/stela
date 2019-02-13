import React, { Component, Fragment } from 'react'
import { Tab, Popup, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class Participants extends Component {
	createPresentContent = (answer) => {
	    /** Create participant row with answer questions
		 * First we filter one response (PRESENT)
		 * Next for each participant we check if it is a answer for each questions -> create one <td></td> per questions
		*/
	    return this.props.participants.filter((part) => {
	        return part.responseType === answer
	    }).map(part => {
	        const responses = this.props.questions.map((question) => {
	            var indexQuestion = question.responses.findIndex((response) => {
	                return response.recipient.uuid === part.recipient.uuid
	            })
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
					    <td></td>
	                )
	            }
	        })
	        return (
	            <tr key={`participant_${part.recipient.uuid}`}>
	                <td key={`info_participant_${part.recipient.uuid}`}>{`${part.recipient.firstname} ${part.recipient.lastname}`}</td>
	                {responses}
	            </tr>
	        )
	    })
	}
	render() {
	    const questions = this.props.questions.map((question) => {
	        return (
	            <Popup key={question.uuid} trigger={<th className='text-ellipsis mw-200 cursor-default' id={question.uuid}>{question.question}</th>}  content={question.question}/>
	        )
	    })

	    const presentContent =
			<table style={{borderSpacing: '10px'}}>
			    <thead>
			        <tr>
			            <th></th>
			            {questions}
			        </tr>
			    </thead>
			    <tbody>
			        {this.createPresentContent('PRESENT')}
			    </tbody>
			</table>

	    const absent = this.props.participants.filter(part => {
	        return part.responseType === 'NOT_PRESENT'
	    }).map(part => {
	        return (
	            <p key={part.recipient.uuid}>
	                {`${part.recipient.firstname} ${part.recipient.lastname}`}
	            </p>
	        )
	    })
	    const absentContent =
			<div>
			    {absent}
			</div>

	    const panes = [
	        { menuItem: 'Présents', render: () => <Tab.Pane>{presentContent}</Tab.Pane> },
	        { menuItem: 'Absents', render: () => <Tab.Pane>{absentContent}</Tab.Pane> },
	        { menuItem: 'Procurations', render: () => <Tab.Pane>Tab 3 Content</Tab.Pane> },
		  ]
	    return (
	        <Tab panes={panes} />
	    )
	}
}

export default translate(['api-gateway'])(Participants)