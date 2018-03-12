import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Row, Col, Button, ButtonGroup } from 'react-bootstrap'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import ContainerDimensions from 'react-container-dimensions'
import Select from 'react-select'
import Slider from 'rc-slider'
import Moment from 'moment'
import { extendMoment } from 'moment-range'
import dateFormat from 'dateformat'
import { v4 } from 'uuid'
import * as d3 from 'd3'
import _ from 'lodash'

@connect(state => ({
    data: state.pq.graphdata.items,
    columns: state.pq.columns.items,
    column_colors: state.pq.colors.items,
    column_filters: state.filter.column,
    date_filters: state.filter.date,
    current_language: state.locale.current_language.translation
}))
export const TrendingChart = class TrendingChart extends Component {
    constructor(props) {
        super(props)
        this.valueField = props.valueField !== undefined ? props.valueField : "question_count"; // property containing value, defaults to question_count
        this.globalTotal = _.sumBy(this.props.data, this.valueField);
        this.threshold = props.threshold !== undefined ? props.threshold : 0.01; // threshold for values to omit
        this.state = {
            activeColumn: this.props.columns[0]
        }
    }

    /**
     * Writes the new column filter to the redux store
     * @param {string} column column to filter
     * @param {string[]} values list of values to filter on
     */
    writeFilterToRedux(column, values) {
        this.props.dispatch({ type: 'SET_COLUMN_FILTER', filter: { key: column, values: values.map(d => d.value) } })
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
    filterTop(data) {
        // if active column contains the filter don't filter the data
        if (this.props.column_filters[this.state.activeColumn]) return data
        const getValues = (column) => {
            const nested = d3.nest()
                .key(d => d[column])
                .rollup(d => d3.sum(d, d => d[this.valueField]))
                .entries(data);
            const total = d3.sum(nested, d => d.value);
            return _.orderBy(nested, d => d.value, 'desc').map(d => d.key).slice(0, 10)
        }
        const allowed_values = getValues(this.state.activeColumn);
        return _.filter(data, d => _.includes(allowed_values, d[this.state.activeColumn]))
    }

    columnButtons() {
        const buttons = this.props.columns.map(column =>
            <ButtonGroup key={v4()}>
                <Button
                    className="TrendingButton"
                    key={v4()}
                    active={column === this.state.activeColumn}
                    onClick={() => this.setState({ activeColumn: column })}>
                    {this.props.current_language.columns[column]}
                </Button>
            </ButtonGroup>
        );
        return <Row>
            <Col xs={12}>
                <ButtonGroup justified>
                    {buttons}
                </ButtonGroup>
            </Col>
        </Row>
    }

    getLineChart(data) {
        const unique_values = _.uniq(data.map(d => d[this.state.activeColumn]))
        const getDate = str => {
            const date = new Date(str.slice(0, 4), str.slice(5, 7));
            return this.props.current_language.months[date.getMonth()] + " " + date.getFullYear();
        }
        const aggregated_data = d3.nest()
            .key(d => d.date.slice(0, -3))
            .rollup(d => {
                const inner_aggregated_data = d3.nest()
                    .key(d => d[this.state.activeColumn])
                    .rollup(d => d3.sum(d, e => e[this.valueField]))
                    .entries(d);
                const inner_aggregated_values = inner_aggregated_data.map(d => d.key);
                const zero_values = _.filter(
                    unique_values,
                    uv => !_.includes(inner_aggregated_values, uv)
                ).map(uv => ({ key: uv, value: 0 }));
                return inner_aggregated_data.concat(zero_values).map(val => ({ [val.key]: val.value }));
            })
            .entries(data);
        const aggregated_data_sorted = _.sortBy(aggregated_data, "key")
            .map(d => Object.assign({ name: getDate(d.key) }, ...d.value))
        return (
            <ContainerDimensions >
                {({ width }) =>
                    <LineChart data={aggregated_data_sorted} height={600} width={width}>
                        <XAxis dataKey="name" />
                        <YAxis />
                        <CartesianGrid strokeDasharray="3 3" />
                        <Legend
                            onClick={(item) => console.log(item)}
                        />
                        <Tooltip />
                        {
                            unique_values.map(uv => <Line
                                key={v4()}
                                type="monotone"
                                dataKey={uv}
                                stroke={this.props.column_colors[this.state.activeColumn][uv]}
                                onClick={(item) => console.log(item)}
                            />)
                        }
                    </LineChart>}
            </ContainerDimensions>
        );
    }

    render() {
        const filtered_data = this.filterData(_.cloneDeep(this.props.data))
        const filtered_top_data = this.filterTop(filtered_data)
        return (
            <div >
                {this.columnButtons()}<br />
                {this.getLineChart(filtered_top_data)}
            </div>
        );
    }
}