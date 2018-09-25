import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { InputFile } from './UI'

class NewTar extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    static defaultProps = {
        uuid: ''
    }
    saveDraftAttachment = (file) => {
        const { _fetchWithAuthzHandling } = this.context
        if (file) {
            const url = `/api/acte/${this.props.uuid}/newTar`
            const data = new FormData()
            data.append('file', file)
            _fetchWithAuthzHandling({ url: url, body: data, method: 'POST', context: this.context })
                .then(() => console.log('done'))
        }
    }
    render() {
        return (
            <InputFile htmlFor='newTar' label='newTar'>
                <input id='newTar'
                    type='file'
                    style={{ display: 'none' }}
                    onChange={e => this.saveDraftAttachment(e.target.files[0])} />
            </InputFile>
        )
    }
}

export default NewTar