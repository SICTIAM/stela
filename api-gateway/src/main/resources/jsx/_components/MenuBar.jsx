import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { Menu } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class MenuBar extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        return (
            <Menu className='mainMenu' fixed='left' vertical>

                <Menu.Item className='appTitle' as={Link} to="/" header>
                    <h1 style={{ textAlign: 'center' }}>{t('app_title')}</h1>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('menu.acte.legality_control')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item as={Link} to="/actes/nouveau">{t('menu.acte.submit_an_act')}</Menu.Item>
                        <Menu.Item as={Link} to="/actes/liste">{t('menu.acte.list')}</Menu.Item>
                        <Menu.Item as={Link} to="/actes/brouillons">{t('menu.acte.drafts')}</Menu.Item>
                        <Menu.Item>{t('menu.acte.deliberation_register')}</Menu.Item>
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('menu.pes.accounting_flow')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item as={Link} to="/pes/nouveau">{t('menu.pes.submit_a_PES_Aller')}</Menu.Item>
                        <Menu.Item as={Link} to="/pes/liste">{t('menu.pes.PES_Aller_list')}</Menu.Item>
                        <Menu.Item>{t('menu.pes.PES_Retour_list')}</Menu.Item>
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('menu.convocation.convocation')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item>{t('menu.convocation.send_a_convocation')}</Menu.Item>
                        <Menu.Item>{t('menu.convocation.reveived_convocations')}</Menu.Item>
                        <Menu.Item>{t('menu.convocation.sent_convocations')}</Menu.Item>
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item as={Link} to="/admin" header>
                    Admin
                </Menu.Item>

                <Menu.Item>
                    <img style={{ width: '100%', padding: '2em' }} src={process.env.PUBLIC_URL + '/img/logo_sictiam.jpg'} alt="SICTIAM" />
                </Menu.Item>

            </Menu >
        )
    }
}

export default translate(['api-gateway'])(MenuBar)