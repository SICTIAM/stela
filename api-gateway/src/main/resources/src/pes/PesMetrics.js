import React, {Component} from 'react'
import {translate} from 'react-i18next'
import {withAuthContext} from '../Auth'
import {Page} from '../_components/UI'
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
        const sample = 'second'
        const toDate = this._convertDateToLocalTime(refDate)
        const fromDate = this._convertDateToLocalTime(refDate.clone().subtract(1, 'hour'))

        this._updateDataSentWaiting(fromDate, toDate, sample)
        this._updateDataSentACK(fromDate, toDate, sample)
    }

    _getPesNumber = async (fromDate, toDate, statusType) => {
        const {_fetchWithAuthzHandling} = this.context
        const queryParams = {fromDate: fromDate, toDate: toDate, statusType: statusType}
        return await (await _fetchWithAuthzHandling({url: '/api/pes/metric', query: queryParams})).json()
    }

    _getNumberOfPesWithSample = async (fromDate, toDate, statusType, sample) => {
        const {_fetchWithAuthzHandling} = this.context
        const queryParams = {fromDate: fromDate, toDate: toDate, statusType: statusType, sample: sample}
        return await (await _fetchWithAuthzHandling({url: '/api/pes/metric/sample', query: queryParams})).json()
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
        let sample = ''
        switch (period) {
        case 'hour':
            fromDate = toDate.clone().subtract(1, 'h')
            sample = 'second'
            break
        case 'day':
            fromDate = toDate.clone().subtract(1, 'd')
            sample = 'minute'
            break
        case 'week':
            fromDate = toDate.clone().subtract(1, 'w')
            sample = 'hour'
            break
        default:
            break
        }
        fromDate = this._convertDateToLocalTime(fromDate)
        toDate = this._convertDateToLocalTime(toDate)
        updateFunction(fromDate.toString(), toDate.toString(), sample)
    }

    _updateDataSentWaiting = async (fromDate, toDate, sample) => {
        /*
        TODO
        REPLACE ALL THE SIGNATURE_MISSING by SENT
        THEN pass min and max to props
        */
        let fetchSentPesForWaiting = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.SIGNATURE_MISSING, 'minute')
        let fetchWaitingPes = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.PENDING_SEND, 'minute')

        this.setState({
            sentPesForWaiting: this._transformDataToCoordinate(fetchSentPesForWaiting[PESstatusType.SIGNATURE_MISSING]),
            waitingPes: this._transformDataToCoordinate(fetchWaitingPes[PESstatusType.PENDING_SEND]),
            doughnutSentWaiting: await this._getDoughnutValues(fromDate, toDate, [PESstatusType.SIGNATURE_MISSING, PESstatusType.PENDING_SEND])
        })
    }

    _updateDataSentACK = async (fromDate, toDate, sample) => {
        let fetchSentPesForWaiting = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.SIGNATURE_MISSING, 'minute')
        let fetchWaitingPes = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.ACK_RECEIVED, 'minute')

        this.setState({
            sentPesForACK: this._transformDataToCoordinate(fetchSentPesForWaiting[PESstatusType.SIGNATURE_MISSING]),
            ackPes: this._transformDataToCoordinate(fetchWaitingPes[PESstatusType.ACK_RECEIVED]),
            doughnutSentACK: await this._getDoughnutValues(fromDate, toDate, [PESstatusType.SIGNATURE_MISSING, PESstatusType.ACK_RECEIVED])
        })
    }

    _getDoughnutValues = async (fromDate, toDate, statusTypeArray) => {
        let fetchPesNumbers = statusTypeArray.map(async statusType => this._getPesNumber(fromDate, toDate, statusType))
        return await Promise.all(fetchPesNumbers)
    }

    _handleChartDatasets = (chartsCoordinatesArrays) => {
        const {t} = this.context

        return chartsCoordinatesArrays.map(item => {
            return {
                label: t(`api-gateway:metrics.pes.${item.statusType}`),
                data: item.coordinates,
                showLine: true,
                fill: false,
                borderColor: pesColors[`${item.statusType}Pes`].borderColor
            }
        })
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

    _convertDateToLocalTime = (date) => {
        const fmt = 'YYYY-MM-DD HH:mm:ss'
        return moment.utc(date, fmt).local().format(fmt)
    }

    _transformDataToCoordinate = (dataArray) => {
        return dataArray.map(item => {
            return {x: item.date_time, y: item.count}
        })
    }

    render() {
        const {t} = this.context
        const {doughnutSentWaiting, doughnutSentACK, sentPesForWaiting, waitingPes, sentPesForACK, ackPes} = this.state
        const chart1 = [
            {coordinates: sentPesForWaiting, statusType: PESstatusType.SIGNATURE_MISSING},
            {coordinates: waitingPes, statusType: PESstatusType.PENDING_SEND}
        ]
        const chart2 = [
            {coordinates: sentPesForACK, statusType: PESstatusType.SIGNATURE_MISSING},
            {coordinates: ackPes, statusType: PESstatusType.ACK_RECEIVED}
        ]


        return (
            <Page title={'Flux PES'}>
                <MetricsSegment
                    doughnutLabels={[t('api-gateway:metrics.pes.SENT'), t('api-gateway:metrics.pes.PENDING_SEND')]}
                    doughnutDatasets={this._handleDoughnutDatasets(doughnutSentWaiting)}
                    chartDatasets={this._handleChartDatasets(chart1)}
                    onClickButton={(period) => this._handleFetchData(period, 'SENT_WAITING')}
                />

                <MetricsSegment
                    doughnutLabels={[t('api-gateway:metrics.pes.SENT'), t('api-gateway:metrics.pes.ACK_RECEIVED')]}
                    doughnutDatasets={this._handleDoughnutDatasets(doughnutSentACK)}
                    chartDatasets={this._handleChartDatasets(chart2)}
                    onClickButton={(period) => this._handleFetchData(period, 'SENT_ACK')}
                />
            </Page>
        )
    }

}


export default translate(['pes', 'api-gateway'])(withAuthContext(PesMetrics))
