import React, { Fragment } from 'react'
import PropTypes from 'prop-types'
import { Form, Grid, Card, Icon, List, Header, Step, Loader, Segment, Popup } from 'semantic-ui-react'
import moment from 'moment'

import { bytesToSize } from '../_util/utils'

const FormField = ({ htmlFor, label, children, inline, helpText, required }) => (
    <Form.Field inline={inline ? true : false}>
        <label htmlFor={htmlFor}>
            {label}
            {required && <span style={{color: '#db2828'}}>*</span>}
            {helpText && <Tooltip icon="question" text={helpText} />}
        </label>
        {children}
    </Form.Field>
)

const FormFieldInline = ({ htmlFor, label, children }) => (
    <Form.Field inline>
        <Grid>
            <Grid.Column className="inline-grid" width={4}>
                <label style={{ verticalAlign: 'middle' }} htmlFor={htmlFor}>
                    {label}
                </label>
            </Grid.Column>
            <Grid.Column className="inline-grid" width={12}>
                {children}
            </Grid.Column>
        </Grid>
    </Form.Field>
)

const Field = ({ htmlFor, label, children }) => (
    <Grid>
        <Grid.Column width={4}>
            <label style={{ verticalAlign: 'middle' }} htmlFor={htmlFor}>
                {label}
            </label>
        </Grid.Column>
        <Grid.Column width={12}>{children}</Grid.Column>
    </Grid>
)

const FieldValue = ({ id, children }) => (
    <span className="fieldValue" id={id}>
        {children}
    </span>
)

const File = ({ attachment, onDelete, extraContent, src }) => (
    <Card onClick={src ? () => window.open(src, '_blank') : undefined}>
        <Card.Content>
            <Icon color="black" style={{ float: 'left' }} name="file outline" size="big" />
            {onDelete && (
                <Icon style={{ float: 'right', cursor: 'pointer' }} name="remove" onClick={() => onDelete(attachment)} />
            )}
            <Card.Header style={attachment.size
                ? { fontSize: 1 + 'em', overflow: 'hidden' }
                : { fontSize: 1 + 'em', overflow: 'hidden', marginTop: '0.5em' }
            }>
                {attachment.filename}
            </Card.Header>
            {attachment.size && <Card.Meta>{bytesToSize(attachment.size)}</Card.Meta>}
        </Card.Content>
        {extraContent && <Card.Content extra>{extraContent}</Card.Content>}
    </Card>
)

const InputFile = ({ htmlFor, label, children }) => (
    <div>
        <label htmlFor={htmlFor} className="ui icon button basic">
            <Icon name="file" /> {label}
        </label>
        {children}
    </div>
)

const ListItem = ({ children, icon, iconColor, title, ...rest }) => (
    <List.Item {...rest}>
        {children}
        {icon && <List.Icon name={icon} size="large" color={iconColor} />}
        <List.Content>
            <Header size="small">{title}</Header>
        </List.Content>
    </List.Item>
)

const StatusDisplay = ({ status, date }, { t }) => (
    <Fragment>
        <span>{status}</span><br/>
        <span style={{fontSize: '0.9em', color: 'rgba(0,0,0,.8)'}}>{moment(date).format('DD/MM/YYYY')}</span>
    </Fragment>
)
StatusDisplay.contextTypes = {
    t: PropTypes.func
}

const Page = ({ children, title, subtitle }) => (
    <div>
        <Header as="h1" style={{ textAlign: 'center' }}>
            {title && title.toUpperCase()}
        </Header>
        {subtitle &&
            <Header style={{ textAlign: 'center', marginTop: 0 }}>
                <Header.Subheader>{subtitle}</Header.Subheader>
            </Header>
        }
        {children}
    </div>
)

const InputTextControlled = ({ component: Component, onChange, value, maxLength, ...props }, { t }) =>
    <div>
        <Component value={value} onChange={(e, { id, value }) => value.length <= maxLength && onChange(id, value)} {...props} />
        <p style={{ fontStyle: 'italic' }}>
            {t('api-gateway:form.max_string_length', { length: maxLength, remaining: maxLength - (value ? value.length : 0) })}
        </p>
    </div>

InputTextControlled.contextTypes = {
    t: PropTypes.func
}

const LoadingContent = ({ children, fetchStatus }, { t }) =>
    fetchStatus && (
        <Fragment>
            {fetchStatus === 'fetched' && children}
            {fetchStatus === 'loading' && (
                <Segment>
                    <Loader active inline="centered">
                        {t('api-gateway:loading')}
                    </Loader>
                </Segment>
            )}
            {fetchStatus !== 'fetched' &&
        fetchStatus !== 'loading' && <p>{t(fetchStatus)}</p>}
        </Fragment>
    )

LoadingContent.contextTypes = {
    t: PropTypes.func
}

const LinkFile = ({ url, text, ariaLabel = 'Lien fichier' }) => (
    <Fragment>
        <a target="_blank" href={url} aria-label={ariaLabel}>
            {text}
        </a>
        <a target="_blank" href={url + '?disposition=attachment'} aria-label={ariaLabel}>
            <Icon style={{ marginLeft: '0.5em' }} name="download" />
        </a>
    </Fragment>
)

/* Home made tooltip, to replace with React SemanticUI Popup once fixed: https://github.com/Semantic-Org/Semantic-UI-React/issues/1126 */
const Tooltip = ({ icon, text }) => (
    <i className={`tooltip ${icon} small circular icon`} style={{ marginLeft: '0.5em' }}>
        <span className="tooltiptext">{text}</span>
    </i>
)

const MigrationSteps = ({ icon, title, description, status, onClick, reset, disabled = false }, { t }) => (
    <Step.Group fluid>
        <Step disabled={disabled} style={{ width: '50%', justifyContent: 'flex-start' }}>
            {icon || <Icon name="tag" />}
            <Step.Content>
                <Step.Title>{title}</Step.Title>
                {description && <Step.Description>{description}</Step.Description>}
            </Step.Content>
        </Step>
        {status === 'NOT_DONE' && (
            <Step disabled={disabled} link onClick={onClick} style={{ backgroundColor: '#21ba45', color: 'white' }}>
                <Icon name="download" />
                <Step.Content>
                    <Step.Title>{t('api-gateway:migration.start')}</Step.Title>
                </Step.Content>
            </Step>
        )}
        {status === 'ONGOING' && (
            <Step disabled={disabled} active>
                <Icon loading name="refresh" />
                <Step.Content>
                    <Step.Title>{t('api-gateway:migration.ongoing')}</Step.Title>
                </Step.Content>
            </Step>
        )}
        {status === 'DONE' && (
            <Step disabled={disabled} style={{ color: '#21ba45', background: '#f3f4f5' }}>
                <Icon name="check" />
                <Step.Content>
                    <Step.Title style={{ color: '#21ba45' }}>
                        {t('api-gateway:migration.finished')}
                    </Step.Title>
                </Step.Content>
            </Step>
        )}
        <Step style={{ paddingLeft: 0, paddingRight: 0 }} link onClick={() => reset()}>
            <Icon name="repeat" style={{ margin: 0 }} />
        </Step>
    </Step.Group>
)

MigrationSteps.contextTypes = {
    t: PropTypes.func
}

const ValidationPopup = ({ children, errorList }) =>
    <Popup className='validation-popup' trigger={<span>{children}</span>} position='top right' verticalOffset={10}
        content={<ErrorListPopup errorList={errorList} />}
    />

const ErrorListPopup = ({ errorList }) =>
    <ul>
        {errorList.map(error =>
            <li>{error}</li>
        )}
    </ul>


const PesErrorList = ( errors, prefix = '' ) =>
    errors.map((error, index) =>
        <div key={`${prefix}-${index}`}>{error.title && `${error.title} : `}{error.message}{error.source && ` (${error.source})`}</div>
    )

export {
    FormField,
    FormFieldInline,
    Field,
    FieldValue,
    File,
    InputFile,
    ListItem,
    Page,
    InputTextControlled,
    MigrationSteps,
    LoadingContent,
    LinkFile,
    Tooltip,
    StatusDisplay,
    PesErrorList,
    ValidationPopup
}
