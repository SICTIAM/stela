import React, {Component} from 'react'
import {translate} from 'react-i18next'
import {withAuthContext} from '../Auth'
import {Page} from '../_components/UI'
import moment from 'moment'
import PropTypes from 'prop-types'
import MetricsSegment from '../_components/MetricsSegment'
import {PESstatusType} from '../_models/PES-status-type'

const pesColors = {
    CLASSEUR_SIGNEDPes: {color: '#2ecc71', borderColor: '#27ae60'},
    PENDING_SIGNATUREPes: {color: '#e74c3c', borderColor: '#c0392b'},
    ACK_RECEIVEDPes: {color: '#2ecc71', borderColor: '#27ae60'},
    CREATEDPes: {color: '#9b59b6', borderColor: '#8e44ad'},
    SENTPes: {color: '#fbc531', borderColor: '#e1b12c'},
}


class PesMetrics extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }

    state = {
        /* Chart 1*/
        sentPesChart1: [],
        createdPesChart1: [],
        ackPesChart1: [],
        doughnutSentCreatedAck:[],
        /* Chart 2 */
        pendingSignatureChart2: [],
        classeurSignedChart2: [],
        doughnutPendingsignatureClasseurSigned: [],

        waitingPes: [],
        sentPesForWaiting: [],
        sentPesForACK: [],
        ackPes: [],
        doughnutSentWaiting: [],
        doughnutSentACK: []
    }

    componentDidMount = async () => {
        const refDate = moment()
        const sample = 'second'
        const toDate = this._convertDateToLocalTime(refDate)
        const fromDate = this._convertDateToLocalTime(refDate.clone().subtract(1, 'hour'))

        this._updateDataMetricSegment1(fromDate, toDate, sample)
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
        case 'metric-segment-2':
            updateFunction = this._updateDataSentACK
            break
        case 'metric-segment-1':
            updateFunction = this._updateDataMetricSegment1
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

    _updateDataMetricSegment1 = async (fromDate, toDate, sample) => {
        let fetchSentPes = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.SENT, sample)
        let fetchCreatedPes = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.CREATED, sample)
        let fetchAckPes = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.ACK_RECEIVED, sample)

        this.setState({
            sentPesChart1: this._transformDataToCoordinate(fetchSentPes[PESstatusType.SENT]),
            createdPesChart1: this._transformDataToCoordinate(fetchCreatedPes[PESstatusType.CREATED]),
            ackPesChart1: this._transformDataToCoordinate(fetchAckPes[PESstatusType.ACK_RECEIVED]),
            doughnutSentCreatedAck: await this._getDoughnutValues(fromDate, toDate, [PESstatusType.SENT, PESstatusType.CREATED, PESstatusType.ACK_RECEIVED])
        })
    }

    _updateDataSentACK = async (fromDate, toDate, sample) => {
        let fetchPendingSignature = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.PENDING_SIGNATURE, sample)
        let fetchClasseurSigned = await this._getNumberOfPesWithSample(fromDate, toDate, PESstatusType.CLASSEUR_SIGNED, sample)

        this.setState({
            pendingSignatureChart2: this._transformDataToCoordinate(fetchPendingSignature[PESstatusType.PENDING_SIGNATURE]),
            classeurSignedChart2: this._transformDataToCoordinate(fetchClasseurSigned[PESstatusType.CLASSEUR_SIGNED]),
            doughnutPendingsignatureClasseurSigned: await this._getDoughnutValues(fromDate, toDate, [PESstatusType.PENDING_SIGNATURE, PESstatusType.CLASSEUR_SIGNED])
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
                label: t(`pes:pes.metrics.status.${item.statusType}`),
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
        const {
            sentPesChart1, createdPesChart1,
            ackPesChart1, doughnutSentCreatedAck,
            doughnutPendingsignatureClasseurSigned,
            classeurSignedChart2, pendingSignatureChart2


        } = this.state
        const chart1 = [
            {coordinates: sentPesChart1, statusType: PESstatusType.SENT},
            {coordinates: createdPesChart1, statusType: PESstatusType.CREATED},
            {coordinates: ackPesChart1, statusType: PESstatusType.ACK_RECEIVED}
        ]
        const chart2 = [
            {coordinates: pendingSignatureChart2, statusType: PESstatusType.PENDING_SIGNATURE},
            {coordinates: classeurSignedChart2, statusType: PESstatusType.CLASSEUR_SIGNED}
        ]


        return (
            <Page title={t('api-gateway:menu.pes.PES_statut')}>
                <MetricsSegment
                    doughnutLabels={[t('pes:pes.metrics.status.SENT'), t('pes:pes.metrics.status.CREATED'), t('pes:pes.metrics.status.ACK_RECEIVED')]}
                    doughnutDatasets={this._handleDoughnutDatasets(doughnutSentCreatedAck)}
                    chartDatasets={this._handleChartDatasets(chart1)}
                    onClickButton={(period) => this._handleFetchData(period, 'metric-segment-1')}
                    t={this.context.t}
                />

                <MetricsSegment
                    doughnutLabels={[t('pes:pes.metrics.status.PENDING_SIGNATURE'), t('pes:pes.metrics.status.CLASSEUR_SIGNED')]}
                    doughnutDatasets={this._handleDoughnutDatasets(doughnutPendingsignatureClasseurSigned)}
                    chartDatasets={this._handleChartDatasets(chart2)}
                    onClickButton={(period) => this._handleFetchData(period, 'metric-segment-2')}
                    t={this.context.t}
                />
            </Page>
        )
    }

}


export default translate(['api-gateway', 'pes'])(withAuthContext(PesMetrics))
