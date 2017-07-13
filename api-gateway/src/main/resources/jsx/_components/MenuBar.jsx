import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { Menu } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class MenuBar extends Component {
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
            <Menu fixed='left' vertical>

                <Menu.Item as={Link} to="/" header>
                    <h1 style={this.styles.appTitle}>{t('app_title')}</h1>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('menu.acte.legality_control')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item name={t('menu.acte.submit_an_act')} as={Link} to="/acte/new" />
                        <Menu.Item name={t('menu.acte.acts_list')} as={Link} to="/acte/list" />
                        <Menu.Item name={t('menu.acte.deliberation_register')} />
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('menu.pes.accounting_flow')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item name={t('menu.pes.submit_a_PES_Aller')} as={Link} to="/pes/new" />
                        <Menu.Item name={t('menu.pes.PES_Aller_list')} as={Link} to="/pes/list" />
                        <Menu.Item name={t('menu.pes.PES_Retour_list')} />
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>{t('menu.convocation.convocation')}</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item name={t('menu.convocation.send_a_convocation')} />
                        <Menu.Item name={t('menu.convocation.reveived_convocations')} />
                        <Menu.Item name={t('menu.convocation.sent_convocations')} />
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <img style={this.styles.logo} src={process.env.PUBLIC_URL + '/img/logo_sictiam.jpg'} alt="SICTIAM" width="100%" />
                </Menu.Item>

            </Menu>
        )
    }
}

export default translate(['api-gateway'])(MenuBar)