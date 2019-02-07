import React, { Component } from 'react'
import { Tab} from 'semantic-ui-react'
import { translate } from 'react-i18next'

class Participants extends Component {
	createPresentContent = (answer) => {
	    return this.props.participants.filter((part) => {
	        return part.responseType === answer
	    }).map(part => {
	        return (
	            <tr key={`participant_${part.recipient.uuid}`}>
	                <td key={`info_participant_${part.recipient.uuid}`}>{`${part.recipient.firstname} ${part.recipient.lastname}`}</td>
	            </tr>
	        )
	    })
	}
	render() {
	    const presentContent =
			<table style={{borderSpacing: '10px'}}>
			    <thead>
			        <tr>
			            <th></th>
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