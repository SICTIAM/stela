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
        }
    }
    render() {
        const { header, lastHistory } = this.props
        return (
            anomalies.includes(lastHistory.status) && (
                <Message negative>
                    <Message.Header style={{ marginBottom: '0.5em'}}>{header}</Message.Header>
                    {lastHistory.message &&
                        <p>{lastHistory.message}</p>
                    }
                    {lastHistory.errors &&
                        <CollapsedList items={PesErrorList(lastHistory.errors)}/>
                    }
                </Message>
            )
        )
    }
}

export default Anomaly