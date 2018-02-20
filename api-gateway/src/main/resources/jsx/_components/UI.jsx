import React from 'react'
import PropTypes from 'prop-types'
import { Form, Grid, Card, Icon, List, Header } from 'semantic-ui-react'

import { bytesToSize } from '../_util/utils'

const FormField = ({ htmlFor, label, children, inline }) =>
    <Form.Field inline={inline ? true : false}>
        <label htmlFor={htmlFor}>{label}</label>
        {children}
    </Form.Field>

const FormFieldInline = ({ htmlFor, label, children }) =>
    <Form.Field inline>
        <Grid>
            <Grid.Column className='inline-grid' width={4}><label style={{ verticalAlign: 'middle' }} htmlFor={htmlFor}>{label}</label></Grid.Column>
            <Grid.Column className='inline-grid' width={12}>{children}</Grid.Column>
        </Grid>
    </Form.Field>

const Field = ({ htmlFor, label, children }) =>
    <Grid>
        <Grid.Column width={4}><label style={{ verticalAlign: 'middle' }} htmlFor={htmlFor}>{label}</label></Grid.Column>
        <Grid.Column width={12}><strong>{children}</strong></Grid.Column>
    </Grid>

const File = ({ attachment, onDelete, extraContent }) =>
    <Card>
        <Card.Content>
            <Icon style={{ float: 'left' }} name='file outline' size='big' />
            <Icon style={{ float: 'right', cursor: 'pointer' }} name='remove' onClick={() => onDelete(attachment.uuid)} />
            <Card.Header style={{ fontSize: 1 + 'em' }}>{attachment.filename}</Card.Header>
            {attachment.size &&
                <Card.Meta>{bytesToSize(attachment.size)}</Card.Meta>
            }
        </Card.Content>
        {extraContent &&
            <Card.Content extra>{extraContent}</Card.Content>
        }
    </Card>

const InputFile = ({ htmlFor, label, children }) =>
    <div>
        <label htmlFor={htmlFor} className="ui icon button basic">
            <Icon name='file' /> {label}
        </label>
        {children}
    </div>

const ListItem = ({ children, icon, iconColor, title }) =>
    <List.Item>
        {children}
        {icon &&
            <List.Icon name={icon} size='large' color={iconColor} />
        }
        <List.Content><Header size='small'>{title}</Header></List.Content>
    </List.Item>

const Page = ({ children, title, subtitle }) =>
    <div>
        <Header as='h1' style={{ textAlign: 'center' }}>{title && title.toUpperCase()}</Header>
        {subtitle &&
            <Header style={{ textAlign: 'center', marginTop: 0 }}><Header.Subheader>{subtitle}</Header.Subheader></Header>}
        {children}
    </div>

const InputTextControlled = ({ component: Component, onChange, value, maxLength, ...props }, { t }) =>
    <div>
        <Component value={value} onChange={(e, { id, value }) => value.length <= maxLength && onChange(id, value)} {...props} />
        <p style={{ fontStyle: 'italic' }}>{t('api-gateway:form.max_string_length', { length: maxLength, remaining: maxLength - (value ? value.length : 0) })}</p>
    </div>

InputTextControlled.contextTypes = {
    t: PropTypes.func
}

module.exports = { FormField, FormFieldInline, Field, File, InputFile, ListItem, Page, InputTextControlled }
