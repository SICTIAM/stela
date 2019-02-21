import React, { Component } from 'react'
import {Label, Dropdown, Popup} from 'semantic-ui-react'
import Validator from 'validatorjs'
import moment from 'moment'
import PropTypes from 'prop-types'

import { InputFile } from './UI'
import InputDatetime from './InputDatetime'
import InputTimePicker from './InputTimePicker'

export default class InputValidation extends Component {
    state = {
        isValid: true,
        errorMessage: '',
        dropdownSearchValue: ''
    }
    static defaultProps = {
        value: '',
        type: '',
        accept: '',
        className: '',
        style: {},
        min: 0
    }
    validateValue = () => {
        const value = this.props.type === 'date' && this.props.value ? moment(this.props.value).format('YYYY-MM-DD') : this.props.value
        const validation = new Validator({ field: value }, { field: this.props.validationRule }, this.props.customErrorMessages)
        validation.setAttributeNames({ field: this.props.fieldName })
        const isValid = validation.passes()
        const errorMessage = validation.errors.first('field') || ''
        this.setState({ isValid: isValid, errorMessage: errorMessage })
    }

    selectInput = () => {
        const {onChange, ...rest } = this.props

        switch (this.props.type) {
        case(''):
        case('text'):
            return (
                <input id={this.props.id}
                    disabled={this.props.disabled}
                    aria-required={this.props.ariaRequired ? this.props.ariaRequired : false}
                    className={this.props.className + (this.state.errorMessage ? ' error' : '')}
                    placeholder={this.props.placeholder}
                    value={this.props.value}
                    onChange={e => this.props.onChange(this.props.id, e.target.value)}
                    onBlur={this.validateValue}/>
            )
        case ('number'):
            return(
                <input id={this.props.id}
                    disabled={this.props.disabled}
                    aria-required={this.props.ariaRequired ? this.props.ariaRequired : false}
                    className={this.props.className + (this.state.errorMessage ? ' error' : '')}
                    placeholder={this.props.placeholder}
                    value={this.props.value}
                    type='number'
                    min={this.props.min}
                    onChange={e => this.props.onChange(this.props.id, e.target.value)}
                    onBlur={this.validateValue}/>
            )
        case('date'):
            return (
                <InputDatetime {...rest}
                    timeFormat={false}
                    onBlur={this.validateValue}
                    placeholder={this.props.placeholder}
                    error={this.state.errorMessage ? true : false}
                    onChange={date => onChange(this.props.id, date, this.validateValue)}/>)
        case('time'):
            return (
                <InputTimePicker
                    dropdown={this.props.dropdown}
                    placeholder={this.props.placeholder}
                    value={this.props.value}
                    id={this.props.id}
                    error={this.state.errorMessage ? true : false}
                    onChange={hour => this.props.onChange(this.props.id, hour)}
                    onBlur={this.validateValue}/>
            )
        case ('file'):
            return (
                <InputFile icon={this.props.icon} htmlFor={this.props.id} label={this.props.label} labelClassName={this.props.labelClassName}>
                    <input id={this.props.id}
                        disabled={this.props.disabled}

                        type='file'
                        aria-required={this.props.ariaRequired ? this.props.ariaRequired : false}
                        style={{display: 'none'}}
                        accept={this.props.accept}
                        multiple={this.props.multiple}
                        placeholder={this.props.placeholder}
                        onChange={e => this.props.onChange(e.target.files[0], this.props.accept)}
                        onBlur={this.validateValue}/>
                </InputFile>)
        case('select'):
            return (
                <select id={this.props.id}
                    aria-required={this.props.ariaRequired ? this.props.ariaRequired : false}
                    className={this.props.className}
                    value={this.props.value}
                    onChange={e => this.props.onChange(this.props.id, e.target.value)}
                    onBlur={this.validateValue}>
                    {this.props.children}
                </select>
            )
        case ('dropdown'):
            return (
                <Dropdown id={!this.props.search ? this.props.id : ''}
                    aria-required={this.props.ariaRequired ? this.props.ariaRequired : false}
                    className={this.props.className}
                    disabled={this.props.disabled}
                    placeholder={this.props.placeholder}
                    onChange={(event, data) => {
                        this.props.search ? this.setState({dropdownSearchValue: ''}) : null
                        this.props.onChange(this.props.id, data.value, this.validateValue)
                    }}
                    onBlur={this.validateValue}
                    options={this.props.options}
                    value={this.props.value}
                    searchQuery={this.props.search ? this.state.dropdownSearchValue : null}
                    searchInput={this.props.search ?
                        <input
                            value={this.state.dropdownSearchValue}
                            onChange={(event) => {
                                event.stopPropagation()
                                this.setState({dropdownSearchValue: event.target.value})
                            }}
                            id={this.props.id}
                            className={'search'}
                            type={'text'}
                            aria-autocomplete={'list'}
                            autoComplete={'off'}
                            tabIndex={'0'}/>
                        : null}
                    search={this.props.search || false}
                    fluid selection>
                </Dropdown>)
        default:
            break
        }
    }


    render() {
        const { style, helper } = this.props
        const input = this.selectInput()
        return (
            <div style={style}>
                {helper ?
                    <Popup trigger={input}
                        content={helper}
                        on='focus'
                    />
                    : input
                }

                <div>
                    {!this.state.isValid && this.props.errorTypePointing && (
                        <Label color='red' pointing>{this.state.errorMessage}</Label>
                    )}
                </div>
                {!this.state.isValid && !this.props.errorTypePointing && (
                    <p className='error-message-form'>{this.state.errorMessage}</p>
                )}
            </div>
        )
    }
}

InputValidation.propTypes = {
    helper: PropTypes.string
}
