import React, { Component, Fragment } from 'react'
import { Icon } from 'semantic-ui-react'

class CollapsedList extends Component {
    static defaultProps = {
        items: [],
        linesBeforeCollapse: 4,
        uncollapsedLines: 3
    }
    state = {
        collapsed: true
    }
    toggleCollapse = () => this.setState({collapsed: !this.state.collapsed})
    render() {
        const { items, linesBeforeCollapse, uncollapsedLines } = this.props
        const lines = items.length > linesBeforeCollapse && this.state.collapsed ? items.slice(0, uncollapsedLines) : items
        return (
            <Fragment>
                <div style={{ marginTop: '0.5em', marginBottom: '0.5em' }}>
                    {lines}
                </div>
                {items.length > linesBeforeCollapse &&
                    <Icon onClick={this.toggleCollapse} className='collapsed-text-icon' circular name='ellipsis horizontal' />
                }
            </Fragment>
        )
    }
}

export default CollapsedList