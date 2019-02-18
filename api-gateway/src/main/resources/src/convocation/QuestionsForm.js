import React, { Component, Fragment } from 'react'
import { Input, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'

import { FormField } from '../_components/UI'

class QuestionsForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	static propTypes = {
	    editable: PropTypes.bool,
	    response: PropTypes.bool,
	    questions: PropTypes.array
	}
	state = {
	    currentQuestion: '',
	    currentRank: 1
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
	        const rank = this.state.currentRank
	        questions.push({ question: this.state.currentQuestion, rank: rank })
	        this.setState({currentQuestion: '', currentRank: rank + 1 })
	        this.props.onUpdateQuestions(questions)
	    }
	}

	removeQuestion = (index) => {
	    const questions = this.props.questions
	    questions.splice(index, 1)
	    this.props.onUpdateQuestions(questions)
	}

	render() {
	    const { t } = this.context
	    const questions = this.props.questions.map((question, index) => {
	        return (
	            <Fragment key={`question_${index}`}>
	                <div className='deletableListItem' id='questions'>
	                    <p className='flexyItem mb-0'>{question.question}</p>
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
	                    label={t('convocation.fields.questions')}>
	                    <Input
	                        id={`${this.props.uuid}_questions`}
	                        value={this.state.currentQuestion}
	                        onKeyPress={(e) => { this.handleKeyPress(e) }}
	                        onChange={(e) => { this.setState({currentQuestion: e.target.value}) } }
	                        action={{ icon: 'add', color: 'blue', onClick: this.addQuestion, type: 'button' }}
	                        placeholder={t('convocation.fields.questions_placeholder')}/>
	                </FormField>
	            )}
	            {questions}
	        </Fragment>
	    )
	}
}
export default translate(['convocation','api-gateway'])(QuestionsForm)