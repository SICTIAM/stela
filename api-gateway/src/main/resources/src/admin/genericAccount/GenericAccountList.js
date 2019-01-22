import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { translate } from 'react-i18next'
import { Segment, Form, Button } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import Pagination from '../../_components/Pagination'
import AdvancedSearch from '../../_components/AdvancedSearch'
import { Page, FormFieldInline, LoadingContent } from '../../_components/UI'
import {
    checkStatus,
    getLocalAuthoritySlug,
    handleSearchChange,
    handlePageClick,
    updateItemPerPage,
    sortTable,
    onSearch
} from '../../_util/utils'
import { notifications } from '../../_util/Notifications'

class GenericAccountList extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        genericAccounts: [],
        search: {
            multifield: '',
            software: '',
            email: ''
        },
        totalCount: 0,
        column: '',
        direction: '',
        limit: 25,
        currentPage: 0,
        offset: 0,
        fetchStatus: ''
    }
    componentDidMount() {
        const itemPerPage = localStorage.getItem('itemPerPage')
        if (!itemPerPage) localStorage.setItem('itemPerPage', 25)
        else this.setState({ limit: 25 }, this.submitForm)
    }
    getSearchData = () => {
        const { limit, offset, direction, column } = this.state
        const data = { limit, offset, direction, column }
        Object.keys(this.state.search)
            .filter(k => this.state.search[k] !== '')
            .forEach(k => (data[k] = this.state.search[k]))
        return data
    }
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        this.setState({ fetchStatus: 'loading' })
        const headers = { Accept: 'application/json' }
        const data = this.getSearchData()
        _fetchWithAuthzHandling({ url: '/api/admin/generic_account', method: 'GET', query: data, headers: headers })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ genericAccounts: json.results, totalCount: json.totalCount, fetchStatus: 'fetched' }))
            .catch(response => {
                this.setState({ fetchStatus: 'api-gateway:error.default' })
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.title', text))
            })
    }

    render() {
        const { t } = this.context
        const { search } = this.state
        const localAuthoritySlug = getLocalAuthoritySlug()
        const metaData = [
            { property: 'uuid', displayed: false, searchable: false },
            { property: 'software', displayed: true, displayName: t('admin.generic_account.fields.software'), searchable: true },
            { property: 'email', displayed: true, displayName: t('admin.generic_account.fields.email'), searchable: true }
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
                currentPage={this.state.currentPage}
            />
        )
        return (
            <Page title={t('admin.generic_account.title')}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Segment>
                        <div style={{ textAlign: 'right', marginBottom: '1em' }}>
                            <Link className='ui button basic primary' to={`/${localAuthoritySlug}/admin/compte-generique/nouveau`}>
                                {t('admin.generic_account.add_new')}
                            </Link>
                        </div>
                        <AdvancedSearch isDefaultOpen={false} fieldId="multifield" fieldValue={search.multifield}
                            fieldOnChange={(id, value) => handleSearchChange(this, id, value)} onSubmit={() => onSearch(this, this.submitForm)}>
                            <Form onSubmit={() => onSearch(this, this.submitForm)}>
                                <FormFieldInline htmlFor="software" label={t('admin.generic_account.fields.software')}>
                                    <input id="software" value={search.software} onChange={e => handleSearchChange(this, 'software', e.target.value)} />
                                </FormFieldInline>
                                <FormFieldInline htmlFor="email" label={t('admin.generic_account.fields.email')}>
                                    <input id="email" value={search.email} onChange={e => handleSearchChange(this, 'email', e.target.value)} />
                                </FormFieldInline>
                                <div style={{ textAlign: 'right' }}>
                                    <Button type="submit" basic primary>
                                        {t('api-gateway:form.search')}
                                    </Button>
                                </div>
                            </Form>
                        </AdvancedSearch>

                        <StelaTable
                            data={this.state.genericAccounts}
                            metaData={metaData}
                            header={true}
                            search={false}
                            link={`/${localAuthoritySlug}/admin/compte-generique/`}
                            linkProperty="uuid"
                            noDataMessage={t('admin.generic_account.empty')}
                            keyProperty="uuid"
                            pagination={pagination}
                            sort={(clickedColumn) => sortTable(this, clickedColumn, this.submitForm)}
                            direction={this.state.direction}
                            column={this.state.column}
                            negativeResolver={this.negativeResolver} />
                    </Segment>
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(GenericAccountList)
