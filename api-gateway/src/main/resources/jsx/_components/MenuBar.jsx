import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { NavLink } from 'react-router-dom'
import { Menu, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class MenuBar extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        t: PropTypes.func
    }
    render() {
        const { isLoggedIn, t } = this.context
        return (
            <Menu style={{ backgroundColor: 'white', color: 'red' }} className='mainMenu' fixed='left' secondary vertical >

                <div className='mainMenus'>
                    {isLoggedIn &&
                        <Menu.Item style={{ width: '100%' }}>
                            <Icon name='checkmark box' size='large' />
                            <Menu.Header>{t('menu.acte.legality_control')}</Menu.Header>
                            <Menu.Menu>
                                <Menu.Item as={NavLink} to="/actes/nouveau">{t('menu.acte.submit_an_act')}</Menu.Item>
                                <Menu.Item as={NavLink} to="/actes/liste">{t('menu.acte.list')}</Menu.Item>
                                <Menu.Item as={NavLink} to="/actes/brouillons">{t('menu.acte.drafts')}</Menu.Item>
                                <Menu.Item>{t('menu.acte.deliberation_register')}</Menu.Item>
                            </Menu.Menu>
                        </Menu.Item>
                    }

                    {isLoggedIn &&
                        <Menu.Item style={{ width: '100%' }}>
                            <Icon name='calculator' size='large' />
                            <Menu.Header>{t('menu.pes.accounting_flow')}</Menu.Header>
                            <Menu.Menu>
                                <Menu.Item as={NavLink} to="/pes/nouveau">{t('menu.pes.submit_a_PES_Aller')}</Menu.Item>
                                <Menu.Item as={NavLink} to="/pes/liste">{t('menu.pes.PES_Aller_list')}</Menu.Item>
                                <Menu.Item>{t('menu.pes.PES_Retour_list')}</Menu.Item>
                            </Menu.Menu>
                        </Menu.Item>
                    }

                    {isLoggedIn &&
                        <Menu.Item style={{ width: '100%' }}>
                            <Icon name='calendar outline' size='large' />
                            <Menu.Header>{t('menu.convocation.convocation')}</Menu.Header>
                            <Menu.Menu>
                                <Menu.Item>{t('menu.convocation.send_a_convocation')}</Menu.Item>
                                <Menu.Item>{t('menu.convocation.reveived_convocations')}</Menu.Item>
                                <Menu.Item>{t('menu.convocation.sent_convocations')}</Menu.Item>
                            </Menu.Menu>
                        </Menu.Item>
                    }

                    <Menu.Item style={{ width: '100%' }}>
                        <Icon name='help' size='large' />
                        <Menu.Header>{t('menu.informations.title')}</Menu.Header>
                        <Menu.Menu>
                            <Menu.Item>{t('menu.informations.help')}</Menu.Item>
                            <Menu.Item>{t('menu.informations.contact')}</Menu.Item>
                            <Menu.Item>{t('menu.informations.cgu')}</Menu.Item>
                        </Menu.Menu>
                    </Menu.Item>
                </div>

                <div>
                    <Menu.Item style={{ width: '100%' }} >
                        <img style={{ width: '100%', padding: '2em' }} src={process.env.PUBLIC_URL + '/img/logo_sictiam.jpg'} alt="SICTIAM" />
                    </Menu.Item>

                    <Menu.Item style={{ textAlign: 'center', width: '100%' }}>
                        {t('made_with_love')}
                    </Menu.Item>
                </div>

            </Menu >
        )
    }
}

export default translate(['api-gateway'])(MenuBar)