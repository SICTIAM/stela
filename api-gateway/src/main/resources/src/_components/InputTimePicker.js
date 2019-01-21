import React, { Component, Fragment } from 'react'
import InputMask from 'react-input-mask'
import { Popup, Input, Button, Icon } from 'semantic-ui-react'
import MobilePicker from './MobilePicker'

/** Add Dropdown to select Hour */
class InputTimePicker extends Component {
	state = {
	    valueGroups: {
	        hour: '00',
	        minute: '00'
	    },
	    optionGroups: {
	        hour: [],
	        minute: []
	    }
	}
	componentDidMount = () => {
	    const hour = Array.from(new Array(24), (val, index) => {
	        return index<10 ? `0${index}` : `${index}`
	    })
	    const minute = Array.from(new Array(60), (val, index) => {
	        return index<10 ? `0${index}` : `${index}`
	    })
	    const optionGroups = {
	        hour: hour,
	        minute: minute
	    }
	    this.setState({optionGroups: optionGroups})
	}
	handleKeyDown = (e) => {
	    if(e.target.selectionStart !== 2 && e.target.selectionStart !== 5) {
	        let valueToChange = this.props.value[e.target.selectionStart]
	        switch(e.keyCode) {
	        case 38:
	            if(!valueToChange !== ':') {
	                if(valueToChange !== '_') {
	                    valueToChange = +valueToChange + +1
	                } else {
	                    valueToChange = 0
	                }
	                this.onChange(this.replaceAt(this.props.value, [e.target.selectionStart], valueToChange))
	            }
	            break
	        case 40:
	            if(!valueToChange !== ':') {
	                if(valueToChange !== '_') {
	                    valueToChange = +valueToChange + -1
	                } else {
	                    valueToChange = 0
	                }
	                this.onChange(this.replaceAt(this.props.value, [e.target.selectionStart], valueToChange))
	            }
	            break
	        default:
	            return true
	        }
	    }
	}
	replaceAt = (string, index, replace) => {
	    if(replace >=10) {
	        replace = 0
	    } else if(replace < 0) {
	        replace = 9
	    }
	    return string.substring(0, index) + replace + string.substring(+index + +1)
	}
	formateDate24h = (hour) => {
	    let hourMinuteArray = hour.split(':')
	    let newHour = hour
	    /* Hour Validation
		 * First accept only 1_, 2_
		 * Else accept 1x (x [0-9]) and 2x (x [1-3])
		 * If hour doesn't match it is formate
		 */
	    if(hourMinuteArray.length === 2) {
	        if(hourMinuteArray[0].match(/^[3-9][_]$/)) {
	            newHour = newHour.replace(hourMinuteArray[0], `0${hourMinuteArray[0].charAt(0)}`)
	        } else if(hourMinuteArray[0].match(/^[2-9][4-9]$/) || hourMinuteArray[0].match(/^[3-9][0-3]/)) {
	            newHour = newHour.replace(hourMinuteArray[0], `0${hourMinuteArray[0].charAt(1)}`)
	        }
	        /* Minute Validation
			 * First accept only x_ (x [0-5])
			 */
	        if(hourMinuteArray[1].match(/^[6-9][_]$/)) {
	            newHour = newHour.replace(hourMinuteArray[1], `0${hourMinuteArray[1].charAt(0)}`)
	        } else if(hourMinuteArray[1].match(/^[6-9][0-9]$/)) {
	            newHour = newHour.replace(hourMinuteArray[1], `0${hourMinuteArray[1].charAt(1)}`)
	        }

	        const hourFormeMobilePicker = newHour.replace(/[_]/g, '0')
	        this.setState(({valueGroups}) => ({
	            valueGroups: {
	                hour: hourFormeMobilePicker.split(':')[0],
	                minute: hourFormeMobilePicker.split(':')[1]
	            }
	        }))
	    }
	    return newHour
	}
	handlePickerChange = (name, value) => {
	    let valueGroups = this.state.valueGroups
	    valueGroups[name] = value
	    this.setState({valueGroups: valueGroups})

	    this.props.onChange(`${this.state.valueGroups['hour']}:${this.state.valueGroups['minute']}`)

	}
	onChange = (value) => {
	    this.props.onChange(this.formateDate24h(value))
	}
	render() {
	    const popupTime = <Popup on='click' position='bottom center' trigger={<Button primary compact><Icon className='my-0' name='time'></Icon></Button>}>
	        <Popup.Header></Popup.Header>
	        <Popup.Content>
	            <MobilePicker
	                optionGroups={this.state.optionGroups}
	                onChange={this.handlePickerChange}
	                height={200}
	                itemHeight={19.5}
	                valueGroups={this.state.valueGroups}/>
	        </Popup.Content>
	    </Popup>

	    return (
	        <Fragment>
	            <InputMask
	                className={this.props.error ? 'error' : ''}
	                value={this.props.value}
	                placeholder={this.props.placeholder}
	                mask="99:99"
	                onKeyDown={this.handleKeyDown}
	                onChange={event => this.onChange(event.target.value)}
	                onBlur={this.props.onBlur}>
	                {(inputProps) => this.props.dropdown ? <Input {...inputProps} action={popupTime}/> : <Input {...inputProps}/>}
	            </InputMask>
	        </Fragment>
	    )
	}
}

export default InputTimePicker
