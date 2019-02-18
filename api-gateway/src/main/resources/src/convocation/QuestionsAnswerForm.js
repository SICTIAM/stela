import React, { Component, Fragment } from 'react'
import { Radio, Form, Grid } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'

class QuestionsAnswerForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	static propTypes = {
	    questions: PropTypes.array
	}

	render() {
	    const { t } = this.context
	    const questions = this.props.questions.map((question, index) => {
	        return (
	            <div key={`question_${index}`} className='bb-1 px-10 py-5'>
	                <Form.Field inline>
	                    <Grid>
	                        <Grid.Column className="inline-grid" width={12}>
	                            <label style={{ verticalAlign: 'middle' }} htmlFor={`question_${index}`}>
	                                {question.question}
	                            </label>
	                        </Grid.Column>
	                        <Grid.Column className="inline-grid" width={4}>
	                            <Radio
	                                value='true'
	                                name={`question_${index}`}
	                                label={t('api-gateway:yes')}
	                                checked={question.response === true}
	                                onChange={(e, {value}) => this.props.handleChangeRadio(e, value, question.uuid)}
	                            ></Radio>
	                            <Radio
	                                value='false'
	                                name={`question_${index}`}
	                                label={t('api-gateway:no')}
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