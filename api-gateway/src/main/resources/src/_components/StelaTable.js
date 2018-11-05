import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import PropTypes from 'prop-types'
import { Table, Input, Checkbox, Dropdown, Button, Icon } from 'semantic-ui-react'

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
        noDataMessage: PropTypes.string.isRequired,
        direction: PropTypes.string,
        column: PropTypes.string,
        fetchedSearch: PropTypes.func,
        additionalElements: PropTypes.array,
        striped: PropTypes.bool
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
        celled: true,
        direction: '',
        column: '',
        additionalElements: [],
        striped: true
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
    componentDidMount = () => {
        if (this.props.data) {
            const checkboxes = {}
            if (this.props.select) {
                this.props.data.map(data => checkboxes[data[this.props.keyProperty]] = false)
            }
            this.setState({ data: this.props.data, originalData: this.props.data, checkboxes: checkboxes })
        }
    }
    componentWillReceiveProps = (nextProps) => {
        if (nextProps.data) {
            const checkboxes = {}
            if (nextProps.select) {
                nextProps.data.map(data => checkboxes[data[nextProps.keyProperty]] = false)
            }
            this.setState({ data: nextProps.data, originalData: nextProps.data, checkboxes: checkboxes })
        }
    }
    dynamicSort = (property, direction) => {
        const sortOrder = direction === 'ASC' ? 1 : -1
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
        if (this.props.fetchedSearch) this.props.fetchedSearch(value)
        else {
            const unsearchableColumns = this.props.metaData.filter(metaData => !metaData.searchable).map(metaData => metaData.property)
            let newData = this.state.originalData
                .filter(row => Object.entries(row)
                    .filter(column => !unsearchableColumns.includes(column[0]) && column[1].toString().search(new RegExp(value, 'i')) !== -1)
                    .length > 0)
            if (this.state.column != null && this.state.direction != null)
                newData = newData.sort(this.dynamicSort(this.state.column, this.state.direction))
            this.setState({ data: newData })
        }
    }
    handleSort = clickedColumn => () => {
        if (this.props.pagination && this.props.sort) {
            this.props.sort(clickedColumn)
            return
        }
        const { column, data, direction } = this.state
        if (column !== clickedColumn) {
            this.setState({
                column: clickedColumn,
                data: data.sort(this.dynamicSort(clickedColumn, 'ASC')),
                direction: 'ASC',
            })
            return
        }
        this.setState({
            data: data.reverse(),
            direction: direction === 'ASC' ? 'DESC' : 'ASC',
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
    getDirectionClass = () => {
        const direction = this.props.direction ? this.props.direction : this.state.direction
        if (direction === 'ASC') return 'ascending'
        else if (direction === 'DESC') return 'descending'
        else return null
    }
    render() {
        const { data } = this.state
        const column = this.props.column ? this.props.column : this.state.column
        const direction = this.getDirectionClass()

        const title = this.props.headerTitle !== ''
        const header = this.props.header
        const search = this.props.search
        const isEmpty = data.length === 0
        const isFilled = data.length > 0
        const select = this.props.select
        const pagination = this.props.pagination
        const options = this.props.selectOptions.length > 0 && this.state.originalData.length > 0
        const additionalElements = this.props.additionalElements.length > 0

        const undisplayedColumnsProperties = this.props.metaData.filter(metaData => !metaData.displayed).map(metaData => metaData.property)
        const displayedColumns = this.props.metaData.filter(metaData => metaData.displayed)

        const trigger = <Button basic color='grey'>Actions <Icon style={{ marginLeft: '0.5em', marginRight: 0 }} name='caret down' /></Button>
        const selectOptions = this.props.selectOptions.map(selectOption =>
            <Dropdown.Item key={selectOption.title} onClick={() => this.handleSelectAction(selectOption.action)}>
                {Object.entries(this.state.checkboxes).filter(checkbox => checkbox[1]).length > 0 ?
                    selectOption.title :
                    (selectOption.titleNoSelection ? selectOption.titleNoSelection : selectOption.title)}
            </Dropdown.Item>
        )

        return (
            <div className={this.props.className} style={{ marginTop: '0.5em' }}>
                {search &&
                    <Input className='tableColor' style={this.floatRightStyle} onChange={this.handleSearch} icon='search' placeholder='Rechercher...' />
                }

                {options &&
                    <Dropdown direction='left' style={this.floatRightStyle} trigger={trigger} icon={false} basic>
                        <Dropdown.Menu>
                            {selectOptions}
                        </Dropdown.Menu>
                    </Dropdown>
                }

                {additionalElements &&
                    this.props.additionalElements.map((element, index) =>
                        <span key={'element-' + index} style={this.floatRightStyle}>{element}</span>
                    )
                }

                <Table selectable striped={this.props.striped} sortable={this.props.header} basic={this.props.basic} celled={this.props.celled}>
                    {title &&
                        <Table.Header>
                            <Table.Row>
                                <Table.HeaderCell colSpan={displayedColumns.length}>{this.props.headerTitle}</Table.HeaderCell>
                            </Table.Row>
                        </Table.Header>
                    }
                    {header &&
                        <Table.Header>
                            <Table.Row>
                                {this.props.metaData.map((metaData, index) =>
                                    !undisplayedColumnsProperties.includes(metaData.property) &&
                                    <Table.HeaderCell key={index + '-' + metaData.displayName}
                                        sorted={column === metaData.property ? direction : null}
                                        onClick={metaData.sortable ? this.handleSort(metaData.property) : undefined}>
                                        {metaData.displayName}
                                    </Table.HeaderCell>
                                )}
                                {select &&
                                    <Table.HeaderCell style={{ width: '40px' }} onClick={this.handleChekAll}>
                                        <Checkbox checked={this.state.checkAll} onClick={this.handleChekAll} />
                                    </Table.HeaderCell>
                                }
                            </Table.Row>
                        </Table.Header>
                    }
                    <Table.Body>
                        {isEmpty &&
                            <Table.Row>
                                <Table.Cell colSpan={displayedColumns.length}>
                                    <p style={{ fontStyle: 'italic', textAlign: 'center' }}>{this.props.noDataMessage}</p>
                                </Table.Cell>
                            </Table.Row>
                        }
                        {isFilled &&
                            data.map(row =>
                                <Table.Row key={row[this.props.keyProperty]} negative={this.props.negativeResolver ? this.props.negativeResolver(row) : false}>
                                    {displayedColumns.map((displayedColumn, index) =>
                                        <Table.Cell
                                            style={this.props.link !== '' ? { cursor: 'pointer' } : null}
                                            key={index + '-' + row[displayedColumn.property]}
                                            collapsing={!!displayedColumn.collapsing}
                                            selectable={this.props.link !== '' ? true : false}
                                            className={this.props.link !== '' ? 'no-hover' : ''}>
                                            <Link to={this.props.link !== '' ? this.props.link + row[this.props.linkProperty] : ''}>{displayedColumn.displayComponent ?
                                                displayedColumn.property === '_self' ?
                                                    displayedColumn.displayComponent(row) : displayedColumn.displayComponent(row[displayedColumn.property])
                                                : row[displayedColumn.property]}
                                            </Link>
                                        </Table.Cell>
                                    )}
                                    {select &&
                                        <Table.Cell style={{ width: '40px' }}>
                                            <Checkbox checked={this.state.checkboxes[row[this.props.keyProperty]]}
                                                onClick={() => this.handleCheckbox(row[this.props.keyProperty])} />
                                        </Table.Cell>
                                    }
                                </Table.Row>
                            )
                        }
                    </Table.Body>
                    {pagination && this.props.pagination}
                </Table>
            </div>
        )
    }
}