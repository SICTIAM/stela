import React, { Component } from 'react'
import { Message } from 'semantic-ui-react'

import { anomalies } from '../_util/constants'
import { PesErrorList } from '../_components/UI'
import CollapsedList from './CollapsedList'

class Anomaly extends Component {
    static defaultProps = {
        lastHistory: {
            status: '',
            message: '',
            errors: []
        },
        type: 'negative'
    }
    render() {
        const { header, lastHistory, type = 'negative' } = this.props
        return (
            anomalies.includes(lastHistory.status) && (
                <Message negative={type === 'negative'} warning={type === 'warning'}>
                    <Message.Header style={{ marginBottom: '0.5em'}}>{header}</Message.Header>
                    {lastHistory.message &&
                        <p>{lastHistory.message}</p>
                    }
                    {lastHistory.errors &&
                        <CollapsedList items={PesErrorList(lastHistory.errors, `anomaly-${lastHistory.uuid}`)}/>
                    }
                </Message>
            )
        )
    }
}

export default Anomaly