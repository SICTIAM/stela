import React, { Component } from 'react'
import Datetime from 'react-datetime'
import 'react-datetime/css/react-datetime.css'

class InputDatetime extends Component {
    render() {
        return (
            <div>
                {this.props.ariaLabel && (
                    <Datetime utc={true} inputProps={{ 'id': this.props.id, 'aria-required': this.props.ariaRequired ? this.props.ariaRequired : false, 'aria-label': this.props.ariaLabel }} locale="fr-fr" dateFormat="DD/MM/YYYY" closeOnSelect={true} {...this.props} />
                )}
                {!this.props.ariaLabel && (
                    <Datetime utc={true} inputProps={{ 'id': this.props.id, 'aria-required': this.props.ariaRequired ? this.props.ariaRequired : false }} locale="fr-fr" dateFormat="DD/MM/YYYY" closeOnSelect={true} {...this.props} />

                )}
            </div>
        )
    }
}

export default InputDatetime