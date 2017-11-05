import React from 'react'
import { Form, Grid, Card, Icon } from 'semantic-ui-react'

import { bytesToSize } from '../_util/utils'

const FormField = ({ htmlFor, label, children, inline }) =>
    <Form.Field inline={inline ? true : false}>
        <label htmlFor={htmlFor}>{label}</label>
        {children}
    </Form.Field>

const FormFieldInline = ({ htmlFor, label, children }) =>
    <Form.Field inline>
        <Grid>
            <Grid.Column className='inline-grid' width={4}><label htmlFor={htmlFor}>{label}</label></Grid.Column>
            <Grid.Column className='inline-grid' width={12}>{children}</Grid.Column>
        </Grid>
    </Form.Field>

const Field = ({ htmlFor, label, children }) =>
    <Grid>
        <Grid.Column width={4}><label htmlFor={htmlFor}>{label}</label></Grid.Column>
        <Grid.Column width={12}><strong>{children}</strong></Grid.Column>
    </Grid>

const File = ({ attachment, onDelete }) =>
    <Card>
        <Card.Content>
            <Icon style={{ float: 'left' }} name='file outline' size='big' />
            <Icon style={{ float: 'right', cursor: 'pointer' }} name='remove' onClick={() => onDelete(attachment.uuid)} />
            <Card.Header style={{ fontSize: 1 + 'em' }}>{attachment.filename}</Card.Header>
            <Card.Meta>{bytesToSize(attachment.size)}</Card.Meta>
        </Card.Content>
    </Card>

const InputFile = ({ htmlFor, label, children }) =>
    <div>
        <label htmlFor={htmlFor} className="ui icon button">
            <Icon name='file' /> {label}
        </label>
        {children}
    </div>


module.exports = { FormField, FormFieldInline, Field, File, InputFile }
