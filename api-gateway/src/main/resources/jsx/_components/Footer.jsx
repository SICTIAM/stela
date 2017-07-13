import React, { Component } from 'react'
import { Segment } from 'semantic-ui-react'

class Footer extends Component {
    render() {
        return (
            <footer>
                <Segment textAlign='center' inverted color='blue' secondary basic>
                    Made with ❤ by SICTIAM
                </Segment>
            </footer>
        )
    }
}

export default Footer