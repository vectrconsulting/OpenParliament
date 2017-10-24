import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Row, Col } from 'react-bootstrap'
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from 'recharts'
import Select from 'react-select'
import Slider from 'rc-slider'
import Moment from 'moment'
import { extendMoment } from 'moment-range'
import dateFormat from 'dateformat'
import { v4 } from 'uuid'
import * as d3 from 'd3'
import _ from 'lodash'


@connect(state => ({
    data: state.pq.data.items,
    columns: state.pq.columns.items,
    column_colors: state.pq.colors.items,
    column_filters: state.filter.column,
    date_filters: state.filter.date,
    current_language: state.locale.current_language.translation
}))
export const PieChartWrapper = class PieChartWrapper extends Component {
    constructor(props) {
        super(props)
        this.valueField = props.valueField !== undefined ? props.valueField : "question_count"; // property containing value, defaults to question_count
        this.globalTotal = _.sumBy(this.props.data, this.valueField);
        this.threshold = props.threshold !== undefined ? props.threshold : 0.01; // threshold for values to omit
    }

    /**
     * Filters the data based on the column and date filters
     */
    filterData(data) {
        const filter_row = (row) => {
            const start_date = this.props.date_filters[0]
            const end_date = this.props.date_filters[1]
            if (start_date > row.date || row.date > end_date) return false
            return _.filter(this.props.column_filters, filter => _.includes(this.props.columns, filter.key))
                .map(filter => (filter.values.length && !_.includes(filter.values, row[filter.key])) ? false : true)
                .reduce((l, r) => l && r, true);

        }
        return _.filter(data, d => filter_row(d))
    }

    /**
     * Filters out values per column that do not have high enough values
     * This is done for each filtered set so entries can change when filters are applied
     */
    filterLowOccurences(data) {
        const getValues = (column) => {
            const nested = d3.nest()
                .key(d => d[column])
                .rollup(d => d3.sum(d, d => d[this.valueField]))
                .entries(data);
            const total = d3.sum(nested, d => d.value);
            return _.filter(nested, d => d.value / total < this.threshold).map(d => d.key)
        }
        const not_allowed_values = Object.assign({}, ...this.props.columns.map(column => ({ [column]: getValues(column) })));
        const row_check = row => {
            if (this.props.columns.map(column => !_.includes(not_allowed_values[column], row[column])).reduce((a, b) => a && b, true)) return row
            else return Object.assign(
                row,
                ...this.props.columns.map(column => ({
                    [column]: !_.includes(not_allowed_values[column], row[column]) ? row[column] : this.props.current_language.other
                }))
            )
        }
        return data.map(row_check)
    }

    /**
     * Writes the new column filter to the redux store
     * @param {string} column column to filter
     * @param {string[]} values list of values to filter on
     */
    writeFilterToRedux(column, values) {
        this.props.dispatch({ type: 'SET_COLUMN_FILTER', filter: { key: column, values: values.map(d => d.value) } })
    }

    getPies(data) {
        const getValues = column => {
            return d3.nest()
                .key(d => d[column])
                .rollup(d => d3.sum(d, e => e[this.valueField]))
                .entries(data);
        }
        return this.props.columns.map(column => {
            const values = _.orderBy(
                getValues(column),
                row => row.key != this.props.current_language.other ? row.value : -1,
                'asc'
            );
            const getColor = value => this.props.column_colors[column][value] !== undefined ? this.props.column_colors[column][value] : value !== this.props.current_language.other ? this.props.column_colors[column].default : "#989898";
            const data = values.map(value => ({
                name: value.key,
                value: value.value,
                color: getColor(value.key)
            }))
            const onclick = value => this.writeFilterToRedux(column, value);
            return (
                <Col key={v4()} sm={6}>
                    <center><h5>{this.props.current_language.columns[column]}</h5></center>
                    <InnerPieWrapper
                        key={v4()}
                        data={data}
                        onClick={onclick.bind(this)}
                        other_keyword={this.props.current_language.other}
                    />
                </Col>
            );
        });

    }

    render() {
        const filtered_data = this.filterData(_.cloneDeep(this.props.data))
        const filtered_low_occurences_fixed = this.filterLowOccurences(filtered_data)
        const pies = this.getPies(filtered_low_occurences_fixed)
        const pies_paired = pies
            .slice(0, -1)
            .map((element, index) => [element, pies[index + 1]])
            .filter((element, index) => index % 2 === 0)
            .map(pair => <Row key={v4()}>{pair}</Row>)
        return (
            <div>
                {pies_paired}
            </div>
        );
    }
}

class InnerPieWrapper extends Component {
    constructor(props) {
        super(props)
    }
    filter(value) {
        if (value !== this.props.other_keyword) {
            this.props.onClick([{ value: value }]);
        }
    }
    render() {

        return (
            <ResponsiveContainer width="100%" height={400}>
                <PieChart >
                    <Pie
                        data={this.props.data}
                        dataKey="value"
                        nameKey="name"
                        startAngle={90}
                        endAngle={450}
                        onClick={(item) => this.filter(item.name)}
                    >
                        {this.props.data.map(entry => <Cell key={v4()} fill={entry.color} />)}
                    </Pie>
                    <Tooltip />
                </PieChart>
            </ResponsiveContainer>
        );
    }
}