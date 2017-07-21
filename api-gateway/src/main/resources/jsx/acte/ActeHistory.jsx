import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Feed } from 'semantic-ui-react'

class ActeHistory extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        history: [],
        historyFetched: false
    }
    componentDidMount() {
        const uuid = this.props.uuid
        if (uuid !== '') {
            fetch('/api/acte/' + uuid + '/history', { credentials: 'same-origin' })
                .then(response => response.json())
                .then(json => this.setState({ history: json, historyFetched: true }))
        }
    }
    render() {
        const { t } = this.context

        const historyFetchedEmpty = renderIf(this.state.historyFetched && this.state.history.length === 0)
        const historyFetchedNotEmpty = renderIf(this.state.historyFetched && this.state.history.length > 0)
        const historyNotFetched = renderIf(!this.state.historyFetched)

        const acteHistory = this.state.history.map(status =>
            <Feed.Event key={status.status} icon='check' date={status.date} summary={t(`acte.status.${status.status}`)} />
        )
        return (
            <div className='secondContent'>
                <h2>{t('acte.page.historic')}</h2>
                {historyFetchedNotEmpty(
                    <Feed >
                        {acteHistory}
                    </Feed>
                )}
                {historyFetchedEmpty(
                    <p>{t('acte.page.no_history')}</p>
                )}
                {historyNotFetched(
                    <p>{t('acte.page.non_existing_act')}</p>
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(ActeHistory)