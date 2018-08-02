import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import moment from 'moment'
import { Feed, Segment } from 'semantic-ui-react'

import { getHistoryStatusTranslationKey } from '../_util/utils'
import { LinkFile } from './UI'

class History extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static defaultProps = {
        title: '',
        moduleName: '',
        emptyMessage: ''
    }
    render() {
        const { t } = this.context
        const { title, moduleName, emptyMessage, history } = this.props

        const historyEmpty = renderIf(history.length === 0)
        const historyNotEmpty = renderIf(history.length > 0)

        const histories = history.map(status =>
            <Feed.Event key={status.uuid}>
                <Feed.Label icon='check' />
                <Feed.Content>
                    <Feed.Date>{moment(status.date).format(`DD/MM/YYYY ${t('api-gateway:at')} HH:mm`)}</Feed.Date>
                    <Feed.Summary>
                        {t(getHistoryStatusTranslationKey(moduleName, status))}
                    </Feed.Summary>
                    {renderIf(status.message)(
                        <Feed.Extra>{status.message}</Feed.Extra>
                    )}
                    {renderIf(status.fileName)(
                        <Feed.Extra>
                            {t(`${moduleName}:${moduleName}.page.linked_file`)}: <LinkFile url={`/api/${moduleName}/${status[`${moduleName}Uuid`]}/history/${status.uuid}/file`} text={status.fileName} />
                        </Feed.Extra>
                    )}
                </Feed.Content>
            </Feed.Event>
        )
        return (
            <Segment>
                <h2>{title}</h2>
                {historyNotEmpty(
                    <Feed >
                        {histories}
                    </Feed>
                )}
                {historyEmpty(
                    <p>{emptyMessage}</p>
                )}
            </Segment>
        )
    }
}

export default translate(['acte', 'pes', 'api-gateway'])(History)