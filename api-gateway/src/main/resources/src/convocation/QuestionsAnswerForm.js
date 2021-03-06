import React, { Component, Fragment } from 'react'
import { Radio, Form, Grid } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'

class QuestionsAnswerForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	static propTypes = {
	    questions: PropTypes.array,
	    disabled: PropTypes.bool
	}

	render() {
	    const { t } = this.context
	    const questions = this.props.questions.map((question, index) => {
	        return (
	            <div key={`question_${index}`} className='bb-1 py-5'>
	                <Form.Field inline>
	                    <Grid>
	                        <Grid.Column className="inline-grid" computer={12} mobile={16}>
	                            <label style={{ verticalAlign: 'middle' }} htmlFor={`question_${index}`}>
	                                {question.question}
	                            </label>
	                        </Grid.Column>
	                        <Grid.Column className="inline-grid" computer={4} mobile={16}>
	                            <Radio
	                                value='true'
	                                name={`question_${index}`}
	                                label={t('api-gateway:yes')}
	                                disabled={this.props.disabled}
	                                checked={question.response === true}
	                                onChange={(e, {value}) => this.props.handleChangeRadio(e, value, question.uuid)}
	                            ></Radio>
	                            <Radio
	                                value='false'
	                                name={`question_${index}`}
	                                label={t('api-gateway:no')}
	                                disabled={this.props.disabled}
	                                checked={question.response === false}
	                                onChange={(e, {value}) => this.props.handleChangeRadio(e, value, question.uuid)}
	                            ></Radio>
	                        </Grid.Column>
	                    </Grid>
	                </Form.Field>
	            </div>
	        )
	    })
	    return (
	        <Fragment>
	            {questions}
	        </Fragment>
	    )
	}
}
export default translate(['convocation','api-gateway'])(QuestionsAnswerForm)