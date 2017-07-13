import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'

import { pesSentSuccess, pesSentVirus, pesSentMissingData } from '../_components/Notifications'
import StelaTable from '../_components/StelaTable'

class PesList extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        pess: []
    }
    componentDidMount() {
        fetch('/api/pes', { credentials: 'same-origin' })
            .then(response => response.json())
            .then(json => this.setState({ pess: json }))
    }
    render() {
        const { t, _addNotification } = this.context
        const statusDisplay = (status) => t(`pes.list.status.${status}`)
        return (
            <div>
                <h1>{t('pes.list.title')}</h1>

                <button onClick={() => _addNotification(pesSentSuccess(t))}>pesSentSuccess</button>
                <button onClick={() => _addNotification(pesSentVirus(t))}>pesSentVirus</button>
                <button onClick={() => _addNotification(pesSentMissingData(t))}>pesSentMissingData</button>

                {renderIf(this.state.pess && this.state.pess.length > 0)(
                    <StelaTable
                        data={this.state.pess}
                        metaData={[
                            { property: 'uuid', displayed: false, searchable: false },
                            { property: 'creationDate', displayed: true, displayName: t('pes.list.table.creationDate'), searchable: true },
                            { property: 'title', displayed: true, displayName: t('pes.list.table.title'), searchable: true },
                            { property: 'file', displayed: false, searchable: false },
                            { property: 'comment', displayed: true, displayName: t('pes.list.table.comment'), searchable: true },
                            { property: 'status', displayed: true, displayName: t('pes.list.table.status'), searchable: false, displayComponent: statusDisplay },
                            { property: 'lastUpdateTime', displayed: true, displayName: t('pes.list.table.lastUpdateTime'), searchable: true }
                        ]}
                        header={true}
                        noDataMessage={t('pes.list.empty')}
                        keyProperty='uuid' />
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(PesList)