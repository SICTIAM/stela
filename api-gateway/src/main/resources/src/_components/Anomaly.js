import React, { Component, Fragment } from 'react'
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
        const { header, lastHistory, type } = this.props
        return (
            <Fragment>
                { anomalies.includes(lastHistory.status) && type === 'negative' && (
                    <Message negative>
                        <Message.Header style={{ marginBottom: '0.5em'}}>{header}</Message.Header>
                        {lastHistory.message &&
                            <p>{lastHistory.message}</p>
                        }
                        {lastHistory.errors &&
                            <CollapsedList items={PesErrorList(lastHistory.errors, `anomaly-${lastHistory.uuid}`)}/>
                        }
                    </Message>
                )}
                { anomalies.includes(lastHistory.status) && type === 'warning' && (
                    <Message warning>
                        <Message.Header style={{ marginBottom: '0.5em'}}>{header}</Message.Header>
                        {lastHistory.message &&
                            <p>{lastHistory.message}</p>
                        }
                        {lastHistory.errors &&
                            <CollapsedList items={PesErrorList(lastHistory.errors, `anomaly-${lastHistory.uuid}`)}/>
                        }
                    </Message>
                )}
            </Fragment>
        )
    }
}

export default Anomaly