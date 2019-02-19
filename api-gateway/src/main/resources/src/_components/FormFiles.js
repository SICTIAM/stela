import React, { Component } from 'react'
import PropTypes from 'prop-types'
import {bytesToSize} from '../_util/utils'
import {Dropdown, Popup, Table} from 'semantic-ui-react'
import {translate} from 'react-i18next'
import {withAuthContext} from '../Auth'

class FormFiles extends Component{
    static contextTypes = {
        t: PropTypes.func,
    }

    createDropDownAttachmentType = (file) => {
        const {attachmentTypeOptions, onAttachmentTypeChange} = this.props
        if(attachmentTypeOptions && attachmentTypeOptions.length > 0) {
            return (<Dropdown scrolling
                placeholder={this.context.t('acte.new.PJ_types')}
                options={attachmentTypeOptions}
                onChange={onAttachmentTypeChange}
                value={file.attachmentTypeCode}
            />)
        } else {
            return( <Popup className='validation-popup'
                trigger={
                    <span>
                        <Dropdown scrolling
                            placeholder={this.context.t('acte.new.PJ_types')}
                            disabled={true}/>
                    </span>
                } position='top right' verticalOffset={10}
                content={
                    ['lalala']
                }
            />
            )
        }
    }

    createRowFromFiles = () => {
        const {files} = this.props
        return files.map((file,index) => (
            <Table.Row key={`${file.filename}_${index}`}>
                <Table.Cell width={4}>{file.filename}</Table.Cell>
                <Table.Cell width={8}> {this.createDropDownAttachmentType(file)}</Table.Cell>
                <Table.Cell width={4}> {bytesToSize(file.size)}</Table.Cell>
            </Table.Row>))
    }


    render(){

        return (
            <Table basic='very' textAlign={'center'}>
                <Table.Header>
                    <Table.Row>
                        <Table.HeaderCell width={4}>File Name</Table.HeaderCell>
                        <Table.HeaderCell width={8}>Attachment Type</Table.HeaderCell>
                        <Table.HeaderCell width={4}>Size</Table.HeaderCell>
                    </Table.Row>
                </Table.Header>
                <Table.Body>
                    {this.createRowFromFiles()}
                </Table.Body>
            </Table>
        )
    }
}

FormFiles.proptypes = {
    files: PropTypes.array,
    attachmentTypeOptions: PropTypes.array,
    onAttachmentTypeChange: PropTypes.func
}


export default translate(['acte', 'api-gateway'])(withAuthContext(FormFiles))
