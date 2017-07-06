import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

class NewPes extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        return (
            <div>
                <h1>{t('pes.new.title')}</h1>
                <p>WIP</p>
            </div>
        )
    }
}

export default translate(['api-gateway'])(NewPes)