import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import Draggable from 'react-draggable'

class DraggablePosition extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static propTypes = {
        position: PropTypes.object.isRequired,
        label: PropTypes.string.isRequired,
        style: PropTypes.object,
        showPercents: PropTypes.bool,
        handleChange: PropTypes.func.isRequired
    }
    static defaultProps = {
        position: { x: 10, y: 10 },
        height: 300,
        width: 190,
        boxWidth: 50,
        boxHeight: 30,
        paddingPercent: 5
    }
    styles = {
        draggablePositionBox: {
            background: '#fff',
            border: '1px solid #999',
            borderRadius: '3px',
            padding: '3px',
            display: 'inline-block'
        },
        globalStyle: {
            position: 'relative', overflow: 'auto', padding: '0'
        }
    }
    handleDrag = (e, ui) => {
        const x = Math.trunc(ui.x / this.props.width * 100)
        const y = Math.trunc(ui.y / this.props.height * 100)
        this.props.handleChange({ x: x, y: y })
    }
    getPixelPosition = () => {
        const { position, height, width } = this.props
        const x = Math.trunc(position.x * width / 100)
        const y = Math.trunc(position.y * height / 100)
        return { x: x, y: y }
    }
    render() {
        const { t } = this.context
        const { style, label, labelColor, helpText, height, width, boxWidth, boxHeight, paddingPercent } = this.props

        const position = this.getPixelPosition()
        const percentBound = paddingPercent / 100
        const revertPercentBound = (100 - paddingPercent) / 100
        const globalStyle = { height: `${this.props.height}px`, width: `${this.props.width}px`, ...this.globalStyle, ...this.styles.draggablePositionBox }
        const box = { color: labelColor, cursor: 'pointer', width: `${boxWidth}px`, height: `${boxHeight}px`, textAlign: 'center' }
        const bounds = {
            top: height * percentBound,
            left: width * percentBound,
            right: (width * revertPercentBound) - boxWidth,
            bottom: (height * revertPercentBound) - boxHeight
        }
        return (
            <div style={{ ...style, width: this.props.width }}>
                {this.props.showPercents &&
                    <p style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span>{t('acte.stamp_pad.width')}: {this.props.position.x}%</span>
                        <span>{t('acte.stamp_pad.height')}: {this.props.position.y}%</span>
                    </p>
                }
                <div style={globalStyle}>
                    <Draggable position={position} bounds={bounds} onDrag={this.handleDrag}>
                        <div style={{ ...box, ...this.styles.draggablePositionBox }}>
                            {label}
                        </div>
                    </Draggable>
                </div>
                {helpText && <p style={{ fontStyle: 'italic', width: `${this.props.width}px`, textAlign: 'center' }}>{helpText}</p>}
            </div >
        )
    }
}

export default translate(['acte'])(DraggablePosition)