import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

class Home extends Component {
    static propTypes = {
        t: PropTypes.func.isRequired
    }
    render() {
        const { t } = this.props
        return (
            <div>
                <h1>Stela</h1>
                {t('heavy_development')}
            </div>
        )
    }
}

export default translate(['api-gateway'])(Home)