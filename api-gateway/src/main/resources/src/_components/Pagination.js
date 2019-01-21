import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Table, Icon, Dropdown } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import ReactPaginate from 'react-paginate'

class Pagination extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    static propTypes = {
        columns: PropTypes.number.isRequired,
        pageCount: PropTypes.number.isRequired,
        itemPerPage: PropTypes.number.isRequired,
        updateItemPerPage: PropTypes.func.isRequired,
        handlePageClick: PropTypes.func.isRequired,
        options: PropTypes.array
    }
    static defaultProps = {
        options: [
            { key: 25, text: 25, value: 25 },
            { key: 50, text: 50, value: 50 },
            { key: 100, text: 100, value: 100 }
        ]
    }
    state = {
        localItemPerPage: this.props.itemPerPage
    }
    //update props item per page + refresh table
    //call whe user click on number or on "enter"
    updateItemPerPage = (itemPerPage) => {
        if(itemPerPage !== this.props.itemPerPage) {
            localStorage.setItem('itemPerPage', itemPerPage)
            this.props.updateItemPerPage(itemPerPage)
        }
    }
    //update only local item per page, don't refresh table
    //use for accessibiliy, whe user use keyboard to change item per page, don't refresh automatically table
    updateLocalItemPerPage = (itemPerPage) => {
        this.setState({localItemPerPage: itemPerPage})
    }
    render() {
        const { t } = this.context
        const { columns, pageCount, handlePageClick, currentPage, options } = this.props

        return (
            <Table.Footer>
                <Table.Row>
                    <Table.Cell style={{ overflow: 'visible' }} colSpan={columns}>
                        <Dropdown aria-label={t('api-gateway:list.item_per_page_label')} compact selection options={options} value={this.state.localItemPerPage} onChange={(e, { value }) => this.updateLocalItemPerPage(value)} onBlur={(e, { value }) => this.updateItemPerPage(value)} />
                        <span style={{ marginLeft: '1em' }}>{t('list.item_per_page')}</span>
                        <ReactPaginate previousLabel={<Icon name='left chevron' aria-label={t('api-gateway:list.previous')}/>}
                            nextLabel={<Icon name='right chevron' aria-label={t('api-gateway:list.next')}/>}
                            breakLabel={<span className='item'>...</span>}
                            pageCount={pageCount}
                            marginPagesDisplayed={2}
                            pageRangeDisplayed={5}
                            onPageChange={handlePageClick}
                            containerClassName={'ui pagination right floated menu'}
                            previousLinkClassName={'icon item'}
                            nextLinkClassName={'icon item'}
                            pageLinkClassName={'item'}
                            activeClassName={'active'}
                            forcePage={currentPage}/>
                    </Table.Cell>
                </Table.Row>
            </Table.Footer>
        )
    }
}

export default translate(['api-gateway'])(Pagination)