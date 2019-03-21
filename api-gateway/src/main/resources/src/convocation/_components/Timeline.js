import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import moment from 'moment'
import {Feed, Segment} from 'semantic-ui-react'

class Timeline extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
	static propTypes = {
	    title: PropTypes.string.isRequired,
	    emptyMessage: PropTypes.string.isRequired,
	    history: PropTypes.array
	}
    static defaultProps = {
        title: '',
        emptyMessage: ''
    }
    render() {
        const { t } = this.context
        const { title, emptyMessage, history } = this.props

        const historyEmpty = history.length === 0

        const histories = history.map(status =>
            <Feed.Event key={status.uuid}>
                <Feed.Label>
                </Feed.Label>
                <Feed.Content className='timeline-container'>
                    <Feed.Date>{moment(status.date).format(`DD/MM/YYYY ${t('api-gateway:at')} HH:mm`)}</Feed.Date>
                    <Feed.Summary>
                        {t(`convocation.status.${status.type}`) + (status.message !== null ? ' : ' + status.message : '')}
                    </Feed.Summary>
                </Feed.Content>
            </Feed.Event>)
        return (
            <Segment>
                <h2>{title}</h2>
                {!historyEmpty && (
                    <Feed className="timeline">{histories}</Feed>
                )}
                {historyEmpty && (
                    <p>{emptyMessage}</p>
                )}
            </Segment>
        )
    }
}

export default translate(['convocation', 'api-gateway'])(Timeline)