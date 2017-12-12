import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import { modules } from '../../_util/constants'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class LocalAuthorityList extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        localAuthorities: []
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/local-authority' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ localAuthorities: json }))
    }
    renderActivatedModule = (activatedModules, moduleName) => activatedModules.includes(moduleName) ? <Icon name='checkmark' color='green' /> : <Icon name='remove' color='red' />
    render() {
        const { t } = this.context
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'siren', displayed: true, displayName: t('local_authority.siren'), searchable: true },
            { property: 'name', displayed: true, displayName: t('local_authority.name'), searchable: true }
        ]
        // TODO: fetch module list from backend
        modules.forEach(moduleName =>
            metaData.push({
                property: 'activatedModules',
                displayed: true,
                displayName: t(`modules.${moduleName}`),
                searchable: false,
                displayComponent: (activatedModules) => this.renderActivatedModule(activatedModules, moduleName)
            })
        )
        return (
            <Segment>
                <h1>{t('admin.modules.local_authority_settings')}</h1>
                <StelaTable
                    data={this.state.localAuthorities}
                    metaData={metaData}
                    header={true}
                    link='/admin/collectivite/'
                    linkProperty='uuid'
                    noDataMessage='Aucune collectivité'
                    keyProperty='uuid' />
            </Segment>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthorityList)