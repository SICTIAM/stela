import React, { Component } from 'react'
import { Label, Dropdown } from 'semantic-ui-react'
import renderIf from 'render-if'
import Validator from 'validatorjs'
import moment from 'moment'

import { InputFile } from './UI'
import InputDatetime from './InputDatetime'

export default class InputValidation extends Component {
    state = {
        isValid: true,
        errorMessage: ''
    }
    static defaultProps = {
        value: '',
        type: '',
        accept: '',
        className: '',
        style: {}
    }
    validateValue = () => {
        const value = this.props.type === 'date' ? moment(this.props.value).format('YYYY-MM-DD') : this.props.value
        const validation = new Validator({ field: value }, { field: this.props.validationRule }, this.props.customErrorMessages)
        validation.setAttributeNames({ field: this.props.fieldName });
        const isValid = validation.passes()
        const errorMessage = validation.errors.first('field') || ''
        this.setState({ isValid: isValid, errorMessage: errorMessage })
    }
    render() {
        const { style, onChange, ...rest } = this.props
        return (
            <div style={style}>
                {(this.props.type === 'text' || this.props.type === '')
                    && <input id={this.props.id}
                        className={this.props.className}
                        placeholder={this.props.placeholder}
                        value={this.props.value}
                        onChange={e => this.props.onChange(this.props.id, e.target.value)}
                        onBlur={this.validateValue} />}

                {this.props.type === 'date' &&
                    <InputDatetime {...rest}
                        timeFormat={false}
                        onChange={date => onChange(this.props.id, date, this.validateValue)} />}

                {this.props.type === 'file' &&
                    <InputFile htmlFor={this.props.id} label={this.props.label}>
                        <input id={this.props.id}
                            type='file'
                            style={{ display: 'none' }}
                            accept={this.props.accept}
                            multiple={this.props.multiple}
                            placeholder={this.props.placeholder}
                            onChange={e => this.props.onChange(e.target.files[0])}
                            onBlur={this.validateValue} />
                    </InputFile>}

                {this.props.type === 'select'
                    && <select id={this.props.id}
                        className={this.props.className}
                        value={this.props.value}
                        onChange={e => this.props.onChange(this.props.id, e.target.value)}
                        onBlur={this.validateValue}>
                        {this.props.children}
                    </select>}

                {this.props.type === 'dropdown'
                    && <Dropdown id={this.props.id}
                        className={this.props.className}
                        value={this.props.value}
                        onChange={(event, data) => this.props.onChange(this.props.id, data.value)}
                        onBlur={this.validateValue}
                        options={this.props.options}
                        search={this.props.search || false}
                        fluid selection />}

                <div>
                    {renderIf(!this.state.isValid)(
                        <Label basic color='red' pointing>{this.state.errorMessage}</Label>
                    )}
                </div>
            </div>
        )
    }
}