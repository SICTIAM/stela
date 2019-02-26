import React, { Component } from 'react'
import PropTypes from 'prop-types'
import {bytesToSize} from '../_util/utils'
import {Dropdown, Popup, Table, Icon} from 'semantic-ui-react'
import {translate} from 'react-i18next'
import {withAuthContext} from '../Auth'
import {ErrorListPopup} from '../_components/UI'

class FormFiles extends Component{
    static contextTypes = {
        t: PropTypes.func,
    }

    createDropDownAttachmentType = (file) => {
        const {attachmentTypeOptions, onAttachmentTypeChange, errors} = this.props
        if(attachmentTypeOptions && attachmentTypeOptions.length > 0) {
            return (<Dropdown scrolling
                placeholder={this.context.t('acte.new.PJ_types')}
                options={attachmentTypeOptions}
                onChange={(e, {value}) => onAttachmentTypeChange(e, {value}, file.uuid)}
                value={file.attachmentTypeCode}
            />)
        } else {
            return( <Popup className='validation-popup'
                trigger={
                    <span>
                        <Dropdown scrolling
                            className={'disabled-error'}
                            placeholder={this.context.t('acte.new.PJ_types')}
                            disabled={true}/>
                    </span>
                } position='top right' verticalOffset={10}
                content={<ErrorListPopup errorList={errors} />}
            />
            )
        }
    }

    createRowFromFiles = () => {
        const {files, onDelete} = this.props
        return files.map((file,index) => (
            <Table.Row key={`${file.filename}_${index}`}>
                <Table.Cell width={4}>{file.filename}</Table.Cell>
                <Table.Cell width={8}> {this.createDropDownAttachmentType(file)}</Table.Cell>
                <Table.Cell width={2}> {bytesToSize(file.size)}</Table.Cell>
                <Table.Cell width={2}><Icon color={'red'}
                    style={{cursor:'pointer'}}
                    name={'trash'}
                    onClick={() => onDelete(file)}/>
                </Table.Cell>
            </Table.Row>))
    }


    render(){

        return (
            <Table basic='very' textAlign={'center'}>
                <Table.Header>
                    <Table.Row>
                        <Table.HeaderCell width={4}>{this.context.t('acte.new.file')}</Table.HeaderCell>
                        <Table.HeaderCell width={8}>{this.context.t('acte.new.PJ_type')}</Table.HeaderCell>
                        <Table.HeaderCell width={2}>{this.context.t('acte.new.file_size')}</Table.HeaderCell>
                        <Table.HeaderCell width={2}>{this.context.t('acte.new.delete_file')}</Table.HeaderCell>
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
    onAttachmentTypeChange: PropTypes.func,
    onDelete: PropTypes.func,
    errors: PropTypes.array
}


export default translate(['acte', 'api-gateway'])(withAuthContext(FormFiles))
