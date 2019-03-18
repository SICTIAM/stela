import React, { Component } from 'react'
import Datetime from 'react-datetime'
import 'react-datetime/css/react-datetime.css'
import moment from 'moment'

class InputDatetime extends Component {
    render() {
        return (
            <div>
                {this.props.ariaLabel && (
                    <Datetime
                        inputProps={{ 'id': this.props.id, 'aria-required': this.props.ariaRequired ? this.props.ariaRequired : false, 'aria-label': this.props.ariaLabel, 'placeholder': this.props.placeholder, 'className': this.props.error ? 'error' : '' }}
                        locale="fr-fr"
                        dateFormat="DD/MM/YYYY"
                        closeOnSelect={true}
                        onBlur={this.props.onBlur}
                        viewDate={this.props.viewDate || moment()}
                        {...this.props} />
                )}
                {!this.props.ariaLabel && (
                    <Datetime
                        inputProps={{ 'id': this.props.id, 'aria-required': this.props.ariaRequired ? this.props.ariaRequired : false, 'placeholder': this.props.placeholder, 'className': this.props.error ? 'error' : '' }}
                        locale="fr-fr"
                        dateFormat="DD/MM/YYYY"
                        closeOnSelect={true}
                        viewDate={this.props.viewDate || moment()}
                        onBlur={this.props.onBlur}
                        {...this.props} />
                )}
            </div>
        )
    }
}

export default InputDatetime
