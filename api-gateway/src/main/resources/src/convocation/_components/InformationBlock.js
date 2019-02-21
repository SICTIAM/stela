import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Grid } from 'semantic-ui-react'

import { Field, FieldValue } from '../../_components/UI'
import { convertDateBackFormatToUIFormat } from '../../_util/utils'

class InformationBlockConvocation extends Component {
	static contextTypes = {
	    t: PropTypes.func
	}
	render() {
	    const { t } = this.context
	    const { convocation } = this.props
	    return (
	        <Grid.Column mobile='16' computer='4'>
	            <div className='block-information'>
	                <Grid columns='1'>
	                    <Grid.Column>
	                        <Field htmlFor="meetingDate" label={t('convocation.fields.date')}>
	                            <FieldValue id="meetingDate">{convertDateBackFormatToUIFormat(convocation.meetingDate, 'DD/MM/YYYY à HH:mm')}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                    <Grid.Column>
	                        <Field htmlFor="assemblyType" label={t('convocation.fields.assembly_type')}>
	                            <FieldValue id="assemblyType">{convocation.assemblyType && convocation.assemblyType.name}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                	<Grid.Column>
	                        <Field htmlFor="location" label={t('convocation.fields.assembly_place')}>
	                            <FieldValue id="location">{convocation.location}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                </Grid>
	            </div>
	        </Grid.Column>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(InformationBlockConvocation)