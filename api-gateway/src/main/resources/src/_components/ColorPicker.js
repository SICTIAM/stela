import React, { Component } from 'react'
import { CirclePicker } from 'react-color'
import {Popup} from 'semantic-ui-react'

export default class ColorPicker extends Component {
	static defaultProps = {
	    value: ''
	}
	state = {
	    colorPickerOpened: false
	}
	render() {
	    return (
	        <div style={{display: 'flex'}}>
	            <Popup on='click'
	                position={'right center'}
	                open={this.state.colorPickerOpened}
	                onClose={() => this.setState({colorPickerOpened: false})}
            		onOpen={() => this.setState({colorPickerOpened: true})}
	                trigger={
	                <div style={styles(this.props.value)}>
	                </div>}>
	                <CirclePicker onChange={(color, event) => { this.props.onChange(this.props.id, color.hex); this.setState({colorPickerOpened: !this.state.colorPickerOpened})}}/>
	            </Popup>
	        </div>
	    )
	}
}

const styles = (backgroundColor) => ({
    height: '38px',
    width: '38px',
    border: '1px solid #5e5e5e',
    marginRight: '20px',
    borderRadius: '50%',
    backgroundColor: backgroundColor
})