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
        const { collapsed } = this.state
        const { items, linesBeforeCollapse, uncollapsedLines } = this.props
        const lines = items.length > linesBeforeCollapse && collapsed ? items.slice(0, uncollapsedLines) : items
        return (
            <Fragment>
                <div style={{ marginTop: '0.5em', marginBottom: '0.5em' }}>
                    {lines}
                </div>
                {items.length > linesBeforeCollapse &&
                    <Icon onClick={this.toggleCollapse} className='collapsed-text-icon' circular name={collapsed ? 'ellipsis horizontal' : 'angle up'} />
                }
            </Fragment>
        )
    }
}

export default CollapsedList