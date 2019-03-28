import React, { Component } from 'react'
import PropTypes from 'prop-types'

export default class Tag extends Component {
	static propTypes = {
	    text: PropTypes.string.isRequired,
	    color: PropTypes.string
	}

	lightenDarkenColor = (hex, lum) => {

	    // validate hex string
	    hex = String(hex).replace(/[^0-9a-f]/gi, '')
	    if (hex.length < 6) {
	        hex.split('').map(function(c) {
	            // iterate and update
	            return c + c
	            // join the updated array
			  }).join('')
	    }
	    lum = lum || 0

	    // convert to decimal and change luminosity
	    var rgb = '#', c, i
	    for (i = 0; i < 3; i++) {
	        c = parseInt(hex.substr(i*2,2), 16)
	        c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16)
	        rgb += ('00'+c).substr(c.length)
	    }

	    return rgb
	}

	render() {
	    const { text, color } = this.props
	    return (
	        <div className='tag' style={{backgroundColor: color, color: this.lightenDarkenColor(color, 3), borderColor: this.lightenDarkenColor(color, -0.1)}}>
	            {text}
	        </div>
	    )
	}
}