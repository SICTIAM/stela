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

    render() {
        const {doughnutLabels, doughnutDatasets, chartDatasets} = this.props
        const {period} = this.state

        return (
            <SegmentGroup>
                <Segment>
                    <Grid>
                        <Grid.Row>
                            <Grid.Column width={4} verticalAlign={'middle'}>
                                <ChartDoughnut
                                    labels={doughnutLabels}
                                    datasets={doughnutDatasets}/>
                            </Grid.Column>
                            <Grid.Column width={12} verticalAlign={'middle'}>
                                <ChartLine height={100} width={250} datasets={chartDatasets} min={period.min} max={period.max} />
                            </Grid.Column>
                        </Grid.Row>
                    </Grid>
                </Segment>
                <Segment>
                    <Grid>
                        <Grid.Row style={{padding: '0.5rem 0 0.5rem 0'}}>
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

MetricsSegment.propTypes = {
    doughnutLabels: PropTypes.array,
    doughnutDatasets: PropTypes.array,
    chartDatasets: PropTypes.array,
    onClickButton: PropTypes.func,
}
