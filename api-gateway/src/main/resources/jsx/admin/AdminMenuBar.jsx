import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { Menu, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class AdminMenuBar extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        return (
            <Menu color='blue' fixed='left' className='mainMenu' inverted secondary vertical>
                <div className='mainMenus'>
                    <Menu.Item as={Link} to="/admin/tableau-de-bord">
                        <Icon name='dashboard' size='large' /> {t('admin.dashboard')}
                    </Menu.Item>
                    <Menu.Item as={Link} to='/admin/ma-collectivite'>
                        <Icon name='building' size='large' /> {t('admin.my_local_authority')}
                    </Menu.Item>
                    <Menu.Item as={Link} to='/admin/collectivite'>
                        <Icon name='building' size='large' /> {t('admin.local_authorities')}
                    </Menu.Item>
                    <Menu.Item as={Link} to='/admin/actes/parametrage-module'>
                        <Icon name='checkmark box' size='large' /> {t('acte:admin.modules.acte.module_settings.title')}
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

export default translate(['api-gateway', 'acte'])(AdminMenuBar)