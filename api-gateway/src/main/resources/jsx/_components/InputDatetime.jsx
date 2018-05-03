import React, { Component } from 'react'
import Datetime from 'react-datetime'
import 'react-datetime/css/react-datetime.css'

class InputDatetime extends Component {
    render() {
        return (
            <Datetime utc={true} locale="fr-fr" closeOnSelect={true} {...this.props} />
        )
    }
}

export default InputDatetime