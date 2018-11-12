import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Header, Accordion, Input, Icon } from 'semantic-ui-react'
import { translate } from 'react-i18next'


class AdvancedSearch extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static propTypes = {
        isDefaultOpen: PropTypes.bool
    }
    static defaultProps = {
        isDefaultOpen: true
    }
    state = {
        isOpen: this.props.isDefaultOpen
    }
    toggle = () => this.setState({ isOpen: !this.state.isOpen })
    handleKeyPressInput = (event) => {
        if (event.key === 'Enter') this.props.onSubmit()
    }
    render() {
        const { t } = this.context
        const { isOpen } = this.state
        const { children, fieldId, fieldValue, fieldOnChange } = this.props
        return (
            <div style={{ display: 'flex', justifyContent: 'center' }}>
                <Accordion style={{ marginBottom: '1em' }} styled>
                    <Accordion.Title active={isOpen} style={{ cursor: 'default' }}>
                        <Input fluid
                            aria-label={t('api-gateway:form.search')}
                            id={fieldId}
                            value={fieldValue}
                            style={{borderRight: 'none'}}
                            onKeyPress={this.handleKeyPress}
                            onChange={(e, { id, value }) => fieldOnChange(id, value)}
                            icon={<button aria-label={t('api-gateway:form.open_advanced_search')} onClick={this.toggle} style={{backgroundColor: '#fff', border: '1px solid rgba(34,36,38,.15)', borderLeft: 'none'}}>
                                <Icon name={'caret ' + (isOpen ? 'down' : 'left')}/>
                            </button>}
                            placeholder={`${t('api-gateway:form.search')}...`}
                        />
                    </Accordion.Title>
                    <Accordion.Content active={isOpen}>
                        <Header size='small'>{t('api-gateway:form.advanced_search')}</Header>
                        {children}
                    </Accordion.Content>
                </Accordion>
            </div>
        )
    }
}

export default translate(['api-gateway'])(AdvancedSearch)