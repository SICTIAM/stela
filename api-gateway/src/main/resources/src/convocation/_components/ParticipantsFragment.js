import React, { Component, Fragment } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Grid, Button, Icon } from 'semantic-ui-react'

import { convertDateBackFormatToUIFormat } from '../../_util/utils'

import StelaTable from '../../_components/StelaTable'

import ParticipantsResponsesFragment from './ParticipantsResponsesFragment'

class ParticipantsFragment extends Component {
	static contextTypes = {
	    t: PropTypes.func
	}
	static propTypes = {
	    title: PropTypes.string.isRequired,
	    participantResponses: PropTypes.array,
	    questions: PropTypes.array,
	    epci: PropTypes.bool
	}
    static defaultProps = {
        title: '',
        participants: [],
        questions: [],
        epci: false
    }
    state = {
        displayListParticipants: false
    }
    sumRecipientsByStatus = (answer) => {
        const { participantResponses } = this.props
	    return participantResponses.reduce( (acc, curr) => {
	        return curr['responseType'] === answer ? acc + 1: acc
	    }, 0)
    }

    greyResolver = (participant) => {
	    return !participant.opened
    }

    render() {
        const { t } = this.context
        const { title, participantResponses, questions, epci } = this.props

        const presents = this.sumRecipientsByStatus('PRESENT')
	    const absents = this.sumRecipientsByStatus('NOT_PRESENT')
	    const procurations = this.sumRecipientsByStatus('SUBSTITUTED')
        const noAnswer = this.sumRecipientsByStatus('DO_NOT_KNOW')

        const answerDisplay = (answer) => {
	        switch(answer) {
	        case 'PRESENT': return <p className='green text-bold'>{t('convocation.page.present')}</p>
	        case 'NOT_PRESENT': return <p className='red'>{t('convocation.page.absent')}</p>
	        case 'SUBSTITUTED': return <p className='red'>{t('convocation.page.substituted')}</p>
	        default: return ''
	        }
	    }
	    const statutDisplay = (opened) => opened ? <p><Icon name='envelope open'/> {convertDateBackFormatToUIFormat(opened, 'DD/MM/YYYY à HH:mm')}</p>: <Icon name='envelope'/>
        const recipientDisplay = (recipient) => `${recipient.firstname} ${recipient.lastname}`
        const recipientEpciName = (recipient) => recipient.epciName
	    const metaData = [
	        { property: 'uuid', displayed: false },
	        { property: 'recipient', displayed: true, searchable: false, displayName: 'Destinataires', displayComponent: recipientDisplay },
	        { property: 'openDate', displayed: true, searchable: false, displayName: 'Statut', displayComponent: statutDisplay },
	        { property: 'responseType', displayed: true, searchable: false, displayName: 'Réponses', displayComponent: answerDisplay },
	    ]
        if(epci) metaData.push({property: 'recipient', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.recipient_config.epci'), displayComponent: recipientEpciName})

        return (
            <Fragment>
                <h2>{title}</h2>
                <Grid columns='1'>
                    <Grid.Column>
                        <p className='mb-0'>
                            {presents > 0 && (
 	                            <Fragment>
 	                                {t('convocation.page.numberPresents',{'number': presents})}
 	                            </Fragment>
                            )}
                            {procurations > 0 && (
 	                            <Fragment>
                                    {(presents > 0) && (
                                        ', '
                                    )}
                                    {t('convocation.page.numberSubstituted',{'number': procurations})}
 	                            </Fragment>
 	                        )}
 	                        {absents > 0 && (
 	                            <Fragment>
 	                                {(presents > 0 || procurations > 0) && (
 	                                    ', '
 	                                )}
 	                                {t('convocation.page.numberAbsents',{'number': absents})}
 	                            </Fragment>
 	                        )}
                        </p>
                        <p className='red'>
 	                        {noAnswer > 0 && (
 	                            <Fragment>
 	                                {t('convocation.page.numberNoAnswer',{'number': noAnswer})}
 	                            </Fragment>
 	                        )}
 	                    </p>
                        <p>
 	                        <Button onClick={() => {
 	                            this.setState({displayListParticipants: !this.state.displayListParticipants})}}
 	                        	className="link" primary compact basic>
 	                            { this.state.displayListParticipants ? t('convocation.page.hide_list') : t('convocation.page.see_list')}
 	                        </Button>
 	                    </p>
                    </Grid.Column>
                    {this.state.displayListParticipants && (
 	                    <Grid.Column className='pt-0'>
 	                        <ParticipantsResponsesFragment
 	                            participants={participantResponses}
 	                            questions={questions}/>
 	                    </Grid.Column>
 	                )}
                </Grid>
                <h3>{t('convocation.page.history')}</h3>
                <Grid columns='1'>
                    <Grid.Column>
                        <StelaTable
 	                            metaData={metaData}
 	                            data={participantResponses}
 	                            header={true}
 	                            sortable={false}
 	                            greyResolver={this.greyResolver}
 	                            striped={false}
 	                            selectable= {false}
 	                            search={false}
 	                            keyProperty="uuid"
 	                            noDataMessage={t('convocation.new.no_recipient')}
 	                        />
                    </Grid.Column>
                </Grid>
            </Fragment>
        )
    }
}

export default translate(['convocation', 'api-gateway'])(ParticipantsFragment)
