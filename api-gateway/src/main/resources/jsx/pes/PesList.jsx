import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Accordion, Button } from 'semantic-ui-react'
import moment from 'moment'

import StelaTable from '../_components/StelaTable'
import { Page, Pagination, FormFieldInline, FormField } from '../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import { pesStatus } from '../_util/constants'

class PesList extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        pess: [],
        search: {
            objet: '',
            creationFrom: '',
            creationTo: '',
            status: ''
        },
        totalCount: 0,
        column: '',
        direction: '',
        limit: 25,
        offset: 0
    }
    componentDidMount() {
        this.submitForm()
    }
    getSearchData = () => {
        const { limit, offset, direction, column } = this.state
        const data = { limit, offset, direction, column }
        Object.keys(this.state.search)
            .filter(k => this.state.search[k] !== '')
            .map(k => data[k] = this.state.search[k])
        return data
    }
    handleFieldChange = (field, value) => {
        const search = this.state.search
        search[field] = value
        this.setState({ search: search })
    }
    submitForm = () => {
        const headers = { 'Accept': 'application/json' }
        const data = this.getSearchData()
        fetchWithAuthzHandling({ url: '/api/pes', method: 'GET', query: data, headers: headers })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ pess: json.results, totalCount: json.totalCount }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    handlePageClick = (data) => {
        const offset = Math.ceil(data.selected * this.state.limit)
        this.setState({ offset }, () => this.submitForm())
    }
    sort = (clickedColumn) => {
        const { column, direction } = this.state
        if (column !== clickedColumn) {
            this.setState({ column: clickedColumn, direction: 'ASC' }, () => this.submitForm())
            return
        }
        this.setState({ direction: direction === 'ASC' ? 'DESC' : 'ASC' }, () => this.submitForm())
    }
    render() {
        const { t, _addNotification } = this.context
        const statusDisplay = (histories) => {
            const lastHistory = histories[histories.length - 1]
            return <span>{moment(lastHistory.date).format('DD/MM/YYYY')} : {t(`pes.status.${lastHistory.status}`)}</span>
        }
        const creationDisplay = (creation) => moment(creation).format('DD/MM/YYYY')
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'creation', displayed: true, displayName: t('pes.fields.creation'), searchable: true, displayComponent: creationDisplay },
            { property: 'objet', displayed: true, displayName: t('pes.fields.objet'), searchable: true },
            { property: 'comment', displayed: true, displayName: t('pes.fields.comment'), searchable: true },
            { property: 'pesHistories', displayed: true, displayName: t('pes.fields.status'), searchable: false, displayComponent: statusDisplay }
        ]
        const displayedColumns = metaData.filter(metaData => metaData.displayed)
        const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
        const pagination =
            <Pagination
                columns={displayedColumns.length}
                pageCount={pageCount}
                handlePageClick={this.handlePageClick} />
        return (
            <Page title={t('pes.list.title')}>
                <Segment>
                    <PesListForm
                        search={this.state.search}
                        handleFieldChange={this.handleFieldChange}
                        submitForm={this.submitForm} />
                    <StelaTable
                        data={this.state.pess}
                        metaData={metaData}
                        header={true}
                        search={false}
                        link='/pes/'
                        linkProperty='uuid'
                        noDataMessage={t('pes.list.empty')}
                        keyProperty='uuid'
                        pagination={pagination}
                        sort={this.sort}
                        direction={this.state.direction}
                        column={this.state.column} />
                </Segment>
            </Page>
        )
    }
}

class PesListForm extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        isAccordionOpen: false
    }
    handleAccordion = () => {
        const isAccordionOpen = this.state.isAccordionOpen
        this.setState({ isAccordionOpen: !isAccordionOpen })
    }
    submitForm = (event) => {
        if (event) event.preventDefault()
        this.props.submitForm()
    }
    render() {
        const { t } = this.context
        const { search, handleFieldChange } = this.props
        const statusOptions = pesStatus.map(statusItem =>
            <option key={statusItem} value={statusItem}>{t(`pes.status.${statusItem}`)}</option>
        )
        return (
            <Accordion style={{ marginBottom: '1em' }} styled>
                <Accordion.Title active={this.state.isAccordionOpen} onClick={this.handleAccordion}>{t('api-gateway:form.advanced_search')}</Accordion.Title>
                <Accordion.Content active={this.state.isAccordionOpen}>
                    <Form onSubmit={this.submitForm}>
                        <FormFieldInline htmlFor='objet' label={t('pes.fields.objet')} >
                            <input id='objet' value={search.objet} onChange={e => handleFieldChange('objet', e.target.value)} />
                        </FormFieldInline>
                        <FormFieldInline htmlFor='creationFrom' label={t('pes.fields.creation')}>
                            <Form.Group style={{ marginBottom: 0 }} widths='equal'>
                                <FormField htmlFor='creationFrom' label={t('api-gateway:form.from')}>
                                    <input type='date' id='creationFrom' value={search.decisionFrom} onChange={e => handleFieldChange('creationFrom', e.target.value)} />
                                </FormField>
                                <FormField htmlFor='creationTo' label={t('api-gateway:form.to')}>
                                    <input type='date' id='creationTo' value={search.decisionTo} onChange={e => handleFieldChange('creationTo', e.target.value)} />
                                </FormField>
                            </Form.Group>
                        </FormFieldInline>
                        <FormFieldInline htmlFor='status' label={t('pes.fields.status')}>
                            <select id='status' value={search.status} onChange={e => handleFieldChange('status', e.target.value)}>
                                <option value=''>{t('api-gateway:form.all')}</option>
                                {statusOptions}
                            </select>
                        </FormFieldInline>
                        <Button type='submit' basic primary>{t('api-gateway:form.search')}</Button>
                    </Form>
                </Accordion.Content>
            </Accordion>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesList)