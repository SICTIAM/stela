import React, { Component } from 'react'
import PropTypes from 'prop-types'

class Overlay extends Component {
	static contextTypes = {
	    _openMenu: PropTypes.fun
	}
	render() {
	    const { _openMenu } = this.context
	    return (
	        <div className="overlay" onClick={_openMenu}></div>
	    )
	}
}

export default (Overlay)