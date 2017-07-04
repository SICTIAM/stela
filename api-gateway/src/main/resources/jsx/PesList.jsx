import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'

import StelaTable from './components/StelaTable'

class PesList extends Component {
    static contextTypes = {
        t: PropTypes.func
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
        const { t } = this.context
        const statusDisplay = (status) => t(`pes.status.${status}`)
        return (
            <div>
                <h1>{t('pes.pes_list_title')}</h1>

                {renderIf(this.state.pess && this.state.pess.length > 0)(
                    <StelaTable
                        data={this.state.pess}
                        metaData={[
                            { property: 'uuid', displayed: false, searchable: false },
                            { property: 'creationDate', displayed: true, displayName: t('pes.pes_list_column_creationDate'), searchable: true },
                            { property: 'title', displayed: true, displayName: t('pes.pes_list_column_title'), searchable: true },
                            { property: 'file', displayed: false, searchable: false },
                            { property: 'comment', displayed: true, displayName: t('pes.pes_list_column_comment'), searchable: true },
                            { property: 'status', displayed: true, displayName: t('pes.pes_list_column_status'), searchable: false, displayComponent: statusDisplay },
                            { property: 'lastUpdateTime', displayed: true, displayName: t('pes.pes_list_column_lastUpdateTime'), searchable: true }
                        ]}
                        header={true}
                        noDataMessage={t('pes.pes_list_empty')}
                        keyProperty='uuid' />
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(PesList)