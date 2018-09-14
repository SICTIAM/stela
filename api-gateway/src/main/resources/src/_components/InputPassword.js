import React, { Component } from 'react'
import { Input, Icon } from 'semantic-ui-react'

class InputPassword extends Component {
    state = {
        type: 'password'
    }
    changeType = () => {
        const type = this.state.type === 'password' ? 'text' : 'password'
        this.setState({ type })
    }
    render() {
        const { type } = this.state
        const iconName = type === 'text' ? 'eye slash' : 'eye'
        return (
            <Input {...this.props} type={type}
                icon={<Icon name={iconName} circular link onClick={this.changeType}/>} />
        )
    }
}

export default InputPassword