import React, { Component } from 'react'
import PropTypes from 'prop-types'
import RichTextEditor from 'react-rte'

class TextEditor extends Component {
    static propTypes = {
        onChange: PropTypes.func.isRequired,
        text: PropTypes.string.isRequired
    }
    state = {
        value: RichTextEditor.createEmptyValue(),
        isSet: false
    }
    componentWillReceiveProps(nextProps) {
        if (!this.state.isSet && nextProps.text) {
            let newValue = RichTextEditor.createValueFromString(nextProps.text, 'markdown')
            this.setState({ value: newValue, isSet: true })
        }
    }
    onChange = (value) => {
        this.setState({ value })
        if (this.props.onChange) {
            this.props.onChange(value.toString('markdown'))
        }
    }
    render() {
        return (
            <div className='editor'>
                <RichTextEditor
                    value={this.state.value}
                    onChange={this.onChange} />
            </div>
        )
    }
}

export default TextEditor
