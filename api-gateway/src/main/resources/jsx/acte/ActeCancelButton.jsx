import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Button } from 'semantic-ui-react'

import { acteCancelledSuccess, acteCancelledError } from '../_components/Notifications'

class ActeCancelButton extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        requestSent: false
    }
    checkStatus = (response) => {
        if (response.status >= 200 && response.status < 300) {
            return response
        } else {
            throw response
        }
    }
    cancelDeposit = () => {
        const uuid = this.props.uuid
        if (this.props.isCancellable && uuid !== '') {
            fetch('/api/acte/' + uuid + '/status/cancel', {
                credentials: 'same-origin',
                headers: {
                    [this.context.csrfTokenHeaderName]: this.context.csrfToken
                },
                method: 'POST'
            })
                .then(this.checkStatus)
                .then(() => {
                    this.context._addNotification(acteCancelledSuccess(this.context.t))
                    this.setState({ requestSent: true })
                })
                .catch(response => {
                    response.text().then(text => this.context._addNotification(acteCancelledError(this.context.t)))
                })
        }
    }
    render() {
        const { t } = this.context
        return (
            renderIf(this.props.isCancellable && this.props.uuid !== '' && !this.state.requestSent)(
                <Button basic onClick={this.cancelDeposit} color='red'>{t('acte.page.cancel_deposit')}</Button>
            )
        )
    }
}

export default translate(['api-gateway'])(ActeCancelButton)