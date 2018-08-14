import React, { Component } from 'react'
import { Message } from 'semantic-ui-react'

import { anomalies } from '../_util/constants'

class Anomaly extends Component {
    static defaultProps = {
        lastHistory: {
            status: '',
            message: ''
        }
    }
    render() {
        const { header, lastHistory } = this.props
        return (
            (anomalies.includes(lastHistory.status) && lastHistory.message) && (
                <Message negative>
                    <Message.Header>{header}</Message.Header>
                    <p>{lastHistory.message}</p>
                </Message>
            )
        )
    }
}

export default Anomaly