import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Header, Segment, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class AccordionSegment extends Component {
    static propTypes = {
        isDefaultOpen: PropTypes.bool,
        title: PropTypes.any.isRequired,
        content: PropTypes.any.isRequired
    }
    static defaultProps = {
        isDefaultOpen: true
    }
    state = {
        isOpen: this.props.isDefaultOpen
    }
    toggle = () => this.setState({ isOpen: !this.state.isOpen })
    render() {
        const { isOpen } = this.state
        return (
            <div style={{ marginBottom: '1em' }}>
                <Header style={{ display: 'flex', justifyContent: 'space-between', cursor: 'pointer' }}
                    onClick={this.toggle} attached='top' block>
                    {this.props.title}
                    <Icon name={'caret ' + (isOpen ? 'down' : 'left')} />
                </Header>
                {isOpen && (
                    <Segment attached='bottom'>{this.props.content}</Segment>
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(AccordionSegment)