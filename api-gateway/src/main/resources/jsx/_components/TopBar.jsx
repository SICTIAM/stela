import React, { Component } from 'react'
import { Menu, Dropdown, Container, Label } from 'semantic-ui-react'

class TopBar extends Component {
    render() {
        return (
            <Menu fixed='top' secondary>
                <Container>
                    <Menu.Menu position='right'>
                        <Menu.Item>
                            <Label color='blue'>3</Label>
                        </Menu.Item>
                        <Dropdown item text='John Doe'>
                            <Dropdown.Menu>
                                <Dropdown.Item>Paramètres</Dropdown.Item>
                                <Dropdown.Item>Déconnexion</Dropdown.Item>
                            </Dropdown.Menu>
                        </Dropdown>
                    </Menu.Menu>
                </Container>
            </Menu>
        )
    }
}

export default TopBar