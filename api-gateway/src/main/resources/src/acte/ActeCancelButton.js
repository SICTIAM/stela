import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button } from 'semantic-ui-react'

import ConfirmModal from '../_components/ConfirmModal'
import { notifications } from '../_util/Notifications'
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
                    this.context._addNotification(notifications.acte.cancelled)
                    this.setState({ requestSent: true })
                })
                .catch(response => {
                    response.text().then(text => this.context._addNotification(notifications.acte.cancelledForbidden))
                })
        }
    }
    render() {
        const { t } = this.context
        return (
            (this.props.isCancellable && this.props.uuid !== '' && !this.state.requestSent) &&
            <ConfirmModal onConfirm={this.cancelDeposit} text={t('acte.page.cancel_deposit_confirm')}>
                <Button basic color='red'>{t('acte.page.cancel_deposit')}</Button>
            </ConfirmModal>
        )
    }
}

export default translate(['acte'])(ActeCancelButton)