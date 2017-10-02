import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Button } from 'semantic-ui-react'

import { acteCancelledSuccess, acteCancelledForbidden } from '../_components/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'

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
    cancelDeposit = () => {
        const uuid = this.props.uuid
        if (this.props.isCancellable && uuid !== '') {
            fetchWithAuthzHandling({ url: '/api/acte/' + uuid + '/status/cancel', method: 'POST', context: this.context })
                .then(checkStatus)
                .then(() => {
                    this.context._addNotification(acteCancelledSuccess(this.context.t))
                    this.setState({ requestSent: true })
                })
                .catch(response => {
                    response.text().then(text => this.context._addNotification(acteCancelledForbidden(this.context.t)))
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

export default translate(['acte'])(ActeCancelButton)