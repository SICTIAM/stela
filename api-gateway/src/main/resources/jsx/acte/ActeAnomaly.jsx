import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Segment } from 'semantic-ui-react'

import { Field } from '../_components/UI'

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
            renderIf(lastHistory.status === 'NACK_RECEIVED')(
                <Segment color='red'>
                    <h2>{t('acte.page.detail_anomaly')}</h2>
                    <Field htmlFor="message" label={t('acte.history.message')}>
                        <span id="message">{lastHistory.message}</span>
                    </Field>
                </Segment >
            )
        )
    }
}

export default translate(['api-gateway'])(ActeAnomaly)