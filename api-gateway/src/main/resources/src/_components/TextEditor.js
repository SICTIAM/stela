import React, { Component } from 'react'
import PropTypes from 'prop-types'
import RichTextEditor from 'react-rte'

class TextEditor extends Component {
    static propTypes = {
        onChange: PropTypes.func.isRequired,
        text: PropTypes.string.isRequired,
        isSet: PropTypes.bool,
        format: PropTypes.string
    }
    static defaultProps = {
        isSet: true,
        format: 'markdown'
    }
    state = {
        value: RichTextEditor.createEmptyValue(),
        isSet: false
    }
    componentDidMount() {
        if(this.props.text) {
            let newValue = RichTextEditor.createValueFromString(this.props.text, this.props.format)
            this.setState({ value: newValue, isSet: true })
        }
    }
    componentWillReceiveProps(nextProps) {
        if (!this.state.isSet && nextProps.text) {
            let newValue = RichTextEditor.createValueFromString(nextProps.text, this.props.format)
            this.setState({ value: newValue, isSet: true })
        }
    }
    componentDidUpdate(prevProps, prevState) {
        const { text, isSet } = this.props
        if(text && text !== prevProps.text && (!this.state.isSet || !isSet)) {
            let newValue = RichTextEditor.createValueFromString(text, this.props.format)
            this.setState({ value: newValue, isSet: true })
        }
    }
    onChange = (value) => {
        this.setState({ value })
        if (this.props.onChange) {
            this.props.onChange(value.toString(this.props.format))
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
