import React, { Component } from 'react'
import { Icon } from 'semantic-ui-react'
import PropTypes from 'prop-types'

export default class Chip extends Component {
	static propTypes = {
	    text: PropTypes.string.isRequired,
	    onRemoveChip: PropTypes.func,
	    removable: PropTypes.bool
	}
	render() {
	    return (
	        <div className='chip'>
	            {this.props.text}
	            {this.props.removable && (
	                <div className="remove" role='presentation' onClick={this.props.onRemoveChip}>
	                    <Icon color='red' name='remove'/>
                	</div>
	            )}

	        </div>
	    )
	}
}