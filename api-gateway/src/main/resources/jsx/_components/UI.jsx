import React, { Component } from 'react'
import { Form, Grid, Label } from 'semantic-ui-react'
import renderIf from 'render-if'
import Validator from 'validatorjs'
import debounce from 'debounce'

const FormField = ({ htmlFor, label, children, inline }) =>
    <Form.Field inline={inline ? true : false}>
        <label htmlFor={htmlFor}>{label}</label>
        {children}
    </Form.Field>

const FormFieldInline = ({ htmlFor, label, children }) =>
    <Form.Field inline>
        <Grid>
            <Grid.Column className='inline-grid' width={4}><label htmlFor={htmlFor}>{label}</label></Grid.Column>
            <Grid.Column className='inline-grid' width={12}>{children}</Grid.Column>
        </Grid>
    </Form.Field>

const Field = ({ htmlFor, label, children, }) =>
    <Grid>
        <Grid.Column width={4}><label htmlFor={htmlFor}>{label}</label></Grid.Column>
        <Grid.Column width={12}>{children}</Grid.Column>
    </Grid>

class ValidationWarning extends Component {
    state = { isValid: true, errorMessage: '', propsReceived: false, initialValue: this.props.value }
    componentWillReceiveProps(nextProps) {
        const customErrorMessages = this.props.customErrorMessages || {}
        if (this.state.initialValue !== nextProps.value || this.state.propsReceived) this.validateValue(nextProps.value, nextProps.validationRule, customErrorMessages)
    }
    validateValue = debounce((data, rule, customErrorMessages) => {
        const validation = new Validator({ field: data }, { field: rule }, customErrorMessages)
        validation.setAttributeNames({ field: this.props.fieldName });
        const isValid = validation.passes()
        const errorMessage = validation.errors.first('field') || ''
        this.setState({ isValid: isValid, errorMessage: errorMessage, propsReceived: true })
    }, 1000)
    render() {
        return (
            renderIf(!this.state.isValid)(
                <Label basic color='red' pointing>{this.state.errorMessage}</Label>
            )
        )
    }
}

module.exports = { FormField, FormFieldInline, Field, ValidationWarning }
