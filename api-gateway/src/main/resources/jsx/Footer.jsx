import React, { Component } from 'react'
import { Segment } from 'semantic-ui-react'

class Footer extends Component {
    render() {
        return (
            <Segment textAlign='center' inverted color='blue' secondary basic>
                Made with ❤ by SICTIAM
            </Segment>
        )
    }
}

export default Footer