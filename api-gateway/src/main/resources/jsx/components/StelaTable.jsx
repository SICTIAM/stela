import React, { Component } from 'react'
import PropTypes from 'prop-types'
import history from '../util/history'
import { Table, Input } from 'semantic-ui-react'
import renderIf from 'render-if'

export default class StelaTable extends Component {
    static propTypes = {
        className: PropTypes.string,
        data: PropTypes.array.isRequired,
        header: PropTypes.bool,
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
        headerTitle: '',
        link: '',
        metaData: []
    }
    state = {
        column: null,
        data: this.props.data,
        direction: null,
        originalData: this.props.data
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
    render() {
        const { column, data, originalData, direction } = this.state

        const title = renderIf(this.props.headerTitle !== '')
        const header = renderIf(this.props.header)
        const isEmpty = renderIf(data.length === 0)
        const isFilled = renderIf(data.length > 0)

        const undisplayedColumns = this.props.metaData.filter(metaData => !metaData.displayed).map(metaData => metaData.property)
        const displayedComlumnsNbr = Object.keys(originalData[0]).length - undisplayedColumns.length

        const Styles = {
            selectableRow: {
                cursor: 'pointer'
            },
            floatRight: {
                float: 'right',
                marginBottom: '1em'
            }
        }

        return (
            <div className={this.props.className}>
                <Input style={Styles.floatRight} onChange={this.handleSearch} icon='search' placeholder='Rechercher...' />
                {isEmpty(
                    <p>{this.props.noDataMessage}</p>
                )}
                {isFilled(
                    <Table sortable={this.props.header} celled fixed>
                        {title(
                            <Table.Header>
                                <Table.Row>
                                    <Table.HeaderCell colSpan={displayedComlumnsNbr}>{this.props.headerTitle}</Table.HeaderCell>
                                </Table.Row>
                            </Table.Header>
                        )}
                        {header(
                            <Table.Header>
                                <Table.Row>
                                    {this.props.metaData.map(metaData =>
                                        renderIf(!undisplayedColumns.includes(metaData.property))(
                                            <Table.HeaderCell key={metaData.displayName} sorted={column === metaData.property ? direction : null} onClick={this.handleSort(metaData.property)}>
                                                {metaData.displayName}
                                            </Table.HeaderCell>
                                        )
                                    )}
                                </Table.Row>
                            </Table.Header>
                        )}
                        <Table.Body>
                            {data.map(row =>
                                <Table.Row style={this.props.link !== '' && Styles.selectableRow} key={row[this.props.keyProperty]} onClick={() => this.handleLink(row[this.props.linkProperty])}>
                                    {Object.entries(row).map(column =>
                                        renderIf(!undisplayedColumns.includes(column[0]))(
                                            <Table.Cell key={column[1]}>{column[1]}</Table.Cell>
                                        )
                                    )}
                                </Table.Row>
                            )}
                        </Table.Body>
                    </Table>
                )}
            </div>
        )
    }
}