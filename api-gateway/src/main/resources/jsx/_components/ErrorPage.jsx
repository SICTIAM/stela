import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

class ErrorPage extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        return (
            <div>
                <h1>{t(`error.${this.props.error}.title`)}</h1>
                <p>{t(`error.${this.props.error}.content`)}</p>
            </div>
        )
    }
}

export default translate('api-gateway')(ErrorPage)


