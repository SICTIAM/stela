import React, { Component } from 'react'
import PropTypes from 'prop-types'

class Overlay extends Component {
	static contextTypes = {
	    _openMenu: PropTypes.fun
	}
	handleKeyPress = (e) => {
	    const { _openMenu } = this.context
	    if (e.keyCode === 27) {
	        _openMenu()
	    }
	}
	render() {
	    const { _openMenu } = this.context
	    return (
	        <div role="presentation" className="overlay" onClick={_openMenu} onKeyPress={this.handleKeyPress}></div>
	    )
	}
}

export default (Overlay)