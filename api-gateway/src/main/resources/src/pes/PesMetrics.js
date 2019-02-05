import React, {Component} from 'react'
import {translate} from 'react-i18next'
import {withAuthContext} from '../Auth'
import {Page} from '../_components/UI'
import faker from 'faker'
import moment from 'moment'
import PropTypes from 'prop-types'
import MetricsSegment from '../_components/MetricsSegment'

const pesColors = {
    waitingPes: {color: '#e74c3c', borderColor: '#c0392b'},
    ackPes: {color: '#2ecc71', borderColor: '#27ae60'},
    sentPes: {color: '#9b59b6', borderColor: '#8e44ad'}
}


class PesMetrics extends Component {
    static contextTypes = {
        t: PropTypes.func
    }

    state = {
        waitingPes: [],
        sentPesForWaiting: [],
        sentPesForACK: [],
        ackPes: [],
        refDate: moment(),
        falseDoughnutSentWaiting: [],
        falseDoughnutSentACK: [],
    }

    componentDidMount = async () => {
        //TODO remove fake data
        this._updateDataSentWaiting()
        this._updateDataSentACK()
    }

    _updateDataSentWaiting = () => {
        this.setState({
            sentPesForWaiting: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            waitingPes: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            falseDoughnutSentWaiting: this._computeChartArraysToDoughnutValues()
        })
    }

    _updateDataSentACK = () => {
        this.setState({
            sentPesForACK: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            ackPes: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            falseDoughnutSentACK: this._computeChartArraysToDoughnutValues()
        })
    }

    fetchFakeDatas = () => {
        const {refDate} = this.state
        let array = []
        let minDate = refDate.clone().subtract(1, 'hour')
        for (let i = 0; i < 100; i++) {
            array.push(faker.date.between(minDate, refDate))
        }
        return array
    }

    _handleFetchData = (period, metric) => {
        let updateFunction = null

        switch (metric) {
        case 'SENT_ACK':
            updateFunction = this._updateDataSentACK
            break
        case 'SENT_WAITING':
            updateFunction = this._updateDataSentWaiting
            break
        default:
            break
        }

        switch (period) {
        case 'hour':
            updateFunction()
            break
        case 'day':
            updateFunction()
            break
        case 'week':
            updateFunction()
            break
        default:
            break
        }
    }

    _transformTimestampToChartArray = (timestampsArray) => {
        const {refDate} = this.state
        let resArray = []

        /*let diffTimesArray = timestampsArray
             .map(timestamp => {
                 let diff = refDate.clone().diff(timestamp)
                 return Math.floor(moment.duration(diff).asMinutes())
             })

        //more powerful than a map on large arrays
         for (let diffTime of diffTimesArray) {
             let value = resArray[diffTime] ? resArray[diffTime]['y'] + 1 : 1
             resArray[diffTime] = {x: diffTime, y: value}
         }*/

        for (let time of timestampsArray) {
            let diff = refDate.clone().diff(time)
            let index = Math.floor(moment.duration(diff).asMinutes())
            let value = resArray[index] ? resArray[index]['y'] + 1 : 1
            resArray[index] = {x: moment(time), y: value}
        }

        return resArray
    }

    _computeChartArraysToDoughnutValues = () => {
        let fakeNumber = faker.random.number({min: 0, max: 100})
        return [fakeNumber, 100 - fakeNumber]
    }


    _handleChartDatasetsSentpesWaitingpes = () => {
        const {sentPesForWaiting, waitingPes} = this.state
        const {t} = this.context
        return [
            {
                label: t('api-gateway:metrics.PES_SENT'),
                data: sentPesForWaiting,
                showLine: true,
                fill: false,
                borderColor: pesColors.sentPes.borderColor
            },
            {
                label: t('api-gateway:metrics.PES_WAITING'),
                data: waitingPes,
                showLine: true,
                fill: false,
                borderColor: pesColors.waitingPes.borderColor
            }]
    }

    _handleChartDatasetsSentpesACKpes = () => {
        const {sentPesForACK, ackPes} = this.state
        const {t} = this.context
        return [
            {
                label: t('api-gateway:metrics.PES_SENT'),
                data: sentPesForACK,
                showLine: true,
                fill: false,
                borderColor: pesColors.sentPes.borderColor
            },
            {
                label: t('api-gateway:metrics.PES_ACK'),
                data: ackPes,
                showLine: true,
                fill: false,
                borderColor: pesColors.ackPes.borderColor
            }]
    }


    _handleDoughnutDatasetsSentpesWaitingpes = () => {
        const {falseDoughnutSentWaiting} = this.state
        return [{
            data: falseDoughnutSentWaiting,
            backgroundColor: [
                pesColors.sentPes.color,
                pesColors.waitingPes.color
            ],
            borderColor: [
                pesColors.sentPes.borderColor,
                pesColors.waitingPes.borderColor
            ],
            borderWidth: 1
        }]
    }

    _handleDoughnutDatasetsSentpesACKpes = () => {
        const {falseDoughnutSentACK} = this.state
        return [{
            data: falseDoughnutSentACK,
            backgroundColor: [
                pesColors.sentPes.color,
                pesColors.ackPes.color
            ],
            borderColor: [
                pesColors.sentPes.borderColor,
                pesColors.ackPes.borderColor
            ],
            borderWidth: 1
        }]
    }


    render() {
        const {t} = this.context

        return (
            <Page title={'Flux PES'}>
                <MetricsSegment
                    doughnutLabels={[t('api-gateway:metrics.PES_SENT'), t('api-gateway:metrics.PES_WAITING')]}
                    doughnutDatasets={this._handleDoughnutDatasetsSentpesWaitingpes()}
                    chartDatasets={this._handleChartDatasetsSentpesWaitingpes()}
                    onClickButton={(period) => this._handleFetchData(period, 'SENT_WAITING')}
                />

                <MetricsSegment
                    doughnutLabels={[t('api-gateway:metrics.PES_SENT'), t('api-gateway:metrics.PES_ACK')]}
                    doughnutDatasets={this._handleDoughnutDatasetsSentpesACKpes()}
                    chartDatasets={this._handleChartDatasetsSentpesACKpes()}
                    onClickButton={(period) => this._handleFetchData(period, 'SENT_ACK')}
                />
            </Page>
        )
    }

}


export default translate(['pes', 'api-gateway'])(withAuthContext(PesMetrics))
