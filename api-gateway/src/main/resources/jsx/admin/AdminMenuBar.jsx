import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { NavLink } from 'react-router-dom'
import { Menu, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class AdminMenuBar extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        return (
            <Menu fixed='left' className='mainMenu admin' inverted secondary vertical>
                <div className='mainMenus'>

                    <Menu.Item style={{ width: '100%' }}>
                        <Icon name='tasks' size='large' />
                        <Menu.Header>Général</Menu.Header>
                        <Menu.Menu>
                            <Menu.Item as={NavLink} to='/admin/tableau-de-bord'>{t('admin.dashboard')}</Menu.Item>
                            <Menu.Item as={NavLink} to='/admin/ma-collectivite'>{t('admin.my_local_authority')}</Menu.Item>
                            <Menu.Item as={NavLink} to='/admin/agents'>{t('admin.users')}</Menu.Item>
                            <Menu.Item as={NavLink} to='/admin/collectivite'>{t('admin.local_authorities')}</Menu.Item>
                        </Menu.Menu>
                    </Menu.Item>

                    <Menu.Item style={{ width: '100%' }}>
                        <Icon name='checkmark box' size='large' />
                        <Menu.Header>{t('menu.acte.legality_control')}</Menu.Header>
                        <Menu.Menu>
                            <Menu.Item as={NavLink} to='/admin/actes/parametrage-module'>Paramètres</Menu.Item>
                        </Menu.Menu>
                    </Menu.Item>

                    <Menu.Item style={{ width: '100%' }}>
                        <Icon name='calculator' size='large' />
                        <Menu.Header>{t('menu.pes.accounting_flow')}</Menu.Header>
                        <Menu.Menu>
                            <Menu.Item as={NavLink} to='/admin/pes/parametrage-module'>Paramètres</Menu.Item>
                        </Menu.Menu>
                    </Menu.Item>
                </div>

                <div>
                    <Menu.Item style={{ width: '100%' }} >
                        <img style={{ width: '100%', padding: '2em' }} src={process.env.PUBLIC_URL + '/img/logo_sictiam_white.png'} alt="SICTIAM" />
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