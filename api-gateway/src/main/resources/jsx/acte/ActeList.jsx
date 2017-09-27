import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'
import { translate } from 'react-i18next'
import { Accordion, Form, Button, Segment } from 'semantic-ui-react'

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
            objet: '',
            nature: '',
            status: '',
            decisionFrom: '',
            decisionTo: ''
        }
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
    submitForm = (event) => {
        event.preventDefault()
        const data = {}
        Object.keys(this.state.search)
            .filter(k => this.state.search[k] !== '')
            .map(k => data[k] = this.state.search[k])
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        fetchWithAuthzHandling({ url: '/api/acte/query', method: 'GET', query: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ actes: json }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const statusDisplay = (history) => t(`acte.status.${history[history.length - 1].status}`)
        const natureDisplay = (nature) => t(`acte.nature.${nature}`)
        const decisionDisplay = (decision) => moment(decision).format('DD/MM/YYYY')
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const statusOptions = status.map(statusItem =>
            <option key={statusItem} value={statusItem}>{t(`acte.status.${statusItem}`)}</option>
        )
        return (
            <Segment>
                <h1>{t('acte.list.title')}</h1>
                <Accordion style={this.styles} styled>
                    <Accordion.Title>{t('acte.list.advanced_search')}</Accordion.Title>
                    <Accordion.Content>
                        <Form onSubmit={this.submitForm}>
                            <FormFieldInline htmlFor='number' label={t('acte.fields.number')} >
                                <input id='number' value={this.state.search.number} onChange={e => this.handleFieldChange('number', e.target.value)} />
                            </FormFieldInline>
                            <FormFieldInline htmlFor='objet' label={t('acte.fields.objet')} >
                                <input id='objet' value={this.state.search.objet} onChange={e => this.handleFieldChange('objet', e.target.value)} />
                            </FormFieldInline>
                            <FormFieldInline htmlFor='decisionFrom' label={t('acte.fields.decision')}>
                                <Form.Group style={{ marginBottom: 0 }} widths='equal'>
                                    <FormField htmlFor='decisionFrom' label={t('form.from')}>
                                        <input type='date' id='decisionFrom' value={this.state.search.decisionFrom} onChange={e => this.handleFieldChange('decisionFrom', e.target.value)} />
                                    </FormField>
                                    <FormField htmlFor='decisionTo' label={t('form.to')}>
                                        <input type='date' id='decisionTo' value={this.state.search.decisionTo} onChange={e => this.handleFieldChange('decisionTo', e.target.value)} />
                                    </FormField>
                                </Form.Group>
                            </FormFieldInline>
                            <FormFieldInline htmlFor='nature' label={t('acte.fields.nature')}>
                                <select id='nature' value={this.state.search.nature} onChange={e => this.handleFieldChange('nature', e.target.value)}>
                                    <option value=''>{t('form.all_feminine')}</option>
                                    {natureOptions}
                                </select>
                            </FormFieldInline>
                            <FormFieldInline htmlFor='status' label={t('acte.fields.status')}>
                                <select id='status' value={this.state.search.status} onChange={e => this.handleFieldChange('status', e.target.value)}>
                                    <option value=''>{t('form.all')}</option>
                                    {statusOptions}
                                </select>
                            </FormFieldInline>
                            <Button type='submit'>{t('form.search')}</Button>
                        </Form>
                    </Accordion.Content>
                </Accordion>
                <StelaTable
                    data={this.state.actes}
                    metaData={[
                        { property: 'uuid', displayed: false, searchable: false },
                        { property: 'number', displayed: true, displayName: t('acte.fields.number'), searchable: true },
                        { property: 'objet', displayed: true, displayName: t('acte.fields.objet'), searchable: true },
                        { property: 'decision', displayed: true, displayName: t('acte.fields.decision'), searchable: true, displayComponent: decisionDisplay },
                        { property: 'nature', displayed: true, displayName: t('acte.fields.nature'), searchable: true, displayComponent: natureDisplay },
                        { property: 'code', displayed: false, searchable: false },
                        { property: 'creation', displayed: false, searchable: false },
                        { property: 'acteHistories', displayed: true, displayName: t('acte.fields.status'), searchable: true, displayComponent: statusDisplay },
                        { property: 'public', displayed: false, searchable: false },
                        { property: 'publicWebsite', displayed: false, searchable: false },
                    ]}
                    header={true}
                    link='/actes/'
                    linkProperty='uuid'
                    noDataMessage='Aucun acte'
                    keyProperty='uuid' />
            </Segment>
        )
    }
}

export default translate(['acte'])(ActeList)