import React, { Component } from 'react'
import { Container, Menu, Loader } from 'semantic-ui-react'

export default class LazyTemplate extends Component {

    render() {
        return (
            <div>
                <header>
                    <Menu className='topBar primary' fixed="top" secondary></Menu>
                </header>
                <div className="wrapperContainer">
                    <aside>
                        <Menu style={{ backgroundColor: 'white' }} className='mainMenu primary' fixed="left" secondary vertical >
                        </Menu>
                    </aside>
                    <Container className="mainContainer" as="main" id="content">
                        <Loader active inline="centered"></Loader>
                    </Container>
                </div>
            </div>
        )
    }
}