import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import moment from 'moment'
import {Feed, Icon, Segment} from 'semantic-ui-react'

import { getHistoryStatusTranslationKey } from '../_util/utils'
import CollapsedList from './CollapsedList'
import { LinkFile, PesErrorList } from './UI'

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
        const { title, moduleName, emptyMessage, history, sesileClasseurUrl } = this.props

        const historyEmpty = history.length === 0
        const historyNotEmpty = history.length > 0

        const histories = history.map(status =>
            <Feed.Event key={status.uuid}>
                <Feed.Label icon='check' />
                <Feed.Content>
                    <Feed.Date>{moment(status.date).format(`DD/MM/YYYY ${t('api-gateway:at')} HH:mm`)}</Feed.Date>
                    <Feed.Summary>
                        {t(getHistoryStatusTranslationKey(moduleName, status))}
                        {(sesileClasseurUrl !== null && status.status === 'PENDING_SIGNATURE') &&
                        <a className='item' href={sesileClasseurUrl} target='_blank'>
                            {` ${t('pes:pes.page.see_classeur')} `}
                            <Icon name="external"/>
                        </a>
                        }
                    </Feed.Summary>
                    {status.message &&
                        <Feed.Extra>{status.message}</Feed.Extra>
                    }
                    {status.errors && <CollapsedList items={PesErrorList(status.errors, `history-${status.uuid}`)}/>}
                    {(status.attachment && status.attachment.filename /* PES histories */) &&
                    <Feed.Extra>
                        {t(`${moduleName}:${moduleName}.page.linked_file`)}:
                        <LinkFile text={status.attachment.filename}
                            url={`/api/${moduleName}/${status[`${moduleName}Uuid`]}/history/${status.uuid}/file`} />
                    </Feed.Extra>
                    }
                    {(status.fileName /* Acte histories */) &&
                    <Feed.Extra>
                        {t(`${moduleName}:${moduleName}.page.linked_file`)}:
                        <LinkFile text={status.fileName}
                            url={`/api/${moduleName}/${status[`${moduleName}Uuid`]}/history/${status.uuid}/file`} />
                    </Feed.Extra>
                    }
                </Feed.Content>
            </Feed.Event>
        )
        return (
            <Segment>
                <h2>{title}</h2>
                {historyNotEmpty && (
                    <Feed>{histories}</Feed>
                )}
                {historyEmpty && (
                    <p>{emptyMessage}</p>
                )}
            </Segment>
        )
    }
}

export default translate(['acte', 'pes', 'api-gateway'])(History)