import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Input, Button } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class InputButton extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        initialValue: '',
        value: '',
        edit: false
    }
    styles = {
        opacity: 1,
    }
    componentWillReceiveProps(nextProps) {
        this.setState({ initialValue: nextProps.value, value: nextProps.value, edit: false })
    }
    handleClick = () => {
        if (this.state.edit) this.props.validateInput(this.props.id, this.state.value)
        else this.setState({ edit: true })
    }
    handleChange = (e) => {
        const { value } = e.target
        this.setState({ value: value })
    }
    render() {
        const icon = this.state.edit ? 'checkmark' : 'write'
        const color = this.state.edit ? 'blue' : 'grey'
        return (
            <Input className='partialUpdateInput'
                id={this.props.id}
                disabled={!this.state.edit}
                focus={this.state.edit}
                value={this.state.value}
                onChange={this.handleChange} action>
                <input />
                <Button color={color} icon={icon} onClick={this.handleClick} />
            </Input>
        )
    }
}

export default translate(['api-gateway'])(InputButton)