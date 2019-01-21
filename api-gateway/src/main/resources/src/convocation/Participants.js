import React, { Component, Fragment } from 'react'
import { Tab, Icon, Popup } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class Participants extends Component {
	createPresentContent = (answer) => {
	    return this.props.participants.filter((part) => {
	        return part.answer === answer
	    }).map(part => {
	        const questions = part.questions.map((question) => {
	            return (
	                <Fragment>
	                    {question === 'oui' && (
	                        <td style={{textAlign: 'center', padding: '5px 0'}}>
	                            <Icon style={{color: '#419443'}} name='check'/>
	                        </td>
	                    )}
	                    {question === 'non' && (
	                        <td style={{textAlign: 'center', padding: '5px 0'}}>
	                            <Icon style={{color: '#c73f3f'}} name='cancel'/>
	                        </td>
	                    )}
	                </Fragment>
	            )
	        })
	        return (
	            <tr>
	                <td>{part.name}</td>
	                {questions}
	            </tr>
	        )
	    })
	}
	render() {
	    const questions = this.props.questions.map((question) => {
	        return (
	            <Popup trigger={<th className='text-ellipsis mw-200 cursor-default'>{question}</th>}  content={question}/>
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
			        {this.createPresentContent('OK')}
			    </tbody>
			</table>

	    const absent = this.props.participants.filter(part => {
	        return part.answer === 'KO'
	    }).map(part => {
	        return (
	            <p>
	                {part.name}
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