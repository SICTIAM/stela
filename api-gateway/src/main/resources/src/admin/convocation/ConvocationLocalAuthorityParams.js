import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment } from 'semantic-ui-react'

import { Page } from '../../_components/UI'

class ConvocationLocalAuthorityParams extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	state = {

	}
	render () {
	    return (
	        <Page>
	            <Segment>
					Coucou
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['acte', 'api-gateway'])(ConvocationLocalAuthorityParams)