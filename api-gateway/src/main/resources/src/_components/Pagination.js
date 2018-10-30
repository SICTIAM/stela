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
        handlePageClick: PropTypes.func.isRequired
    }
    updateItemPerPage = (itemPerPage) => {
        localStorage.setItem('itemPerPage', itemPerPage)
        this.props.updateItemPerPage(itemPerPage)
    }
    render() {
        const { t } = this.context
        const { columns, pageCount, handlePageClick, itemPerPage, currentPage } = this.props
        const options = [
            { key: 25, text: 25, value: 25 },
            { key: 50, text: 50, value: 50 },
            { key: 100, text: 100, value: 100 }
        ]
        return (
            <Table.Footer>
                <Table.Row>
                    <Table.HeaderCell style={{ overflow: 'visible' }} colSpan={columns}>
                        <Dropdown compact selection options={options} value={itemPerPage} onChange={(e, { value }) => this.updateItemPerPage(value)} />
                        <span style={{ marginLeft: '1em' }}>{t('list.item_per_page')}</span>
                        <ReactPaginate previousLabel={<Icon name='left chevron' />}
                            nextLabel={<Icon name='right chevron' />}
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
                            forcePage={currentPage} />
                    </Table.HeaderCell>
                </Table.Row>
            </Table.Footer>
        )
    }
}

export default translate(['api-gateway'])(Pagination)