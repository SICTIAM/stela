import React, { Component } from 'react'
import PropTypes from 'prop-types'
import renderIf from 'render-if'
import { Table, Input, Checkbox, Dropdown } from 'semantic-ui-react'

import history from '../_util/history'

export default class StelaTable extends Component {
    static propTypes = {
        className: PropTypes.string,
        data: PropTypes.array,
        header: PropTypes.bool,
        search: PropTypes.bool,
        select: PropTypes.bool,
        selectOptions: PropTypes.array,
        headerTitle: PropTypes.string,
        keyProperty: PropTypes.string.isRequired,
        link: PropTypes.string,
        linkProperty: PropTypes.string,
        metaData: PropTypes.array,
        noDataMessage: PropTypes.string.isRequired
    }
    static defaultProps = {
        className: '',
        header: false,
        search: true,
        select: false,
        selectOptions: [],
        headerTitle: '',
        link: '',
        linkProperty: '',
        metaData: [],
        celled: true
    }
    state = {
        column: null,
        data: [],
        direction: null,
        originalData: [],
        checkboxes: {},
        checkAll: false
    }
    floatRightStyle = {
        float: 'right',
        marginBottom: '1em',
        marginLeft: '1em'
    }
    componentWillReceiveProps = (nextProps) => {
        if (nextProps.data && !this.state.dataReceived) {
            const checkboxes = {}
            if (nextProps.select) {
                nextProps.data.map(data => checkboxes[data[nextProps.keyProperty]] = false)
            }
            this.setState({ data: nextProps.data, originalData: nextProps.data, checkboxes: checkboxes })
        }
    }
    dynamicSort = (property, direction) => {
        const sortOrder = direction === 'ascending' ? 1 : -1
        return (a, b) => {
            const result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0
            return result * sortOrder
        }
    }
    handleLink = linkProperty => {
        if (this.props.link !== '') {
            history.push(this.props.link + linkProperty)
        }
    }
    handleSearch = (e, { value }) => {
        const unsearchableColumns = this.props.metaData.filter(metaData => !metaData.searchable).map(metaData => metaData.property)
        let newData = this.state.originalData
            .filter(row => Object.entries(row)
                .filter(column => !unsearchableColumns.includes(column[0]) && column[1].toString().search(new RegExp(value, "i")) !== -1)
                .length > 0)
        if (this.state.column != null && this.state.direction != null)
            newData = newData.sort(this.dynamicSort(this.state.column, this.state.direction))
        this.setState({ data: newData })
    }
    handleSort = clickedColumn => () => {
        const { column, data, direction } = this.state
        if (column !== clickedColumn) {
            this.setState({
                column: clickedColumn,
                data: data.sort(this.dynamicSort(clickedColumn, 'ascending')),
                direction: 'ascending',
            })
            return
        }
        this.setState({
            data: data.reverse(),
            direction: direction === 'ascending' ? 'descending' : 'ascending',
        })
    }
    handleCheckbox = (keyProperty) => {
        const checkboxes = this.state.checkboxes
        checkboxes[keyProperty] = !checkboxes[keyProperty]
        this.setState({ checkboxes })
    }
    handleChekAll = () => {
        const checkAll = !this.state.checkAll
        const checkboxes = this.state.checkboxes
        Object.keys(checkboxes).forEach(keyProperty => checkboxes[keyProperty] = checkAll)
        this.setState({ checkboxes: checkboxes, checkAll: checkAll })
    }
    handleSelectAction = (action) => {
        const selectedUuids = Object.entries(this.state.checkboxes)
            .filter(checkbox => checkbox[1])
            .map(checkbox => checkbox[0])
        action(selectedUuids)
    }
    render() {
        const { column, data, direction } = this.state

        const title = renderIf(this.props.headerTitle !== '')
        const header = renderIf(this.props.header)
        const search = renderIf(this.props.search)
        const isEmpty = renderIf(data.length === 0)
        const isFilled = renderIf(data.length > 0)
        const select = renderIf(this.props.select)
        const options = renderIf(this.props.selectOptions.length > 0 && this.state.originalData.length > 0)

        const undisplayedColumnsProperties = this.props.metaData.filter(metaData => !metaData.displayed).map(metaData => metaData.property)
        const displayedColumns = this.props.metaData.filter(metaData => metaData.displayed)

        const selectOptions = this.props.selectOptions.map(selectOption =>
            <Dropdown.Item key={selectOption.title} onClick={() => this.handleSelectAction(selectOption.action)}>
                {Object.entries(this.state.checkboxes).filter(checkbox => checkbox[1]).length > 0 ?
                    selectOption.title :
                    (selectOption.titleNoSelection ? selectOption.titleNoSelection : selectOption.title)}
            </Dropdown.Item>
        )

        return (
            <div className={this.props.className}>
                {search(
                    <Input style={this.floatRightStyle} onChange={this.handleSearch} icon='search' placeholder='Rechercher...' />
                )}

                {options(
                    <Dropdown style={this.floatRightStyle} text='Actions' button>
                        <Dropdown.Menu>
                            {selectOptions}
                        </Dropdown.Menu>
                    </Dropdown>
                )}

                <Table sortable={this.props.header} basic={this.props.basic} celled={this.props.celled} fixed>
                    {title(
                        <Table.Header>
                            <Table.Row>
                                <Table.HeaderCell colSpan={displayedColumns.length}>{this.props.headerTitle}</Table.HeaderCell>
                            </Table.Row>
                        </Table.Header>
                    )}
                    {header(
                        <Table.Header>
                            <Table.Row>
                                {select(
                                    <Table.HeaderCell style={{ width: '40px' }} onClick={this.handleChekAll}>
                                        <Checkbox checked={this.state.checkAll} onClick={this.handleChekAll} />
                                    </Table.HeaderCell>
                                )}
                                {this.props.metaData.map((metaData, index) =>
                                    renderIf(!undisplayedColumnsProperties.includes(metaData.property))(
                                        <Table.HeaderCell key={index + '-' + metaData.displayName}
                                            sorted={column === metaData.property ? direction : null}
                                            onClick={this.handleSort(metaData.property)}>
                                            {metaData.displayName}
                                        </Table.HeaderCell>
                                    )
                                )}
                            </Table.Row>
                        </Table.Header>
                    )}
                    <Table.Body>
                        {isEmpty(
                            <Table.Row>
                                <Table.Cell colSpan={displayedColumns.length}>
                                    <p style={{ fontStyle: 'italic', textAlign: 'center' }}>{this.props.noDataMessage}</p>
                                </Table.Cell>
                            </Table.Row>
                        )}
                        {isFilled(
                            data.map(row =>
                                <Table.Row key={row[this.props.keyProperty]}>
                                    {select(
                                        <Table.Cell style={{ width: '40px' }}>
                                            <Checkbox checked={this.state.checkboxes[row[this.props.keyProperty]]}
                                                onClick={() => this.handleCheckbox(row[this.props.keyProperty])} />
                                        </Table.Cell>
                                    )}
                                    {displayedColumns.map((displayedColumn, index) =>
                                        <Table.Cell onClick={() => this.handleLink(row[this.props.linkProperty])}
                                            style={this.props.link !== '' ? { cursor: 'pointer' } : null}
                                            key={index + '-' + row[displayedColumn.property]}>
                                            {displayedColumn.displayComponent ?
                                                displayedColumn.displayComponent(row[displayedColumn.property]) : row[displayedColumn.property]}
                                        </Table.Cell>
                                    )}
                                </Table.Row>
                            )
                        )}
                    </Table.Body>
                </Table>
            </div>
        )
    }
}