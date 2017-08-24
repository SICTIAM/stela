import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

import StelaTable from '../../_components/StelaTable'

class LocalAuthorityList extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        localAuthorities: []
    }
    componentDidMount() {
        fetch('/api/acte/localAuthority', { credentials: 'same-origin' })
            .then(this.checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ localAuthorities: json }))
    }
    render() {
        const { t } = this.context
        return (
            <div>
                <h1>{t('admin.modules.local_authority_settings')}</h1>
                <StelaTable
                    data={this.state.localAuthorities}
                    metaData={[
                        { property: 'uuid', displayed: false, searchable: false },
                        { property: 'name', displayed: true, displayName: t('local_authority.name'), searchable: true },
                        { property: 'siren', displayed: true, displayName: t('local_authority.siren'), searchable: true },
                        { property: 'department', displayed: true, displayName: t('local_authority.department'), searchable: false },
                        { property: 'district', displayed: true, displayName: t('local_authority.district'), searchable: false },
                        { property: 'nature', displayed: true, displayName: t('local_authority.nature'), searchable: false },
                        { property: 'nomenclatureDate', displayed: true, displayName: t('local_authority.nomenclatureDate'), searchable: false },
                        { property: 'miatConnectionStatus', displayed: false, searchable: false },
                        { property: 'miatConnectionStartDate', displayed: false, searchable: false },
                        { property: 'miatConnectionEndDate', displayed: false, searchable: false },
                    ]}
                    header={true}
                    link='/admin/actes/parametrage-collectivite/'
                    linkProperty='uuid'
                    noDataMessage='Aucune collectivité'
                    keyProperty='uuid' />
            </div>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthorityList)