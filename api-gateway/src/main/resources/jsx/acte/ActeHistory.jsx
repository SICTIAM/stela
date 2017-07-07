import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Step } from 'semantic-ui-react'

class ActeHistory extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        const history = [
            {
                acteUuid: "a31b362c-c74f-4cc9-ba2b-12050f0d1a41",
                status: "CREATED",
                date: 1499422508552
            },
            {
                acteUuid: "a31b362c-c74f-4cc9-ba2b-12050f0d1a41",
                status: "SENT_INITIATED",
                date: 1499422508555
            },
            {
                acteUuid: "a31b362c-c74f-4cc9-ba2b-12050f0d1a41",
                status: "ANTIVIRUS_OK",
                date: 1499422508559
            }
        ]
        const acteHistory = history.map(status =>
            <Step key={status.status} completed title={t(`acte.status.${status.status}`)} description={status.date} />
        )
        return (
            <div>
                <h2>Historique</h2>
                <Step.Group ordered>
                    {acteHistory}
                </Step.Group>
            </div>
        )
    }
}

export default translate(['api-gateway'])(ActeHistory)