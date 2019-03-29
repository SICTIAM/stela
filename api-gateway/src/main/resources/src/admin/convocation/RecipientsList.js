import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import debounce from 'debounce'
import accepts from 'attr-accept'
import Validator from 'validatorjs'
import { Segment, Icon, Button, Form, Modal, Grid, Message } from 'semantic-ui-react'

import {
    getLocalAuthoritySlug,
    handleSearchChange,
    handlePageClick,
    updateItemPerPage,
    sortTable
} from '../../_util/utils'
import { notifications } from '../../_util/Notifications'
import ConvocationService from '../../_util/convocation-service'

import { withAuthContext } from '../../Auth'

import StelaTable from '../../_components/StelaTable'
import AdvancedSearch from '../../_components/AdvancedSearch'
import { Page, FormFieldInline, FormField, InputFile, File, ValidationPopup } from '../../_components/UI'
import Pagination from '../../_components/Pagination'
import Breadcrumb from '../../_components/Breadcrumb'

class RecipientsList extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    csrfToken: PropTypes.string,
	    csrfTokenHeaderName: PropTypes.string,
	    _addNotification: PropTypes.func,
	}
	state = {
	    recipients:[],
	    search: {
	        multifield: '',
	        fistname: '',
	        lastname: '',
	        email: '',
	        active: ''
	    },
	    column: '',
	    direction: '',
	    limit: 10,
	    offset: 0,
	    currentPage: 0,
	    totalCount: 0,
	    localAuthority: {
	        epci: false
	    },
	    importRecipientsModal: false,
	    isFormValid: false,
	    allFormErrors: [],
	    fields: {
	        recipients: null
	    },
	    importError: []
	}
	validationRules = {
	    recipients: ['required']
	}
	acceptFile = '.csv'
	componentDidMount = async () => {
	    this._convocationService = new ConvocationService()
	    const itemPerPage = localStorage.getItem('itemPerPage')
	    const localAuthorityResponse = await this._convocationService.getConfForLocalAuthority(this.props.authContext)
	    if (!itemPerPage) localStorage.setItem('itemPerPage', 10)
	    else this.setState({ limit: 10, localAuthority: localAuthorityResponse }, this.loadData)
	}

	loadData = () => {
	    const { _fetchWithAuthzHandling } = this.context
	    const data = this.getSearchData()
	    _fetchWithAuthzHandling({ url: '/api/convocation/recipient', query: data, method: 'GET' })
	        .then(response => response.json())
	        .then((response) => this.setState({recipients: response.results, totalCount: response.totalCount}))
	}

	getSearchData = () => {
	    const { limit, offset, direction, column } = this.state
	    const data = { limit, offset, direction, column }
	    Object.keys(this.state.search)
	        .filter(k => this.state.search[k] !== '')
	        .map(k => data[k] = this.state.search[k])
	    return data
	}

	lineThroughResolver = (recipient) => {
	    return !recipient.active
	}

	openImportRecipientModal = () => this.setState({ importRecipientsModal: true })

	closeImportRecipientModal = () => {
	    const { fields } = this.state
	    fields.recipients = null
	    this.setState({ importRecipientsModal: false, fields})
	}

	handleFieldCheckboxChange = (row) => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const url = !row.active ? `/api/convocation/recipient/${row.uuid}` : `/api/convocation/recipient/${row.uuid}`
	    const body = {
	        active: !row.active ? true : false
	    }
	    const headers = { 'Content-Type': 'application/json' }

	    _fetchWithAuthzHandling({url: url, method: 'PUT', headers: headers, body: JSON.stringify(body), context: this.props.authContext})
	        .then(response => {
	            _addNotification(notifications.admin.statusUpdated)
	            this.loadData()
	            row.active = !row.active
	        })
	}

	validateForm = debounce(() => {
	    const { t } = this.context
	    const data = {
	        recipients: this.state.fields.recipients
	    }

	    const attributeNames = {
	        recipients: t('convocation.admin.modules.convocation.recipient_list.recipients_file')
	    }
	    const validationRules = this.validationRules
	    //add validation format file

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const allFormErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, allFormErrors })
	})

	handleFileChange = (field, file, acceptType) => {
	    if(this.acceptsFile(file, acceptType)) {
	        const fields = this.state.fields

	        if (file) {
	            fields[field] = file
	            this.setState({ fields }, this.validateForm)
	        }
	    }
	}

	acceptsFile = (file, acceptType) => {
	    const { _addNotification, t } = this.context
	    if(accepts(file, acceptType)) {
	        return true
	    } else {
	        _addNotification(notifications.defaultError, 'notifications.convocation.title', t('api-gateway:form.validation.bad_extension_file', {name: file.name}))
	        return false
	    }
	}

	submit = async () => {
	    const { _addNotification } = this.context
	    const data = new FormData()
	    data.append('recipients', this.state.fields.recipients)
	    try {
	        await this._convocationService.importRecipients(this.props.authContext, data)
	        _addNotification(notifications.admin.importRecipientsSuccess)
	    } catch (error){
	        if(error.status === 400) {
	            error.json().then(json => {
	                this.setState({importError: json.errors})
	            })
	        }
	    }
	    this.closeImportRecipientModal()

	}

	deactivateAll = async () => {
	    const { _addNotification } = this.context
	    await this._convocationService.desactivateAllRecipients(this.props.authContext)
	    _addNotification(notifications.admin.all_recipients_deactivated_success)
	    this.loadData()
	}
	render() {
	    const { t } = this.context
	    const { search } = this.state
	    const deactivateAll = {
	        title: t('convocation.admin.modules.convocation.recipient_list.deactivate_all'),
	        action: this.deactivateAll
	    }
	    const importRecipient = {
	        title: t('convocation.admin.modules.convocation.recipient_list.importRecipient'),
	        action: this.openImportRecipientModal
	    }
	    const assemblyTypes = (assemblyTypes) => {
	        if(assemblyTypes && assemblyTypes.length > 0) {
	            let temp = assemblyTypes
	            if (assemblyTypes.length === 1){
	                return <span>{assemblyTypes[0].name}</span>
	            }
	            if(assemblyTypes.length > 2) {
	                temp = assemblyTypes.slice(0, 2)
	            }
	            temp = temp.reduce((acc, curr, index) => {
	                if(index > 0) {
	                    acc = acc.name + ', '
	                }
	                return acc + curr.name
	            })
	            if(assemblyTypes.length > 2) {
	                return <span>{temp},... <span style={{fontStyle: 'italic', marginLeft: '5px'}}>Voir Tout <Icon name='arrow right'/></span></span>
	            }
	            return <span>{temp}</span>
	        }
	        return ''
	    }
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'lastname', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.lastname') },
	        { property: 'firstname', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.firstname') },
	        { property: 'email', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.email') },
	        { property: 'phoneNumber', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.phonenumber') },
	        { property: 'assemblyTypes', displayed: true, searchable: false, sortable: false, displayName: t('convocation.admin.modules.convocation.assembly_types'), displayComponent: assemblyTypes }	    ]

	    if(this.state.localAuthority.epci) metaData.splice(6,0,  {property: 'epciName', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.epci')})
	    const options = [
	        { key: 10, text: 10, value: 10 },
	        { key: 25, text: 25, value: 25 },
	        { key: 50, text: 50, value: 50 },
	        { key: 100, text: 100, value: 100 }]
	    const displayedColumns = metaData.filter(metaData => metaData.displayed)
	    const pageCount = Math.ceil(this.state.totalCount / this.state.limit)
	    const pagination =
            <Pagination
                columns={displayedColumns.length +1}
                pageCount={pageCount}
                handlePageClick={(data) => handlePageClick(this, data, this.loadData)}
                itemPerPage={this.state.limit}
                updateItemPerPage={(itemPerPage) => updateItemPerPage(this, itemPerPage, this.loadData)}
                currentPage={this.state.currentPage}
                options={options} />
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const submissionButton = <Button type='submit' primary basic disabled={!this.state.isFormValid}>
	        {t('api-gateway:form.send')}
	    </Button>

	    let notificationErrorImport = null
	    if(this.state.importError.length > 0) {
	        notificationErrorImport = this.state.importError.map((error, index) => {
	            return (
	                <p key={`error_${index}`}>
	                    {`${t('convocation.errors.csv.line')} ${error.line} - ${t('convocation.errors.csv.error_on')}: ${error.field}`}
	                    <br/>
	                    {`${t(`${error.message}`)}`}
	                </p>
	            )
	        })
	    }
	    return (
	        <Page>
	            <Breadcrumb
	                data={[
	                    {title: t('api-gateway:breadcrumb.admin_home'), url: `/${localAuthoritySlug}/admin/ma-collectivite`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation'), url: `/${localAuthoritySlug}/admin/ma-collectivite/convocation`},
	                    {title: t('api-gateway:breadcrumb.convocation.recipients_list')}
	                ]}
	            />
	            {notificationErrorImport && (
	                <Message warning>
	                    <Message.Header style={{ marginBottom: '0.5em'}}>{t('convocation.errors.csv.title')}</Message.Header>
	                    {notificationErrorImport}
	                </Message>
	            )}
	            <Modal open={this.state.importRecipientsModal}>
	                <Modal.Header>{t('convocation.admin.modules.convocation.recipient_list.importRecipient')}</Modal.Header>
	                <Modal.Content>
	                    <Form onSubmit={this.submit}>
	                        <Grid>
	                            <Grid.Column mobile='16' computer='8'>
	                                <FormField htmlFor={`${this.props.uuid}_recipients`}
	                                    label={t('convocation.admin.modules.convocation.recipient_list.recipients_file')}>
	                                    <InputFile labelClassName="primary" htmlFor={`${this.props.uuid}_recipients`}
	                                        label={`${t('api-gateway:form.add_a_file')}`}>
	                                        <input type="file"
	                                            id={`${this.props.uuid}_recipients`}
	                                            accept={this.acceptFile}
	                                            onChange={(e) => this.handleFileChange('recipients', e.target.files[0], this.acceptFile)}
	                                            style={{ display: 'none' }}/>
	                                    </InputFile>
	                                </FormField>
	                                {this.state.fields.recipients && (
	                                    <File attachment={{ filename: this.state.fields.recipients.name }} onDelete={() => this.deleteFile('recipients')} />
	                                )}
	                            </Grid.Column>
	                        </Grid>
	                        <div className='footerForm'>
	                            <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.closeImportRecipientModal()} basic color='red'>
	                                {t('api-gateway:form.cancel')}
	                            </Button>

	                            {this.state.allFormErrors.length > 0 &&
									<ValidationPopup errorList={this.state.allFormErrors}>
									    {submissionButton}
									</ValidationPopup>
	                            }
	                            {this.state.allFormErrors.length === 0 && submissionButton}
	                        </div>
	                    </Form>
	                </Modal.Content>
	            </Modal>
	            <Segment>
	                <AdvancedSearch
	                    isDefaultOpen={false}
	                    fieldId='multifield'
	                    fieldValue={search.multifield}
	                    fieldOnChange={(id, value) => handleSearchChange(this, id, value)}
	                    onSubmit={this.loadData}>
	                    <Form onSubmit={this.loadData}>
	                        <FormFieldInline htmlFor='firstname' label={t('convocation.admin.modules.convocation.recipient_config.firstname')} >
	                            <input id='firstname' aria-label={t('convocation.admin.modules.convocation.recipient_config.firstname')} value={search.firstname} onChange={e => handleSearchChange(this, 'firstname', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='name' label={t('convocation.admin.modules.convocation.recipient_config.lastname')} >
	                            <input id='lastname' aria-label={t('convocation.admin.modules.convocation.recipient_config.lastname')} value={search.lastname} onChange={e => handleSearchChange(this, 'lastname', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='email' label={t('convocation.admin.modules.convocation.recipient_config.email')} >
	                            <input id='email' aria-label={t('convocation.admin.modules.convocation.recipient_config.email')} value={search.email} onChange={e => handleSearchChange(this, 'email', e.target.value)} />
	                        </FormFieldInline>
	                        <FormFieldInline htmlFor='active' label={t('convocation.admin.modules.convocation.recipient_config.status')}>
	                            <select id='active' aria-label={t('convocation.admin.modules.convocation.recipient_config.status')} onBlur={e => handleSearchChange(this, 'active', e.target.value)}>
	                                <option value=''>{t('api-gateway:form.all')}</option>
	                                <option value={true}>{t('convocation.admin.modules.convocation.recipient_list.active')}</option>
	                                <option value={false}>{t('convocation.admin.modules.convocation.recipient_list.inactive')}</option>
	                            </select>
	                        </FormFieldInline>
	                        <div style={{ textAlign: 'right' }}>
	                            <Button type='submit' basic primary>{t('api-gateway:form.search')}</Button>
	                        </div>
	                    </Form>
	                </AdvancedSearch>
	                <StelaTable
	                    header={true}
	                    search={false}
	                    click={true}
	                    sortable={true}
	                    metaData={metaData}
	                    data={this.state.recipients}
	                    keyProperty="uuid"
	                    link={`/${localAuthoritySlug}/admin/convocation/destinataire/liste-destinataires/`}
	                    linkProperty='uuid'
	                    onHandleToggle={this.handleFieldCheckboxChange}
	                    lineThroughResolver={this.lineThroughResolver}
	                    pagination={pagination}
	                    sort={(clickedColumn) => sortTable(this, clickedColumn, this.loadData)}
	                    direction={this.state.direction}
	                    column={this.state.column}
	                    toogleHeader={t('convocation.admin.modules.convocation.assembly_type_config.status')}
	                    toggleButton={true}
	                    toogleProperty='active'
	                    noDataMessage={t('convocation.new.no_recipient')}
	                    selectOptions={[
	                        importRecipient,
 							deactivateAll
	                    ]}
	                />
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(RecipientsList))