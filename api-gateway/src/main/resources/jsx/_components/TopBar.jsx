import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Menu, Dropdown, Container } from 'semantic-ui-react'

class TopBar extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        t: PropTypes.func
    }
    render() {
        const { isLoggedIn, t } = this.context
        return (
            <Menu className='topBar' fixed='top' secondary>
                <Container>
                    <Menu.Menu position='right'>
                        {isLoggedIn &&
                            <Dropdown item text='John Doe'>
                                <Dropdown.Menu>
                                    <Dropdown.Item>{t('top_bar.params')}</Dropdown.Item>
                                    <Dropdown.Item onClick={() => window.location.href = '/logout'}>{t('top_bar.log_out')}</Dropdown.Item>
                                </Dropdown.Menu>
                            </Dropdown>
                        }
                        {!isLoggedIn &&
                            <Menu.Item>
                                <Button primary onClick={() => window.location.href = '/login'}>{t('top_bar.log_in')}</Button>
                            </Menu.Item>
                        }
                    </Menu.Menu>
                </Container>
            </Menu>
        )
    }
}

export default translate('api-gateway')(TopBar)