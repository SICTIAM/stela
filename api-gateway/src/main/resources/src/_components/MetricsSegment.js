import React, {Component} from 'react'
import {ChartDoughnut} from './chart/ChartDoughnut'
import {ChartLine} from './chart/ChartLine'
import {Grid, Segment, Button, SegmentGroup} from 'semantic-ui-react'
import PropTypes from 'prop-types'
import moment from 'moment'

export default class MetricsSegment extends Component {

    state = {
        activeButton: 'hour',
        period: {min: moment().subtract(1, 'h'), max: moment()}
    }

    _handleActiveButton = (buttonType) => {
        const {activeButton} = this.state
        return activeButton === buttonType
    }

    _handleButtonClick = (buttonType) => {
        const {onClickButton} = this.props
        onClickButton(buttonType)
        this._handleMinMax(buttonType)
        this.setState({activeButton: buttonType})
    }

    _handleMinMax = (timeType) => {
        let max = moment()
        let min = null

        switch (timeType) {
        case 'hour':
            min = max.clone().subtract(1, 'h')
            break
        case 'day':
            min = max.clone().subtract(1, 'd')
            break
        case 'week':
            min = max.clone().subtract(1, 'w')
            break
        default:
            break
        }
        this.setState({period: {min: min, max: max}})
    }

    _isDataPresent = (chart) => {
        const res = chart.filter(item => {
            return item.data.length > 0
        })
        return res.length > 0
    }

    _displayLegends = (chartDatasets) => {
        const res = chartDatasets.map(item => {
            return (
                <React.Fragment>
                    <div style={{backgroundColor: item.borderColor, ...styles.legend}}/>
                    <p style={{margin: 0}}>{item.label}</p>
                </React.Fragment>
            )
        })
        return (
            <div style={styles.row}>
                {res}
            </div>
        )
    }

    render() {
        const {doughnutLabels, doughnutDatasets, chartDatasets} = this.props
        const {period} = this.state
        const isDataPresent= this._isDataPresent(chartDatasets)

        return (
            <SegmentGroup>
                <Segment>
                    <Grid>
                        { isDataPresent &&
                            <Grid.Row>
                                <Grid.Column width={4} verticalAlign={'middle'} textAlign={'center'}>
                                    <ChartDoughnut
                                        labels={doughnutLabels}
                                        datasets={doughnutDatasets}
                                        displayLegends={false}/>
                                </Grid.Column>
                                <Grid.Column width={12} verticalAlign={'middle'}>
                                    <ChartLine height={100} width={250} datasets={chartDatasets} min={period.min} max={period.max} displayLegends={false}/>
                                </Grid.Column>
                            </Grid.Row>
                        }
                        {
                            !isDataPresent &&
                            <Grid.Row>
                                <Grid.Column width={16}>
                                    <div style={{height:'20vh', display: 'flex', alignItems:'center', justifyContent: 'center'}}>
                                        <p>{`${this.props.t('pes:pes.metrics.no_data_to_display')} ${this.props.t('pes:pes.metrics.no_data_to_display', {context: this.state.activeButton})}`}</p>
                                    </div>
                                </Grid.Column>
                            </Grid.Row>
                        }
                    </Grid>
                </Segment>
                <Segment>
                    <Grid>
                        <Grid.Row style={{padding: '0.5rem 0 0.5rem 0'}} verticalAlign={'middle'}>
                            <Grid.Column width={10} floated={'left'}>
                                {this._displayLegends(chartDatasets)}
                            </Grid.Column>
                            <Grid.Column width={6} floated={'right'}>
                                <Button.Group size={'mini'} floated={'right'} basic>
                                    <Button onClick={() => this._handleButtonClick('hour')} active={this._handleActiveButton('hour')}>1h</Button>
                                    <Button onClick={() => this._handleButtonClick('day')} active={this._handleActiveButton('day')}>1j</Button>
                                    <Button onClick={() => this._handleButtonClick('week')} active={this._handleActiveButton('week')}>1s</Button>
                                </Button.Group>
                            </Grid.Column>
                        </Grid.Row>
                    </Grid>
                </Segment>
            </SegmentGroup>
        )


    }
}

const styles = {
    legend:{
        width: '30px',
        height: '10px',
        margin: '0.5em'
    },
    row: {
        display: 'flex',
        flexDirection:'row',
        alignItems: 'center'
    }
}

MetricsSegment.propTypes = {
    doughnutLabels: PropTypes.array,
    doughnutDatasets: PropTypes.array,
    chartDatasets: PropTypes.array,
    onClickButton: PropTypes.func,
}
