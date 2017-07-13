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

                <Menu.Item as={Link} to="/admin/dashboard">
                    {t('admin.dashboard')}
                </Menu.Item>

                <Menu.Item as={Link} to="/admin/dashboard">
                    {t('admin.users')}
                </Menu.Item>

                <Menu.Item as={Link} to="/admin/dashboard">
                    {t('admin.collectivities')}
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('admin.modules.title')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item name={t('admin.modules.acte')} as={Link} to="/admin/dashboard" />
                        <Menu.Item name={t('admin.modules.pes')} as={Link} to="/admin/dashboard" />
                        <Menu.Item name={t('admin.modules.convocation')} as={Link} to="/admin/dashboard" />
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item as={Link} to="/" header>
                    {t('admin.back_to_stela')}
                </Menu.Item>

            </Menu>
        )
    }
}

export default translate(['api-gateway'])(AdminMenuBar)