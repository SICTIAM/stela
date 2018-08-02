import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Header } from 'semantic-ui-react'

import { File } from '../_components/UI'

class Defere extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static defaultProps = {
        acteUuid: '',
        acteHistories: []
    }
    render() {
        const { t } = this.context
        const { acteHistories } = this.props
        const defereHistory = acteHistories.find(acteHistory => acteHistory.status === 'DEFERE_RECEIVED')
        return (
            <Segment color='orange'>
                <Header size='small'>{t('acte.page.defere.received')}</Header>
                <File attachment={{ filename: defereHistory.fileName }}
                    src={`/api/acte/${this.props.acteUuid}/history/${defereHistory.uuid}/file`} />
            </Segment>
        )
    }
}

export default translate(['acte'])(Defere)