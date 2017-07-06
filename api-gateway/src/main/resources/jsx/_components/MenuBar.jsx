import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import { Menu } from 'semantic-ui-react'

class MenuBar extends Component {
    render() {
        const Styles = {
            appTitle: {
                textAlign: 'center',
            },
            logo: {
                width: '100%',
                padding: '2em'
            }
        }
        return (
            <Menu fixed='left' vertical>

                <Menu.Item as={Link} to="/" header>
                    <h1 style={Styles.appTitle}>stela</h1>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>Contrôle de légalité</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item name='Déposet un acte' as={Link} to="/acte/new" />
                        <Menu.Item name='Liste des actes' as={Link} to="/acte/list" />
                        <Menu.Item name='Registre des délibarations' />
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>Flux comptable</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item name='Déposet un PES Aller' as={Link} to="/pes/new" />
                        <Menu.Item name='Liste des PES Aller' as={Link} to="/pes/list" />
                        <Menu.Item name='Liste des PES Retour' />
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <Menu.Header>Convocation</Menu.Header>
                    <Menu.Menu>
                        <Menu.Item name='Déposer une convocation' />
                        <Menu.Item name='Convocations reçues' />
                        <Menu.Item name='Convocations envoyées' />
                    </Menu.Menu>
                </Menu.Item>

                <Menu.Item>
                    <img style={Styles.logo} src={process.env.PUBLIC_URL + '/img/logo_sictiam.jpg'} alt="SICTIAM" width="100%" />
                </Menu.Item>

            </Menu>
        )
    }
}

export default MenuBar