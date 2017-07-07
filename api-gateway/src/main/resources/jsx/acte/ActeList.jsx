import React, { Component } from 'react'
import PropTypes from 'prop-types'
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
        // TODO : fetch real actes
    }
    render() {
        const { t } = this.context
        const statusDisplay = (status) => t(`acte.status.${status}`)
        const natureDisplay = (nature) => t(`acte.nature.${nature}`)
        return (
            <div>
                <h1>{t('acte.list.title')}</h1>
                <StelaTable
                    data={[
                        {
                            uuid: "041334b4-36df-4de0-9796-978306dc093f",
                            number: "001",
                            decisionDate: 1499288997571,
                            nature: "DELIBERATION",
                            code: "1-0-0-1-0",
                            title: "STELA 3 sera fini en Décembre",
                            creationDate: 1499288997614,
                            status: "CREATED",
                            lastUpdateTime: 1499288997614,
                            public: true
                        },
                        {
                            uuid: "a2fc06bc-911a-481b-97ce-d1ccd13951a7",
                            number: "002",
                            decisionDate: 1499288997630,
                            nature: "DELIBERATION",
                            code: "1-0-0-1-0",
                            title: "SESILE 4 sera fini quand il sera fini",
                            creationDate: 1499288997632,
                            status: "CREATED",
                            lastUpdateTime: 1499288997632,
                            public: true
                        },
                        {
                            uuid: "e3eb961b-7979-4808-b75c-04b59bb7531e",
                            number: "003",
                            decisionDate: 1499288997635,
                            nature: "DELIBERATION",
                            code: "1-0-0-1-0",
                            title: "Le DC Exporter sera mis aux oubliettes",
                            creationDate: 1499288997638,
                            status: "CREATED",
                            lastUpdateTime: 1499288997638,
                            public: true
                        }
                    ]}
                    metaData={[
                        { property: 'uuid', displayed: false, searchable: false },
                        { property: 'number', displayed: true, displayName: t('acte.list.table.number'), searchable: true },
                        { property: 'decisionDate', displayed: true, displayName: t('acte.list.table.decisionDate'), searchable: true },
                        { property: 'nature', displayed: true, displayName: t('acte.list.table.nature'), searchable: true, displayComponent: natureDisplay },
                        { property: 'code', displayed: false, searchable: false },
                        { property: 'title', displayed: true, displayName: t('acte.list.table.title'), searchable: true },
                        { property: 'creationDate', displayed: false, searchable: false },
                        { property: 'status', displayed: true, displayName: t('acte.list.table.status'), searchable: true, displayComponent: statusDisplay },
                        { property: 'lastUpdateTime', displayed: false, searchable: false },
                        { property: 'public', displayed: false, searchable: false },
                    ]}
                    header={true}
                    link='/acte/'
                    linkProperty='uuid'
                    noDataMessage='Aucun acte'
                    keyProperty='uuid' />
            </div>
        )
    }
}

export default translate(['api-gateway'])(ActeList)