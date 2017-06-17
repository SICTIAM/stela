import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import { Menu, Container, Button } from 'semantic-ui-react'

class MenuBar extends Component {
    render() {
        return (
            <Menu fixed='top'>
                <Container>
                    <Menu.Item as={Link} to="/" header>stela</Menu.Item>
                    <Menu.Item name='Dashboard' />
                    <Menu.Item name='Acte' as={Link} to="/miat" />
                    <Menu.Item name='PES' as={Link} to="/pes" />
                    <Menu.Menu position='right'>
                        <Menu.Item>
                            <Button primary>Sign in</Button>
                        </Menu.Item>
                    </Menu.Menu>
                </Container>
            </Menu>
        )
    }
}

export default MenuBar