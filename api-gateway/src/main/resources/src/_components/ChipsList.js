import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { Button } from 'semantic-ui-react'

import Chip from './Chip'

export default class ChipList extends Component {
	static propTypes = {
	    list: PropTypes.array.isRequired,
	    onRemoveChip: PropTypes.func,
	    removable: PropTypes.bool,
	    numberChipDisplay: PropTypes.number,
	    labelText: PropTypes.string.isRequired
	}
	static defaultProps = {
	    numberChipDisplay: 2,
	    removable: false
	}
	state = {
	    showMore: true
	}
	render() {
	    const chipToDisplay = this.state.showMore && this.props.numberChipDisplay > 0 && this.props.list.length > this.props.numberChipDisplay ? this.props.list.slice(0, this.props.numberChipDisplay) : this.props.list.slice()

	    const chipsList = chipToDisplay.map((chip, index) => {
	        console.log(index)
	        return <Chip
	            key={`chip-${index}`}
	            removable={this.props.removable}
	            text={chip[this.props.labelText]}
	            onRemoveChip={() => this.props.onRemoveChip(index) }/>
	    })
	    return (
	        <Fragment>
	            {chipsList.length > 0 && (
	                <Fragment>
	                    {chipsList}
	                    { this.props.numberChipDisplay > 0 && this.props.list.length > this.props.numberChipDisplay &&(
	                        <div className='mt-15'>
	                                <Button onClick={() => this.setState({showMore: !this.state.showMore})} className="link" primary compact basic>
	                                {this.state.showMore && (
	                                    <span>{this.props.viewMoreText}</span>
	                                )}
	                                {!this.state.showMore && (
	                                    <span>{this.props.viewLessText}</span>
	                                )}
	                            </Button>
	                        </div>
	                    )}

	                </Fragment>
	            )}
	        </Fragment>
	    )
	}
}