import React, { Component, Fragment } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Grid  } from 'semantic-ui-react'

import { FormField } from '../../../_components/UI'
import InputValidation from '../../../_components/InputValidation'

import { withAuthContext } from '../../../Auth'


class CreateEditTagsFragment extends Component {
	static contextTypes = {
	    csrfToken: PropTypes.string,
	    csrfTokenHeaderName: PropTypes.string,
	    t: PropTypes.func,
	    _addNotification: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func
	}
	static propTypes = {
	    validationRules: PropTypes.object,
	    handleFieldChange: PropTypes.func,
	    fields: PropTypes.object,
	    tags: PropTypes.object,
	    fieldsObject: PropTypes.string
	}
	render() {
	    const { t } = this.context
	    const { validationRules, handleFieldChange, fields, tags, fieldsObject } = this.props
	    return (
	        <Fragment>
	            <Grid.Column mobile="16" computer='8'>
	                <FormField htmlFor={`${fields.uuid}_name`}
	                    label={t('convocation.admin.modules.convocation.local_authority_settings.tags.name')} required={true}>
	                    <InputValidation
	                        errorTypePointing={false}
	                        validationRule={validationRules.tags.name}
	                        id={`${fields.uuid}_name`}
	                        fieldName={t('convocation.admin.modules.convocation.local_authority_settings.tags.name')}
	                        onChange={(id, value) => handleFieldChange(fieldsObject, id, value)}
	                        value={tags.name}/>
	                </FormField>
	            </Grid.Column>
	            <Grid.Column mobile="16" computer='8'>
	                <FormField htmlFor={`${fields.uuid}_color`}
	                    label={t('convocation.admin.modules.convocation.local_authority_settings.tags.color')} required={true}>
	                    <InputValidation
	                        errorTypePointing={false}
	                        type='colorPicker'
	                        validationRule={validationRules.tags.color}
	                        id={`${fields.uuid}_color`}
	                        fieldName={t('convocation.admin.modules.convocation.local_authority_settings.tags.color')}
	                        onChange={(id, value) => handleFieldChange(fieldsObject, id, value)}
	                        value={tags.color}/>
	                </FormField>
	            </Grid.Column>
	        </Fragment>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(CreateEditTagsFragment))