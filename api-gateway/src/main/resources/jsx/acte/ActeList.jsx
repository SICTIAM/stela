import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'
import { translate } from 'react-i18next'
import { Accordion, Form, Button } from 'semantic-ui-react'

import StelaTable from '../_components/StelaTable'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import { errorNotification } from '../_components/Notifications'
import { FormFieldInline, FormField } from '../_components/UI'
import { natures, status } from '../_util/constants'

class ActeList extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        actes: [],
        search: {
            number: '',
            title: '',
            nature: '',
            status: '',
        },
        searchDecisionFrom: '',
        searchDecisionTo: ''
    }
    styles = {
        marginBottom: 1 + 'em'
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/acte/' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ actes: json }))
    }
    handleFieldChange = (field, value) => {
        const search = this.state.search
        search[field] = value
        this.setState({ search: search })
    }
    handleChange = (e) => {
        const { id, value } = e.target
        this.setState({ [id]: value })
    }
    submitForm = (event) => {
        event.preventDefault()
        let acteData = Object.assign({}, this.state.search)
        if (acteData.nature === '') delete acteData.nature
        if (acteData.status === '') delete acteData.status
        const data = { acte: acteData }
        if (this.state.searchDecisionFrom !== '') data['decisionFrom'] = this.state.searchDecisionFrom
        if (this.state.searchDecisionTo !== '') data['decisionTo'] = this.state.searchDecisionTo
        const jsonData = JSON.stringify(data)
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        fetchWithAuthzHandling({ url: '/api/acte/query', method: 'POST', body: jsonData, headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ actes: json }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const statusDisplay = (status) => t(`acte.status.${status}`)
        const natureDisplay = (nature) => t(`acte.nature.${nature}`)
        const decisionDisplay = (decision) => moment(decision).format('DD/MM/YYYY')
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const statusOptions = status.map(statusItem =>
            <option key={statusItem} value={statusItem}>{t(`acte.status.${statusItem}`)}</option>
        )
        return (
            <div>
                <h1>{t('acte.list.title')}</h1>
                <Accordion style={this.styles} styled>
                    <Accordion.Title>Recherche avancée</Accordion.Title>
                    <Accordion.Content>
                        <Form onSubmit={this.submitForm}>
                            <FormFieldInline htmlFor='number' label={t('acte.fields.number')} >
                                <input id='number' value={this.state.search.number} onChange={e => this.handleFieldChange('number', e.target.value)} />
                            </FormFieldInline>
                            <FormFieldInline htmlFor='title' label={t('acte.fields.title')} >
                                <input id='title' value={this.state.search.title} onChange={e => this.handleFieldChange('title', e.target.value)} />
                            </FormFieldInline>
                            <FormFieldInline htmlFor='searchDecisionFrom' label={t('acte.fields.decision')}>
                                <Form.Group style={{ marginBottom: 0 }} widths='equal'>
                                    <FormField htmlFor='searchDecisionFrom' label='from'>
                                        <input type='date' id='searchDecisionFrom' value={this.state.searchDecisionFrom} onChange={this.handleChange} />
                                    </FormField>
                                    <FormField htmlFor='searchDecisionTo' label='to'>
                                        <input type='date' id='searchDecisionTo' value={this.state.searchDecisionTo} onChange={this.handleChange} />
                                    </FormField>
                                </Form.Group>
                            </FormFieldInline>
                            <FormFieldInline htmlFor='nature' label={t('acte.fields.nature')}>
                                <select id='nature' value={this.state.search.nature} onChange={e => this.handleFieldChange('nature', e.target.value)}>
                                    <option value=''>Toutes</option>
                                    {natureOptions}
                                </select>
                            </FormFieldInline>
                            <FormFieldInline htmlFor='status' label={t('acte.fields.status')}>
                                <select id='status' value={this.state.search.status} onChange={e => this.handleFieldChange('status', e.target.value)}>
                                    <option value=''>Tous</option>
                                    {statusOptions}
                                </select>
                            </FormFieldInline>
                            <Button type='submit'>Rechercher</Button>
                        </Form>
                    </Accordion.Content>
                </Accordion>
                <StelaTable
                    data={this.state.actes}
                    metaData={[
                        { property: 'uuid', displayed: false, searchable: false },
                        { property: 'number', displayed: true, displayName: t('acte.fields.number'), searchable: true },
                        { property: 'title', displayed: true, displayName: t('acte.fields.title'), searchable: true },
                        { property: 'decision', displayed: true, displayName: t('acte.fields.decision'), searchable: true, displayComponent: decisionDisplay },
                        { property: 'nature', displayed: true, displayName: t('acte.fields.nature'), searchable: true, displayComponent: natureDisplay },
                        { property: 'code', displayed: false, searchable: false },
                        { property: 'creation', displayed: false, searchable: false },
                        { property: 'status', displayed: true, displayName: t('acte.fields.status'), searchable: true, displayComponent: statusDisplay },
                        { property: 'lastUpdateTime', displayed: false, searchable: false },
                        { property: 'public', displayed: false, searchable: false },
                        { property: 'publicWebsite', displayed: false, searchable: false },
                    ]}
                    header={true}
                    link='/actes/'
                    linkProperty='uuid'
                    noDataMessage='Aucun acte'
                    keyProperty='uuid' />
            </div >
        )
    }
}

export default translate(['api-gateway'])(ActeList)