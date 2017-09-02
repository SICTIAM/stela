import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { Menu } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class AdminMenuBar extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    styles = {
        appTitle: {
            textAlign: 'center',
        },
        logo: {
            width: '100%',
            padding: '2em'
        }
    }
    render() {
        const { t } = this.context
        return (
            <Menu color='blue' fixed='left' inverted vertical>

                <Menu.Item as={Link} to="/" header>
                    <h1 style={this.styles.appTitle}>{t('app_title')}</h1>
                </Menu.Item>

                <Menu.Item as={Link} to="/admin/tableau-de-bord">
                    {t('admin.dashboard')}
                </Menu.Item>

                <Menu.Item>
                    {t('admin.users')}
                </Menu.Item>

                <Menu.Item>
                    {t('admin.collectivities')}
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('admin.modules.acte.title')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item>{t('admin.modules.configuration')}</Menu.Item>
                        <Menu.Item as={Link} to='/admin/actes/parametrage-collectivite'>{t('admin.modules.local_authority_settings')}</Menu.Item>
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    {t('admin.modules.pes')}
                </Menu.Item>

                <Menu.Item>
                    {t('admin.modules.convocation')}
                </Menu.Item>

                <Menu.Item as={Link} to="/" header>
                    {t('admin.back_to_stela')}
                </Menu.Item>

            </Menu>
        )
    }
}

export default translate(['api-gateway'])(AdminMenuBar)