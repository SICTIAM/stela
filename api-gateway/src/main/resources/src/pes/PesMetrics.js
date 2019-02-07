import React, {Component} from 'react'
import {translate} from 'react-i18next'
import {withAuthContext} from '../Auth'
import {Page} from '../_components/UI'
import faker from 'faker'
import moment from 'moment'
import PropTypes from 'prop-types'
import MetricsSegment from '../_components/MetricsSegment'
import {PESstatusType} from '../_models/PES-status-type'

const pesColors = {
    PENDING_SENDPes: {color: '#e74c3c', borderColor: '#c0392b'},
    ACK_RECEIVEDPes: {color: '#2ecc71', borderColor: '#27ae60'},
    SENTPes: {color: '#9b59b6', borderColor: '#8e44ad'},
    SIGNATURE_MISSINGPes: {color: '#9b59b6', borderColor: '#8e44ad'},
}


class PesMetrics extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }

    state = {
        waitingPes: [],
        sentPesForWaiting: [],
        sentPesForACK: [],
        ackPes: [],
        refDate: moment(),
        doughnutSentWaiting: [],
        doughnutSentACK: [],
    }

    componentDidMount = async () => {
        const {refDate} = this.state
        let toDate = this._convertDateToLocalTime(refDate)
        let fromDate = this._convertDateToLocalTime(refDate.clone().subtract(1, 'hour'))

        this._updateDataSentWaiting(fromDate, toDate)
        this._updateDataSentACK(fromDate, toDate)
    }

    _updateDataSentWaiting = async (fromDate, toDate) => {
        this.setState({
            sentPesForWaiting: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            waitingPes: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            doughnutSentWaiting: await this._getDoughnutValues(fromDate, toDate, [PESstatusType.SIGNATURE_MISSING, PESstatusType.PENDING_SEND])
        })
    }

    _updateDataSentACK = async (fromDate, toDate) => {
        this.setState({
            sentPesForACK: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            ackPes: this._transformTimestampToChartArray(this.fetchFakeDatas()),
            doughnutSentACK: await this._getDoughnutValues(fromDate, toDate, [PESstatusType.SENT, PESstatusType.ACK_RECEIVED])
        })
    }

    _getPesNumber = async (fromDate, toDate, statusType) => {
        const {_fetchWithAuthzHandling} = this.context
        const queryParams = {fromDate: fromDate, toDate: toDate, statusType: statusType}
        return await (await _fetchWithAuthzHandling({url: '/api/pes/metric', query: queryParams})).json()
    }

    _getDoughnutValues = async (fromDate, toDate, statusTypeArray) => {
        let fetchPesNumbers = statusTypeArray.map(async statusType => this._getPesNumber(fromDate, toDate, statusType))
        return await Promise.all(fetchPesNumbers)
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

    _handleFetchData = async (period, metric) => {
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

        let fromDate = null
        let toDate = moment()
        switch (period) {
        case 'hour':
            fromDate = toDate.clone().subtract(1, 'h')
            break
        case 'day':
            fromDate = toDate.clone().subtract(1, 'd')
            break
        case 'week':
            fromDate = toDate.clone().subtract(1, 'w')
            break
        default:
            break
        }
        fromDate = this._convertDateToLocalTime(fromDate)
        toDate = this._convertDateToLocalTime(toDate)
        updateFunction(fromDate.toString(), toDate.toString())
    }

    _convertDateToLocalTime = (date) => {
        const fmt = 'YYYY-MM-DD HH:mm:ss'
        return moment.utc(date, fmt).local().format(fmt)
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

    _handleChartDatasetsSentpesWaitingpes = () => {
        const {sentPesForWaiting, waitingPes} = this.state
        const {t} = this.context
        return [
            {
                label: t('api-gateway:metrics.PES_SENT'),
                data: sentPesForWaiting,
                showLine: true,
                fill: false,
                borderColor: pesColors.SENTPes.borderColor
            },
            {
                label: t('api-gateway:metrics.PES_WAITING'),
                data: waitingPes,
                showLine: true,
                fill: false,
                borderColor: pesColors.PENDING_SENDPes.borderColor
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
                borderColor: pesColors.SENTPes.borderColor
            },
            {
                label: t('api-gateway:metrics.PES_ACK'),
                data: ackPes,
                showLine: true,
                fill: false,
                borderColor: pesColors.ACK_RECEIVEDPes.borderColor
            }]
    }


    _handleDoughnutDatasets = (doughnutValues) => {
        let backgroundColor = []
        let borderColor = []
        let data = []

        doughnutValues.forEach(item => {
            let key = Object.keys(item)
            let value = item[key[0]]
            backgroundColor.push(pesColors[`${key[0]}Pes`].color)
            borderColor.push(pesColors[`${key[0]}Pes`].borderColor)
            data.push(value)
        })

        return [{
            data: data,
            backgroundColor: backgroundColor,
            borderColor: borderColor,
            borderWidth: 1
        }]
    }

    render() {
        const {t} = this.context
        const {doughnutSentWaiting, doughnutSentACK} = this.state

        return (
            <Page title={'Flux PES'}>
                <MetricsSegment
                    doughnutLabels={[t('api-gateway:metrics.PES_SENT'), t('api-gateway:metrics.PES_WAITING')]}
                    doughnutDatasets={this._handleDoughnutDatasets(doughnutSentWaiting)}
                    chartDatasets={this._handleChartDatasetsSentpesWaitingpes()}
                    onClickButton={(period) => this._handleFetchData(period, 'SENT_WAITING')}
                />

                <MetricsSegment
                    doughnutLabels={[t('api-gateway:metrics.PES_SENT'), t('api-gateway:metrics.PES_ACK')]}
                    doughnutDatasets={this._handleDoughnutDatasets(doughnutSentACK)}
                    chartDatasets={this._handleChartDatasetsSentpesACKpes()}
                    onClickButton={(period) => this._handleFetchData(period, 'SENT_ACK')}
                />
            </Page>
        )
    }

}


export default translate(['pes', 'api-gateway'])(withAuthContext(PesMetrics))
