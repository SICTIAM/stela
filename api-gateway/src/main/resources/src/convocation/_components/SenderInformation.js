import React, { Component } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Grid, Segment } from 'semantic-ui-react'

import { Field, FieldValue } from '../../_components/UI'
import { convertDateBackFormatToUIFormat } from '../../_util/utils'

class SenderInformation extends Component {
	static contextTypes = {
	    t: PropTypes.func
	}

	render() {
	    const { t } = this.context
	    const { convocation } = this.props
	    return (
	        <Segment>
	            <h2>{t('convocation.page.sent')}</h2>
	                <Grid columns='2'>
	                    <Grid.Column mobile='16' tablet='8' computer='6'>
	                        <Field htmlFor="sendBy" label={t('convocation.page.send_by')}>
	                            <FieldValue id="sendBy">{convocation.profile && `${convocation.profile.firstname} ${convocation.profile.lastname}`}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                    <Grid.Column mobile='16' computer='4'>
	                        <Field htmlFor="sendingDate" label={t('convocation.list.sent_date')}>
	                            <FieldValue id="sendingDate">{convertDateBackFormatToUIFormat(convocation.sentDate, 'DD/MM/YYYY à HH:mm')}</FieldValue>
	                        </Field>
	                    </Grid.Column>
	                </Grid>
	        </Segment>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(SenderInformation)