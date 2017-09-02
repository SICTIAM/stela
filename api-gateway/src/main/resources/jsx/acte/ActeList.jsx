import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'
import { translate } from 'react-i18next'

import StelaTable from '../_components/StelaTable'

class ActeList extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        actes: []
    }
    componentDidMount() {
        fetch('/api/acte/', { credentials: 'same-origin' })
            .then(this.checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ actes: json }))
    }
    render() {
        const { t } = this.context
        const statusDisplay = (status) => t(`acte.status.${status}`)
        const natureDisplay = (nature) => t(`acte.nature.${nature}`)
        const decisionDisplay = (decision) => moment(decision).format('DD/MM/YYYY')
        return (
            <div>
                <h1>{t('acte.list.title')}</h1>
                <StelaTable
                    data={this.state.actes}
                    metaData={[
                        { property: 'uuid', displayed: false, searchable: false },
                        { property: 'number', displayed: true, displayName: t('acte.fields.number'), searchable: true },
                        { property: 'decision', displayed: true, displayName: t('acte.fields.decision'), searchable: true, displayComponent: decisionDisplay },
                        { property: 'nature', displayed: true, displayName: t('acte.fields.nature'), searchable: true, displayComponent: natureDisplay },
                        { property: 'code', displayed: false, searchable: false },
                        { property: 'title', displayed: true, displayName: t('acte.fields.title'), searchable: true },
                        { property: 'creation', displayed: false, searchable: false },
                        { property: 'status', displayed: true, displayName: t('acte.fields.status'), searchable: true, displayComponent: statusDisplay },
                        { property: 'lastUpdateTime', displayed: false, searchable: false },
                        { property: 'public', displayed: false, searchable: false },
                    ]}
                    header={true}
                    link='/actes/'
                    linkProperty='uuid'
                    noDataMessage='Aucun acte'
                    keyProperty='uuid' />
            </div>
        )
    }
}

export default translate(['api-gateway'])(ActeList)