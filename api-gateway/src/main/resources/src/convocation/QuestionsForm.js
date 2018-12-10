import React, { Component, Fragment } from 'react'
import { Input, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

import { FormField } from '../_components/UI'

class QuestionsForm extends Component {

	state = {
	    currentQuestion: ''
	}
	handleKeyPress = (event) => {
	    if(event.key === 'Enter') {
	        event.preventDefault()
	        this.addQuestion()
	    }
	}

	addQuestion = () => {
	    if(this.state.currentQuestion.trim().length > 0 ) {
	        const questions = this.props.questions
	        questions.push(this.state.currentQuestion)
	        this.setState({currentQuestion: ''})
	        this.props.onUpdateQuestions(questions)
	    }
	}

	removeQuestion = (index) => {
	    const questions = this.props.questions
	    questions.splice(index, 1)
	    this.props.onUpdateQuestions(questions)
	}

	render() {
	    const questions = this.props.questions.map((question, index) => {
	        return (
	            <Fragment>
	                <div className='deletableListItem' id='questions'>
	                    <p className='flexyItem mb-0'>{question}</p>
	                    { this.props.editable  && (
	                        <Icon name='remove' color='red' className='pointer l-icon' onClick={() => {this.removeQuestion(index)}}/>
	                    )}
	                </div>
	            </Fragment>
	        )
	    })

	    return (
	        <Fragment>
	            {this.props.editable && (
	                <FormField htmlFor={`${this.props.uuid}_questions`}
	                    label='Question(s)'>
	                    <Input
	                        id={`${this.props.uuid}_questions`}
	                        value={this.state.currentQuestion}
	                        onKeyPress={(e) => { this.handleKeyPress(e) }}
	                        onChange={(e) => { this.setState({currentQuestion: e.target.value}) } }
	                        action={{ icon: 'add', color: 'primary', onClick: this.addQuestion, type: 'button' }}
	                        placeholder='Entrez votre question'/>
	                </FormField>
	            )}
	            {questions}
	        </Fragment>
	    )
	}
}
export default translate(['api-gateway'])(QuestionsForm)