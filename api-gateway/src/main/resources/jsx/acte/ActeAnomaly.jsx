import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Message } from 'semantic-ui-react'

import { anomalies } from '../_util/constants'

class ActeAnomaly extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static defaultProps = {
        lastHistory: {
            status: '',
            message: ''
        }
    }
    render() {
        const { t } = this.context
        const { lastHistory } = this.props
        return (
            renderIf(anomalies.includes(lastHistory.status) && lastHistory.message)(
                <Message negative>
                    <Message.Header>{t('acte.history.message')}</Message.Header>
                    <p>{lastHistory.message}</p>
                </Message>
            )
        )
    }
}

export default translate(['acte'])(ActeAnomaly)