import React, { Component } from 'react'
import { Label, Dropdown } from 'semantic-ui-react'
import renderIf from 'render-if'
import Validator from 'validatorjs'
import moment from 'moment'

export default class InputValidation extends Component {
    state = { isValid: true, errorMessage: '' }
    static defaultProps = { value: '', type: '', accept: '', className: '' }
    validateValue = () => {
        const value = this.props.type === 'date' ? moment(this.props.value).format('MM.DD.YYYY') : this.props.value
        const validation = new Validator({ field: value }, { field: this.props.validationRule }, this.props.customErrorMessages)
        validation.setAttributeNames({ field: this.props.fieldName });
        const isValid = validation.passes()
        const errorMessage = validation.errors.first('field') || ''
        this.setState({ isValid: isValid, errorMessage: errorMessage })
    }
    render() {
        return (
            <div>
                {(this.props.type === 'text' || this.props.type === '')
                    && <input id={this.props.id}
                        className={this.props.className}
                        placeholder={this.props.placeholder}
                        value={this.props.value}
                        onChange={e => this.props.onChange(this.props.id, e.target.value)}
                        onBlur={this.validateValue} />}

                {this.props.type === 'date'
                    && <input id={this.props.id}
                        type='date'
                        className={this.props.className}
                        placeholder={this.props.placeholder}
                        max={this.props.max}
                        value={this.props.decision}
                        onChange={e => this.props.onChange(this.props.id, e.target.value)}
                        onBlur={this.validateValue} />}

                {this.props.type === 'file'
                    && <input id={this.props.id}
                        type='file'
                        className={this.props.className}
                        accept={this.props.accept}
                        multiple={this.props.multiple}
                        placeholder={this.props.placeholder}
                        onChange={e => this.props.onChange(this.props.id, e.target.files[0])}
                        onBlur={this.validateValue} />}

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
                       onChange={(event,data) => this.props.onChange(this.props.id, data.value)}
                       onBlur={this.validateValue}
                       options={this.props.options}
                       fluid search selection />}

                <div>
                    {renderIf(!this.state.isValid)(
                        <Label basic color='red' pointing>{this.state.errorMessage}</Label>
                    )}
                </div>
            </div>
        )
    }
}