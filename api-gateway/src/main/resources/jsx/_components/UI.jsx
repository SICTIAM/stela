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
        <Grid.Column width={12}>{children}</Grid.Column>
    </Grid>

const File = ({ attachment, onDelete, extraContent, src }) =>
    <Card onClick={src ? () => window.open(src, '_blank') : undefined}>
        <Card.Content>
            <Icon color='black' style={{ float: 'left' }} name='file outline' size='big' />
            {onDelete &&
                <Icon style={{ float: 'right', cursor: 'pointer' }} name='remove' onClick={() => onDelete(attachment)} />
            }
            <Card.Header style={attachment.size ? { fontSize: 1 + 'em', overflow: 'hidden' } : { fontSize: 1 + 'em', overflow: 'hidden', marginTop: '0.5em' }}>
                {attachment.filename}
            </Card.Header>
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

const ListItem = ({ children, icon, iconColor, title, ...rest }) =>
    <List.Item {...rest}>
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
