import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { Grid, Icon } from 'semantic-ui-react'
import { Field, FieldValue } from './UI'

export default class QuickView extends Component {
	static propTypes = {
	    open: PropTypes.bool,
	    header: PropTypes.bool
	}
	static defaultProps = {
	    open: false
	}
	render() {
	    // return (
	    //     <div className={'quick-view' + (this.props.open ? ' open' : '')}>
	    //         {this.props.data && (
	    //             <div>{this.props.data}</div>
	    //         )}
	    //     </div>
	    // )
	    const data = this.props.data && this.props.data.data ? this.props.data.data.map((row) => {
	        return (
	            <Fragment key={row.id}>
	                <Grid.Column mobile={16} computer={row.computer}>
	                    <Field htmlFor={row.id} label={row.label}>
	                        <FieldValue id={row.id}>{row.value}</FieldValue>
	                    </Field>
	                </Grid.Column>
	            </Fragment>
	        )
	    }) : null
	    return (
	        <div className={'quick-view' + (this.props.open ? ' open' : '')}>
	            <div style={{width: '100%'}}>
	                {this.props.header && this.props.data && (
	                    <div className='header'>
	                        <Grid>
	                            <Grid.Column computer={16} style={{display: 'flex', justifyContent: 'flex-end'}}>
	                                <Icon name='close' className='l-icon' onClick={this.props.onClose}/>
	                            </Grid.Column>
	                            {this.props.data.action && (
	                                <Fragment>
	                                    <Grid.Column mobile={16} computer={8}>
	                                        <h3>{this.props.data.headerContent}</h3>
	                                    </Grid.Column>
	                                    <Grid.Column mobile={16} computer={8} style={{display: 'flex', justifyContent: 'flex-end'}}>
	                                        {this.props.data.action}
	                                    </Grid.Column>
	                                </Fragment>
	                            )}
	                            {!this.props.data.action && (
	                                <Grid.Column computer={16}>
	                                    <h3>{this.props.data.headerContent}</h3>
	                                </Grid.Column>
	                            )}
	                        </Grid>
	                    </div>
	                )}

	                <div>
	                    <Grid>
	                        {data}
	                    </Grid>
	                </div>
	            </div>
	        </div>
	    )
	}
}