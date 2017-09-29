import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import moment from 'moment'
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
            <Feed.Event key={status.status}>
                <Feed.Label icon='check' />
                <Feed.Content>
                    <Feed.Date>{moment(status.date).format('DD/MM/YYYY hh:mm')}</Feed.Date>
                    <Feed.Summary>{t(`acte.status.${status.status}`)}</Feed.Summary>
                    {renderIf(status.message)(
                        <Feed.Extra>{status.message}</Feed.Extra>
                    )}
                    {renderIf(status.fileName && status.file)(
                        <Feed.Extra>
                            {t('acte.page.linked_file')}: <a target='_blank' href={`/api/acte/${status.acteUuid}/history/${status.uuid}/file`}>{status.fileName}</a>
                        </Feed.Extra>
                    )}
                </Feed.Content>
            </Feed.Event>
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

export default translate(['acte'])(ActeHistory)