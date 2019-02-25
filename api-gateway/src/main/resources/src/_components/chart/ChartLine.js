import React, {Component} from 'react'
import Chart from 'chart.js'
import PropTypes from 'prop-types'

export class ChartLine extends Component {

    state = {
        chart: null,
        rendered: false
    }

    componentWillUpdate(nextProps, nextState, nextContext) {
        //that is to avoid the chart to display old data when you are hover it
        const {datasets, min, max, displayLegends} = this.props
        const {chart} = this.state
        const refDatasets = datasets && datasets.map(sets => JSON.stringify(sets.data)).join()
        const newDatasets = nextProps.datasets && nextProps.datasets.map(sets => JSON.stringify(sets.data)).join()

        if (chart && refDatasets !== newDatasets) {
            chart.destroy()
            let newChart = new Chart(this.canvas, {
                type: 'scatter',
                data: {
                    datasets: nextProps.datasets
                },
                options: {
                    legend: {
                        display: displayLegends
                    },
                    scales: {
                        xAxes: [{
                            type: 'time',
                            time: {
                                min: min,
                                max: max,
                                tooltipFormat: 'h:mm a'
                            }
                        }],
                        yAxes: [{
                            ticks: {
                                beginAtZero: true
                            }
                        }]
                    }
                }
            })
            this.setState({chart: newChart})
        }
    }

    _initChart = () => {
        const {rendered, chart} = this.state
        const {datasets, min, max, displayLegends} = this.props

        if (!rendered) {
            this.setState({rendered: true})
        }

        if (rendered && !chart) {
            let newChart = new Chart(this.canvas, {
                type: 'scatter',
                data: {
                    datasets: datasets
                },
                options: {
                    legend:{
                        display: displayLegends
                    },
                    scales: {
                        xAxes: [{
                            type: 'time',
                            time: {
                                min: min,
                                max: max,
                                tooltipFormat: 'h:mm a',
                            },
                        }],
                        yAxes: [{
                            ticks: {
                                beginAtZero: true
                            }
                        }]
                    }
                }
            })
            this.setState({chart: newChart})
        }
    }


    render() {
        const {height, width} = this.props

        this._initChart()

        return (
            <canvas ref={(chart) => this.canvas = chart} width={width} height={height}/>
        )
    }
}

ChartLine.defaultProps = {
    height: 400,
    width: 400
}

ChartLine.propTypes = {
    height: PropTypes.number,
    width: PropTypes.number,
    datasets: PropTypes.array,
    min: PropTypes.object,
    max: PropTypes.object,
    displayLegends: PropTypes.bool
}
