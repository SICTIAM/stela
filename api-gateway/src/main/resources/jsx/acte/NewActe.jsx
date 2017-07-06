import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

class NewActe extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        return (
            <div>
                <h1>{t('acte.new.title')}</h1>
                <p>WIP</p>
            </div>
        )
    }
}

export default translate(['api-gateway'])(NewActe)