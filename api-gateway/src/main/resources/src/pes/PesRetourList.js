import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button } from 'semantic-ui-react'
import moment from 'moment'

import StelaTable from '../_components/StelaTable'
import AdvancedSearch from '../_components/AdvancedSearch'
import InputDatetime from '../_components/InputDatetime'
import Pagination from '../_components/Pagination'
import { Page, FormFieldInline, FormField } from '../_components/UI'
import {
    checkStatus,
    handleSearchChange,
    handlePageClick,
    updateItemPerPage,
    onSearch
} from '../_util/utils'
import { notifications } from '../_util/Notifications'

class PesRetourList extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        pesRetours: [],
        search: {
            multifield: '',
            filename: '',
            creationFrom: '',
            creationTo: ''
        },
        totalCount: 0,
        limit: 25,
        currentPage: 0,
        offset: 0
    }
    componentDidMount() {
        this.submitForm()
    }
    getSearchData = () => {
        const { limit, offset } = this.state
        const data = { limit, offset }
        Object.keys(this.state.search)
            .filter(k => this.state.search[k] !== '')
            .forEach(k => (data[k] = this.state.search[k]))
        if (data.creationFrom) data.creationFrom = moment(data.creationFrom).format('YYYY-MM-DD')
        if (data.creationTo) data.creationTo = moment(data.creationTo).format('YYYY-MM-DD')
        return data
    }
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const headers = { Accept: 'application/json' }
        const data = this.getSearchData()
        _fetchWithAuthzHandling({ url: '/api/pes/pes-retour', method: 'GET', query: data, headers: headers })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ pesRetours: json.results, totalCount: json.totalCount }))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    render() {
        const { t } = this.context
        const { search } = this.state
        const creationDisplay = creation => moment(creation).format('DD/MM/YYYY')
        const attachmentLink = pesRetour => (
            <a id="attachment" target="_blank" href={`/api/pes/pes-retour/${pesRetour.uuid}/file`}>{pesRetour.attachment.filename}</a>
        )
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'creation', displayed: true, displayName: t('pes.fields.creation'), searchable: true, displayComponent: creationDisplay },
            { property: '_self', displayed: true, displayName: t('pes.fields.attachment'), searchable: false, displayComponent: attachmentLink }
        ]
        const displayedColumns = metaData.filter(metaData => metaData.displayed)
        const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
        const pagination = (
            <Pagination
                columns={displayedColumns.length}
                pageCount={pageCount}
                handlePageClick={(data) => handlePageClick(this, data, this.submitForm)}
                itemPerPage={this.state.limit}
                updateItemPerPage={(itemPerPage) => updateItemPerPage(this, itemPerPage, this.submitForm)}
                currentPage={this.state.currentPage} />
        )
        return (
            <Page title={t('pes.retour.list.title')}>
                <Segment>
                    <AdvancedSearch isDefaultOpen={false} fieldId="multifield" fieldValue={search.multifield} fieldOnChange={(id, value) => handleSearchChange(this, id, value)}
                        onSubmit={() => onSearch(this, this.submitForm)}>
                        <Form onSubmit={() => onSearch(this, this.submitForm)}>
                            <FormFieldInline htmlFor="filename" label={t('pes.fields.attachment')}>
                                <input id="filename" value={search.objet} onChange={e => handleSearchChange(this, 'filename', e.target.value)} />
                            </FormFieldInline>
                            <FormFieldInline htmlFor="creationFrom" label={t('pes.fields.creation')}>
                                <Form.Group style={{ marginBottom: 0 }} widths="equal">
                                    <FormField htmlFor="creationFrom" label={t('api-gateway:form.from')}>
                                        <InputDatetime id="creationFrom" timeFormat={false} value={search.decisionFrom}
                                            onChange={date => handleSearchChange(this, 'creationFrom', date)} />
                                    </FormField>
                                    <FormField htmlFor="creationTo" label={t('api-gateway:form.to')}>
                                        <InputDatetime id="creationTo" timeFormat={false} value={search.creationTo}
                                            onChange={date => handleSearchChange(this, 'creationTo', date)} />
                                    </FormField>
                                </Form.Group>
                            </FormFieldInline>
                            <div style={{ textAlign: 'right' }}>
                                <Button type="submit" basic primary>
                                    {t('api-gateway:form.search')}
                                </Button>
                            </div>
                        </Form>
                    </AdvancedSearch>

                    <StelaTable
                        data={this.state.pesRetours}
                        metaData={metaData}
                        header={true}
                        search={false}
                        noDataMessage={t('pes.retour.list.empty')}
                        keyProperty="uuid"
                        pagination={pagination}
                    />
                </Segment>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesRetourList)
