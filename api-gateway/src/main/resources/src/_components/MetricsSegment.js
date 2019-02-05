import React, {Component} from 'react'
import {ChartDoughnut} from './chart/ChartDoughnut'
import {ChartLine} from './chart/ChartLine'
import {Grid, Segment, Button, SegmentGroup} from 'semantic-ui-react'
import PropTypes from 'prop-types'
import moment from 'moment'

export default class MetricsSegment extends Component {

    state = {
        activeButton: 'hour'
    }


    _handleActiveButton = (buttonType) => {
        const {activeButton} = this.state
        return activeButton === buttonType
    }

    _handleButtonClick = (buttonType) => {
        const {onClickButton} = this.props
        onClickButton(buttonType)
        this.setState({activeButton: buttonType})
    }

    render() {
        const {doughnutLabels, doughnutDatasets, chartDatasets} = this.props

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
                                <ChartLine height={100} width={250} datasets={chartDatasets} min={moment().subtract(1, this.state.activeButton)} max={moment()} />
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
