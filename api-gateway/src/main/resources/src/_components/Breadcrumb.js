import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import PropTypes from 'prop-types'

export default class Breadcrumb extends Component {
	static propTypes = {
	    data: PropTypes.array.isRequired
	}
	render() {
	    const breadcrumb = this.props.data.map((item, index) => {
	        if(item.url) {
	            return index > 0 ? <Link key={item.title} to={item.url}>{` > ${item.title}`}</Link> : <Link key={item.title} to={item.url}>{item.title}</Link>
	        } else {
	            return index > 0 ? <span key={item.title}>{` > ${item.title}`}</span> : <span>{item.title}</span>
	        }
	    })
	    return (
	        <div>{breadcrumb}</div>
	    )
	}
}