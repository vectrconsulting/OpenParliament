import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Row, Col } from 'react-bootstrap'
import Select from 'react-select'
import _ from 'lodash'

@connect(state => ({
    data: state.pq.data.items,
    columns: state.pq.columns.items,
    column_filters: state.filter.column,
    current_language: state.locale.current_language.translation
}))
export const ColumnFilters = class ColumnFilters extends Component {
    /**
     * Writes the new column filter to the redux store
     * @param {string} column column to filter
     * @param {string[]} values list of values to filter on
     */
    writeFilterToRedux(column, values) {
        this.props.dispatch({ type: 'SET_COLUMN_FILTER', filter: { key: column, values: values.map(d => d.value) } })
    }
    /**
     * Returns a row of multiselect dropdowns for the filter row
     */
    getFilters() {
        return this.props.columns.map((column) => {
            const values = _.uniq(this.props.data.map(d => d[column]))
                .map(d => ({ value: d, label: d }))
            return <Col key={column + "filtercolumn"}
                id="column_filter"
                sm={Math.floor(12 / this.props.columns.length)}>
                <Select
                    placeholder={this.props.current_language.columns[column]}
                    key={column + "filter"}
                    multi={true}
                    options={values}
                    onChange={val => this.writeFilterToRedux(column, val)}
                    disabled={false}
                    value={
                        _.find(this.props.column_filters, d => d.key === column) !== undefined ?
                            _.find(this.props.column_filters, d => d.key === column).values.map(d => ({ value: d, label: d })) :
                            []}
                />
            </Col>
        })
    }
    render() {
        return <Row id="column_filter">
            {this.getFilters()}
        </Row>
    }
}