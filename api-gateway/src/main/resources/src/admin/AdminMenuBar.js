import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { NavLink } from 'react-router-dom'
import { Menu, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

import { getLocalAuthoritySlug } from '../_util/utils'

class AdminMenuBar extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        return (
            <Menu style={{ backgroundColor: 'white' }} fixed='left' className='mainMenu rosso' secondary vertical>
                <div className='mainMenus'>

                    <Menu.Item style={{ width: '100%' }}>
                        <Icon name='tasks' size='large' />
                        <Menu.Header>Général</Menu.Header>
                        <Menu.Menu>
                            <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/ma-collectivite`}>{t('admin.my_local_authority')}</Menu.Item>
                            <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/agents`}>{t('admin.users')}</Menu.Item>
                            <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/collectivite`}>{t('admin.local_authorities')}</Menu.Item>
                            <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/parametrage-instance`}>Paramètre d'instance</Menu.Item>
                            <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/compte-generique/liste`}>{t('admin.generic_account.title')}</Menu.Item>
                        </Menu.Menu>
                    </Menu.Item>

                    <Menu.Item style={{ width: '100%' }}>
                        <Icon name='checkmark box' size='large' />
                        <Menu.Header>{t('menu.acte.legality_control')}</Menu.Header>
                        <Menu.Menu>
                            <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/actes/parametrage-module`}>Paramètres</Menu.Item>
                        </Menu.Menu>
                    </Menu.Item>

                    <Menu.Item style={{ width: '100%' }}>
                        <Icon name='calculator' size='large' />
                        <Menu.Header>{t('menu.pes.accounting_flow')}</Menu.Header>
                        <Menu.Menu>
                            <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/pes/parametrage-module`}>Paramètres</Menu.Item>
                        </Menu.Menu>
                    </Menu.Item>
                </div>

                <div>
                    <Menu.Item style={{ width: '100%' }} >
                        <img style={{ width: '100%', padding: '2em' }} src={process.env.PUBLIC_URL + '/img/logo_sictiam.jpg'} alt="SICTIAM" />
                    </Menu.Item>
                    <Menu.Item style={{ textAlign: 'center', width: '100%' }}>
                        Créé avec ❤ par le SICTIAM
                    </Menu.Item>
                </div>
            </Menu>
        )
    }
}

export default translate(['api-gateway', 'acte', 'pes'])(AdminMenuBar)