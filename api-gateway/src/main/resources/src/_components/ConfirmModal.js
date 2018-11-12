import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { Confirm } from 'semantic-ui-react'
import { translate } from 'react-i18next'

class ConfirmModal extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static propTypes = {
        onConfirm: PropTypes.func.isRequired,
        text: PropTypes.string.isRequired
    }
    state = {
        isConfirmModalOpen: false
    }
    openConfirmModal = () => this.setState({ isConfirmModalOpen: true })
    closeConfirmModal = () => this.setState({ isConfirmModalOpen: false })
    confirm = () => {
        this.props.onConfirm()
        this.closeConfirmModal()
    }
    render() {
        const { t } = this.context
        const { isConfirmModalOpen } = this.state
        const { text } = this.props
        return (
            <Fragment>
                <span role="presentation" onKeyPress={this.handleKeyPress} onClick={this.openConfirmModal}>
                    {this.props.children}
                </span>
                <Confirm
                    open={isConfirmModalOpen}
                    content={text}
                    confirmButton={t('form.confirm')}
                    cancelButton={t('form.cancel')}
                    onCancel={this.closeConfirmModal}
                    onConfirm={this.confirm} />
            </Fragment>
        )
    }
}

export default translate(['api-gateway'])(ConfirmModal)