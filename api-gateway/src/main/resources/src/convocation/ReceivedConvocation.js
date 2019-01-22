import React, { Component } from 'react'
import { Segment, Grid, Button, Icon, Radio, Form } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'

import { Page, Field, FieldValue, FormFieldInline } from '../_components/UI'

import Pagination from '../_components/Pagination'


class ReceivedConvocation extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	state = {
	    displayListParticipants: false,
	    totalCount: 0,
	    limit: 25,
	    convocation: {
	        uuid: 'test',
	        date: '19/12/2016 à 10h00',
	        assemblyType: 'Bureau syndical test',
	        assemblyPlace: 'Le lieu habituel',
	        comments: 'Je suis un commentaire',
	        document: '',
	        procuration: 'Visualiser',
	        questions: ['Je suis la question 1 ?', 'Ma question 2 ?'],
	        transmitterGroup: 'Sictiam Test',
	        sendBy: 'Anne-Sophie LEVEQUE',
	        sendingDate: '20/11/2018 à 09h07',
	        participants: [{
	            name: 'Julie ALBALADEJO',
	            answer: 'OK',
	            opened: '21/10/2018 10:30',
	            questions:['oui', 'oui']
	        },{
	            name: 'Anne-Sophie LEVEQUE',
	            answer: 'KO',
	            opened: '23/10/2018 14:24'
	        }, {
	            name: 'Anne-Sophie LEVEQUES',
	            answer: 'OK',
	            opened: '23/10/2018 14:24',
	            questions:['non', 'oui']
	        },
	        {
	            name: 'Anne-Sophie LEVEQUES',
	            answer: 'KO',
	            opened: '23/10/2018 14:24'
	        },
	        {
	            name: 'Anne-Sophie LEVEQUES',
	            answer: null,
	            opened: null
	        },{
	            name: 'Anne-Sophie LEVEQUES',
	            answer: null,
	            opened: null
	        }]
	    }
	}

	sumParticipantsByStatus = (answer) => {
	    return this.state.convocation.participants.reduce( (acc, curr) => {
	        return curr['answer'] === answer ? acc + 1: acc
	    }, 0)
	}

	greyResolver = (participant) => {
	    return !participant.opened
	}

	render() {
	    const { t } = this.context

	    return (
	        <Page>
	            <Segment>
	                <Form>
	                    <h2>Titre de la convocation</h2>
	                    <Grid reversed='mobile tablet vertically'>
	                        <Grid.Column mobile='16' tablet='16' computer='12'>
	                            <Grid>
	                                <Grid.Column computer='16'>
	                                    <Field htmlFor="comments" label={t('convocation.fields.comment')}>
	                                        <FieldValue id="comments">{this.state.convocation.comments}</FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor="document" label={t('convocation.fields.convocation_document')}>
	                                        <FieldValue id="document"><Button className="link" primary compact basic>DEC_01_Courrier_logik.pdf</Button></FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                                <Grid.Column mobile='16' computer='8'>
	                                    <Field htmlFor="procuration" label={t('convocation.fields.default_procuration')}>
	                                        <FieldValue id="procuration"><Button className="link" primary compact basic>{this.state.convocation.procuration}</Button></FieldValue>
	                                    </Field>
	                                </Grid.Column>
	                            </Grid>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='4'>
	                            <div className='block-information'>
	                                <Grid columns='1'>
	                                    <Grid.Column>
	                                        <Field htmlFor="Date" label={t('convocation.fields.date')}>
	                                            <FieldValue id="Date">{this.state.convocation.date}</FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                    <Grid.Column>
	                                        <Field htmlFor="assemblyType" label={t('convocation.fields.assembly_type')}>
	                                            <FieldValue id="assemblyType">{this.state.convocation.assemblyType}</FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                    <Grid.Column>
	                                        <Field htmlFor="assemblyPlace" label={t('convocation.fields.assembly_place')}>
	                                            <FieldValue id="assemblyPlace">{this.state.convocation.assemblyPlace}</FieldValue>
	                                        </Field>
	                                    </Grid.Column>
	                                </Grid>
	                            </div>
	                        </Grid.Column>
	                    </Grid>
	                    <h2>Envoi</h2>
	                    <Grid columns='3'>
	                        <Grid.Column mobile='16' tablet='8' computer='6'>
	                            <Field htmlFor="transmitterGroup" label='Groupe émétteur'>
	                                <FieldValue id="transmitterGroup">{this.state.convocation.transmitterGroup}</FieldValue>
	                            </Field>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' tablet='8' computer='6'>
	                            <Field htmlFor="sendBy" label='Convocation envoyée par'>
	                                <FieldValue id="sendBy">{this.state.convocation.sendBy}</FieldValue>
	                            </Field>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='4'>
	                            <Field htmlFor="sendingDate" label='Date d envoie'>
	                                <FieldValue id="sendingDate">{this.state.convocation.sendingDate}</FieldValue>
	                            </Field>
	                        </Grid.Column>
	                    </Grid>
	                    <h2>Ma réponse</h2>
	                    <FormFieldInline htmlFor='residentThreshold'
	                        label='Serez-vous présent?'>
	                        <Radio
	                            label='Présent'
	                            value='Présent'
	                            name='residentThreshold'
	                            //checked={this.state.fields.residentThreshold === true}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'residentThreshold')}
	                        ></Radio>
	                        <Radio
	                            label='Absent'
	                            value='Absent'
	                            name='residentThreshold'
	                            //checked={this.state.fields.residentThreshold === true}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'residentThreshold')}
	                        ></Radio>
	                        <Radio
	                            label='Procuration'
	                            name='residentThreshold'
	                            value='Procuration'
	                            //checked={this.state.fields.residentThreshold === false}
	                            onChange={(e, {value}) => this.handleChangeRadio(e, value, 'residentThreshold')}
	                        ></Radio>
	                    </FormFieldInline>
	                    <h2>Questions supplémentaires</h2>
	                </Form>
	            </Segment>
	        </Page>
	    )
	}

}

export default translate(['convocation', 'api-gateway'])(ReceivedConvocation)