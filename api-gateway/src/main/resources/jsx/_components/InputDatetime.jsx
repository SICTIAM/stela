import React, { Component } from 'react'
import Datetime from 'react-datetime'
import 'react-datetime/css/react-datetime.css'

class InputDatetime extends Component {
    render() {
        return (
            <Datetime
                id={this.props.id}
                utc={true}
                onBlur={this.props.onBlur}
                locale="fr-fr"
                value={this.props.value}
                inputProps={this.props.inputProps}
                onChange={this.props.onChange} />
        )
    }
}

export default InputDatetime