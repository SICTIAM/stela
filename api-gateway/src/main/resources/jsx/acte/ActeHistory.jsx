import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Feed } from 'semantic-ui-react'

class ActeHistory extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context

        const historyEmpty = renderIf(this.props.history.length === 0)
        const historyNotEmpty = renderIf(this.props.history.length > 0)

        const acteHistory = this.props.history.map(status =>
            <Feed.Event key={status.status} icon='check' date={status.date} summary={t(`acte.status.${status.status}`)} />
        )
        return (
            <div className='secondContent'>
                <h2>{t('acte.page.historic')}</h2>
                {historyNotEmpty(
                    <Feed >
                        {acteHistory}
                    </Feed>
                )}
                {historyEmpty(
                    <p>{t('acte.page.no_history')}</p>
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(ActeHistory)