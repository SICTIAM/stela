import React, {Component} from 'react'
import Chart from 'chart.js'
import PropTypes from 'prop-types'

export class ChartDoughnut extends Component {
    state = {
        rendered: false,
        doughnut: null
    }

    componentWillUpdate(nextProps, nextState, nextContext) {
        //that is to avoid the chart to display old data when you are hover it
        const {datasets, labels} = this.props
        const {doughnut} = this.state
        const refDatasets = datasets && datasets.map(sets => JSON.stringify(sets.data)).join()
        const newDatasets = nextProps.datasets && nextProps.datasets.map(sets => JSON.stringify(sets.data)).join()

        if (doughnut && refDatasets !== newDatasets) {
            doughnut.destroy()
            let newDoughnut =
                new Chart(this.canvas, {
                    type: 'doughnut',
                    data: {
                        datasets: nextProps.datasets,
                        labels: labels
                    }
                })
            this.setState({doughnut: newDoughnut})
        }
    }

    _initDoughnut = () => {
        const {rendered, doughnut} = this.state
        const {datasets, labels} = this.props

        if (!rendered) {
            this.setState({rendered: true})
        }

        if (rendered && !doughnut) {
            let newDoughnut =
                new Chart(this.canvas, {
                    type: 'doughnut',
                    data: {
                        datasets: datasets,
                        labels: labels
                    }
                })
            this.setState({doughnut: newDoughnut})
        }
    }

    render() {
        const {size} = this.props

        this._initDoughnut()

        return (
            <canvas ref={(chart) => this.canvas = chart} height={size} width={size}/>
        )
    }
}

ChartDoughnut.defaultProps = {
    size: 30
}

ChartDoughnut.propTypes = {
    size: PropTypes.number,
    datasets: PropTypes.array,
    labels: PropTypes.array
}

