import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { NavLink } from 'react-router-dom'
import { Menu, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

import { withAuthContext } from '../Auth'

import { rightsModuleResolver, rightsFeatureResolver } from '../_util/utils'
import { getLocalAuthoritySlug } from '../_util/utils'

class AdminMenuBar extends Component {
    static contextTypes = {
        t: PropTypes.func,
        isMenuOpened: PropTypes.bool,
    }
    checkActivatedModule = (module) => {
        const { profile } = this.props.authContext
        return profile && profile.localAuthority && profile.localAuthority.activatedModules.includes(module)
    }
    render() {
        const { t, isMenuOpened } = this.context
        const { userRights, profile } = this.props.authContext
        const localAuthoritySlug = getLocalAuthoritySlug()
        const rights = userRights
        return (
            <Menu style={{ backgroundColor: 'white' }} fixed='left' className={'mainMenu secondary' + (isMenuOpened ? ' open' : '')} secondary vertical>
                <div className='mainMenus'>

                    {profile && profile.admin && (
                        <Fragment>
                            <Menu.Item style={{ width: '100%' }}>
                                <Menu.Header className="secondary">
									Général
                                    <Icon name='tasks' size='large' className="float-right"/>
                                </Menu.Header>
                                <Menu.Menu>
                                    <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/ma-collectivite`}>{t('admin.my_local_authority')}</Menu.Item>
                                    <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/parametrage-instance`}>{t('admin.instance_params.instance_params')}</Menu.Item>
                                    <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/compte-generique/liste`}>{t('admin.generic_account.title')}</Menu.Item>
                                </Menu.Menu>
                            </Menu.Item>

                            {this.checkActivatedModule('ACTES') && (
                                <Menu.Item style={{ width: '100%' }}>
                                    <Menu.Header className="secondary">
                                        {t('menu.acte.legality_control')}
                                        <Icon name='checkmark box' className="float-right" size='large' />
                                    </Menu.Header>
                                    <Menu.Menu>
                                        <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/actes/parametrage-module`}>{t('admin.parameters')}</Menu.Item>
                                    </Menu.Menu>
                                </Menu.Item>
                            )}
                            {this.checkActivatedModule('PES') && (
                                <Menu.Item style={{ width: '100%' }}>
                                    <Menu.Header className="secondary">
                                        {t('menu.pes.accounting_flow')}
                                        <Icon name='calculator' size='large' className="float-right" />
                                    </Menu.Header>
                                    <Menu.Menu>
                                        <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/pes/parametrage-module`}>{t('admin.parameters')}</Menu.Item>
                                    </Menu.Menu>
                                </Menu.Item>
                            )}
                        </Fragment>
                    )}

                    {this.checkActivatedModule('CONVOCATION') && rightsModuleResolver(rights, 'CONVOCATION') && rightsFeatureResolver(rights, ['CONVOCATION_ADMIN']) && (
                        <Menu.Item style={{ width: '100%' }}>
                            <Menu.Header className="secondary">
                                {t('menu.convocation.convocation')}
                                <Icon name='calendar outline' size='large' className="float-right" />
                            </Menu.Header>
                            <Menu.Menu>
                                <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/ma-collectivite/convocation`}>{t('admin.parameters')}</Menu.Item>
                                <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/convocation/type-assemblee/nouveau`}>{t('admin.convocation.assembly_type')}</Menu.Item>
                                <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/convocation/type-assemblee/liste-type-assemblee`}>{t('admin.convocation.assembly_type_list')}</Menu.Item>
                                <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/convocation/destinataire/nouveau`}>{t('admin.convocation.recipent')}</Menu.Item>
                                <Menu.Item as={NavLink} to={`/${localAuthoritySlug}/admin/convocation/destinataire/liste-destinataires`}>{t('admin.convocation.recipient_list')}</Menu.Item>
                            </Menu.Menu>
                        </Menu.Item>
                    )}
                </div>

                <div>
                    <Menu.Item style={{ width: '100%' }} >
                        <img style={{ width: '100%', padding: '2em' }} src={process.env.PUBLIC_URL + '/img/logo_sictiam.jpg'} alt="SICTIAM" />
                    </Menu.Item>
                    <Menu.Item style={{ textAlign: 'center', width: '100%' }}>
                        Créé avec ❤ par le{' '}
                        <a style={{ color: '#c06 !important', fontWeight: 'bold' }} href="https://www.sictiam.fr/" target="_blank" rel="noopener noreferrer">
                            SICTIAM
                        </a>
                    </Menu.Item>
                </div>
            </Menu>
        )
    }
}

export default translate(['api-gateway', 'acte', 'pes'])(withAuthContext(AdminMenuBar))
