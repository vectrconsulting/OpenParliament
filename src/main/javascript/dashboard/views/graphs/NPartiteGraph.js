import React, { Component } from 'react'
import { Row, Col } from 'react-bootstrap'
import Select from 'react-select'
import ContainerDimensions from 'react-container-dimensions'
import { connect } from 'react-redux'
import _ from 'lodash'
import * as d3 from 'd3'
import Slider from 'rc-slider'
import Moment from 'moment'
import { extendMoment } from 'moment-range'
import dateFormat from 'dateformat'

import PropTypes from 'prop-types'
import { v4 } from 'uuid'

@connect(state => ({
    data: state.pq.data.items,
    dataloading: state.pq.data.loading,
    columns: state.pq.columns.items,
    column_colors: state.pq.colors.items,
    column_filters: state.filter.column,
    date_filters: state.filter.date,
    current_language: state.locale.current_language.translation
}))
export const NPartiteGraph = class NPartitGraph extends Component {
    constructor(props) {
        super(props);

        this.height = this.props.height; // height of drawing in pixels
        this.threshold = props.threshold !== undefined ? props.threshold : 0.01; // threshold for values to omit
        this.columnWidth = props.columnWidth !== undefined ? props.columnWidth : 2; // relative column size
        this.columnSpacing = props.columnSpacing !== undefined ? props.columnSpacing : 1; // relative columnspacing (edges)
        this.padding = props.padding !== undefined ? props.padding : 5; // padding in pixels
        this.valueField = props.valueField !== undefined ? props.valueField : "count"; // property containing value, defaults to question_count
        this.minimum_date = _.minBy(this.props.data, 'date').date
        this.maximum_date = _.maxBy(this.props.data, 'date').date
        this.globalTotal = _.sumBy(this.props.data, this.valueField);
    }

    /**
     * Aggregates the data into colored paths
     * Takes all paths and combines them into the heaviest possible path with the same party, author, topic and department
     */
    aggregateData(data) {
        // check if first column has colors in store
        const colorMap = this.props.column_colors[this.props.columns[0]];
        const getColor = value => colorMap[value] !== undefined ? colorMap[value] : value !== this.props.current_language.other ? colorMap.default : "#989898";

        const aggregated_data = d3.nest()
            .key(d => this.props.columns.map(column => d[column]).join('-'))
            .rollup(d => ({
                ...d[0],
                question_count: d3.sum(d, d => d[this.valueField]),
                color: getColor(d[0][this.props.columns[0]])
            }))
            .entries(data)
            .map(d => d.value);

        const unique_column_values_weight = Object.assign({}, ...this.props.columns.map(column => ({
            [column]: Object.assign({}, ..._.orderBy(_.uniq(aggregated_data.map(d => d[column]))
                .map(d => ({ value: d, weight: _.sumBy(_.filter(aggregated_data, e => e[column] === d), this.valueField) })), ['weight'], ['desc'])
                .map(d => ({ [d.value]: d.weight })))
        })));

        return _.orderBy(aggregated_data,
            this.props.columns.map(column => d => d[column] !== this.props.current_language.other ? unique_column_values_weight[column][d[column]] : -1),
            this.props.columns.map(column => 'desc'));
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
     * Writes the new column filter to the redux store
     * @param {string} column column to filter
     * @param {string[]} values list of values to filter on
     */
    writeFilterToRedux(column, values) {
        this.props.dispatch({ type: 'SET_COLUMN_FILTER', filter: { key: column, values: values.map(d => d.value) } })
    }

    /**
     * Returns all the parts of the NPartite graph: columns and edges
     * @param {number} width width of the current view, passed down by ContainerDimensions
     */
    getColumns(width) {
        const scaleX = width / (this.props.columns.length * (this.columnWidth + this.columnSpacing) - this.columnSpacing)

        const offsetX = (index) => (index * (this.columnWidth + this.columnSpacing)) * scaleX;

        return this.props.columns.map((column, index, columns) => {
            const onClickFilter = (value) => {
                this.writeFilterToRedux(column, value);
            }

            return <PartiteColumn
                key={index}
                width={this.columnWidth * scaleX}
                height={this.height}
                offsetX={offsetX(index)}
                padding={this.padding}
                data={this.colored_paths}
                columns={columns}
                columnIndex={index}
                onClickFilter={onClickFilter}
                valueField={this.valueField}
                globalTotal={this.globalTotal}
                other_keyword={this.props.current_language.other}
            />
        })
    }

    getEdges(width) {
        const scaleX = width / (this.props.columns.length * (this.columnWidth + this.columnSpacing) - this.columnSpacing)
        const offsetX = (index) => (index * (this.columnWidth + this.columnSpacing) + this.columnWidth) * scaleX

        const columnpairs = this.props.columns.slice(0, -1).map((value, index) => ({ left: value, right: this.props.columns[index + 1] }))

        return columnpairs.map((columnpair, index) =>
            <PartiteEdge
                key={v4()}
                width={this.columnSpacing * scaleX}
                height={this.height}
                offsetX={offsetX(index)}
                data={this.colored_paths}
                columnpair={columnpair}
                columns={this.props.columns}
                valueField={this.valueField}
                padding={this.padding}
                other_keyword={this.props.current_language.other}
            />
        );
    }

    render() {
        if (this.props.dataloading) return <div align="center">Loading...</div>;
        if (this.props.columns.length === 0) return <div />;
        const filtered = this.filterData(_.cloneDeep(this.props.data));
        const filtered_low_occurences_fixed = this.filterLowOccurences(filtered);
        this.colored_paths = this.aggregateData(filtered_low_occurences_fixed);
        return (
            <div>
                <ContainerDimensions >
                    {({ width }) =>
                        <svg width={width} height={this.height} id="npartite">
                            {this.getColumns(width)}
                            {this.getEdges(width)}
                        </svg>}
                </ContainerDimensions>
            </div>
        );
    }
}

class PartiteColumn extends Component {
    constructor(props) {
        super(props)
        this.padding = props.padding !== undefined ? props.padding : 10;
        this.filterActive = false;
    }

    clickCaptured(key) {
        if (key === this.props.other_keyword) return;
        if (this.filterActive) {
            this.filterActive = false;
            this.props.onClickFilter([]);
        } else {
            this.filterActive = true;
            this.props.onClickFilter([{ label: key, value: key }]);
        }
    }

    getBlocks() {
        const totalY = _.sumBy(this.props.data, 'question_count');
        const columnname = this.props.columns[this.props.columnIndex]
        const uniqColumnValues = _.uniq(this.props.data.map(d => d[columnname]))
            .map(d => ({ key: d, weight: _.sumBy(_.filter(this.props.data, e => e[columnname] === d), this.props.valueField) }))
        const uniqColumnValuesSorted = _.orderBy(uniqColumnValues, row => row.key !== this.props.other_keyword ? row.weight : -1, ['desc']);

        const scaleY = (this.props.height - (uniqColumnValuesSorted.length - 1) * this.padding) / totalY;

        let offsetY = 0;
        return uniqColumnValuesSorted.map(({ key, weight }) => {
            const filtered_data = _.filter(this.props.data, d => d[columnname] === key);
            const blockheight = weight * scaleY;
            const percentage = weight / totalY * 100;
            const globalpercentage = weight / this.props.globalTotal * 100;

            let offsetYInner = 0;
            const rects = filtered_data.map(d => {
                const blockheightInner = d[this.props.valueField] * scaleY;
                const rect = <g key={v4()}>
                    <rect
                        key={v4()}
                        x={0}
                        y={offsetYInner}
                        width={this.props.width * .1}
                        height={blockheightInner}
                        fill={d.color}
                    />
                    <rect
                        key={v4()}
                        x={this.props.width * .9}
                        y={offsetYInner}
                        width={this.props.width * .1}
                        height={blockheightInner}
                        fill={d.color}
                    />
                </g>;
                offsetYInner += blockheightInner;
                return rect;
            });
            offsetYInner = 0;
            const innerrects = filtered_data.map(d => {
                const blockheightInner = d[this.props.valueField] * scaleY;
                const rect = <rect
                    key={v4()}
                    x={this.props.width * .1}
                    y={offsetYInner}
                    width={this.props.width * .8}
                    height={blockheightInner}
                    fill={d.color}
                />
                offsetYInner += blockheightInner;
                return rect;
            });
            offsetY += blockheight + this.padding;
            return (
                <g key={v4()}
                    transform={`translate(0,${offsetY - blockheight - this.padding})`}
                    onClickCapture={() => this.clickCaptured(key)}
                    shapeRendering="crispEdges"
                >
                    {rects}
                    <g className="InnerRects" shapeRendering="crispEdges" opacity={.1}> {innerrects} </g>
                    <PartiteLabel
                        width={this.props.width * .8 - this.padding / 2}
                        height={blockheight}
                        offsetX={this.props.width * .1 + this.padding / 2}
                        label={key}
                        percentage={percentage.toFixed(2)}
                        globalPercentage={globalpercentage.toFixed(2) + "%"}
                    />
                </g>
            );
        });
    }


    render() {
        return (
            <g transform={`translate(${this.props.offsetX}, 0)`}>
                {this.getBlocks()}
            </g>
        )
    }
}
PartiteColumn.propTypes = {
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    offsetX: PropTypes.number.isRequired,
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    columns: PropTypes.arrayOf(PropTypes.string).isRequired,
    columnIndex: PropTypes.number.isRequired,
    padding: PropTypes.number,
    onClickFilter: PropTypes.func.isRequired,
    valueField: PropTypes.string.isRequired,
    globalTotal: PropTypes.number.isRequired,
    other_keyword: PropTypes.string.isRequired
}

class PartiteLabel extends Component {
    constructor(props) {
        super(props)
        this.state = {
            percentagelabel: props.percentage
        }
    }

    label() {
        const fontSize = Math.min(this.props.height, 10) > 15 ? 15 : Math.min(this.props.height, 10);
        const textlength = this.props.label.length * fontSize / 1.8;
        if (textlength > this.props.width * .6) {
            // truncate text 
            const maxwidth = (this.props.width * .6 / fontSize * 2) - 3;
            const label = this.props.label.slice(0, maxwidth) + "...";
            return [<text
                key={v4()}
                x={0}
                y={this.props.height / 2}
                fontSize={fontSize}
                textAnchor="start"
                alignmentBaseline="middle">
                {label}
            </text>]
        } else {
            return [<text
                key={v4()}
                x={0}
                y={this.props.height / 2}
                fontSize={fontSize}
                textAnchor="start"
                alignmentBaseline="middle">
                {this.props.label}
            </text>];
        }
    }

    percentage() {
        const fontSize = Math.min(this.props.height, 10) > 15 ? 15 : Math.min(this.props.height, 10);
        return <text
            key={v4()}
            x={this.props.width}
            y={this.props.height / 2}
            fontSize={fontSize}
            textAnchor="end"
            alignmentBaseline="middle"
        >
            {this.props.percentage + "/" + this.props.globalPercentage}
        </text>
    }

    render() {
        return (
            <g key={v4()} transform={`translate(${this.props.offsetX},0)`} >
                {this.label()}
                {this.percentage()}
            </g>
        )
    }
}
PartiteLabel.propTypes = {
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    offsetX: PropTypes.number.isRequired,
    label: PropTypes.string.isRequired,
    percentage: PropTypes.string.isRequired,
    globalPercentage: PropTypes.string.isRequired
}

class PartiteEdge extends Component {
    constructor(props) {
        super(props)
        this.padding = props.padding !== undefined ? props.padding : 5;
        this.filterActive = false;
    }

    getEdges() {
        const totalY = _.sumBy(this.props.data, 'question_count');
        const uniqColumnValuesLeft = _.uniq(this.props.data.map(d => d[this.props.columnpair.left]))
            .map(d => ({ key: d, weight: _.sumBy(_.filter(this.props.data, e => e[this.props.columnpair.left] === d), this.props.valueField) }));
        const uniqColumnValuesLeftSorted = _.orderBy(uniqColumnValuesLeft, row => row.key !== this.props.other_keyword ? row.weight : -1, ['desc']);

        const uniqColumnValuesRight = _.uniq(this.props.data.map(d => d[this.props.columnpair.right]))
            .map(d => ({ key: d, weight: _.sumBy(_.filter(this.props.data, e => e[this.props.columnpair.right] === d), this.props.valueField) }));
        const uniqColumnValuesRightSorted = _.orderBy(uniqColumnValuesRight, row => row.key !== this.props.other_keyword ? row.weight : -1, ['desc']);

        const scaleYLeft = (this.props.height - (uniqColumnValuesLeftSorted.length - 1) * this.padding) / totalY;
        const scaleYRight = (this.props.height - (uniqColumnValuesRightSorted.length - 1) * this.padding) / totalY;

        let offsetYLeft = 0;
        let colorkeyOffsetYRight = {};
        return _.flatMap(uniqColumnValuesLeftSorted.map(({ key, weight }, index) => {
            const filteredDataLeft = _.filter(this.props.data, d => d[this.props.columnpair.left] === key)
            const blockHeightLeft = _.sumBy(filteredDataLeft, this.props.valueField) * scaleYLeft;

            const filteredDataLeftNested = d3.nest().key(d => d[this.props.columns[0]]).key(d => d[this.props.columnpair.right])
                .rollup(d => ({ color: d[0].color, weight: _.sumBy(d, this.props.valueField) }))
                .entries(filteredDataLeft)

            const filteredDataLeftAggregated = _.flatMap(filteredDataLeftNested.map(({ key, values }) => values.map(value => ({
                colorkey: key,
                right: value.key,
                color: value.value.color,
                weight: value.value.weight
            }))));

            let offsetYLeftInner = 0;
            const edges = filteredDataLeftAggregated.map((aggregatedRow) => {
                if (colorkeyOffsetYRight[aggregatedRow.right] === undefined) {
                    colorkeyOffsetYRight[aggregatedRow.right] = { [aggregatedRow.colorkey]: 0 };
                } else if (colorkeyOffsetYRight[aggregatedRow.right][aggregatedRow.colorkey] === undefined) {
                    colorkeyOffsetYRight[aggregatedRow.right][aggregatedRow.colorkey] = 0;
                }
                const offsetYRight = _.sumBy(_.takeWhile(uniqColumnValuesRightSorted, d => d.key !== aggregatedRow.right), d => d.weight * scaleYRight + this.padding);
                const offsetYRightInner = _.sumBy(_.takeWhile(_.filter(this.props.data, d => d[this.props.columnpair.right] === aggregatedRow.right),
                    d => d[this.props.columns[0]] !== aggregatedRow.colorkey), this.props.valueField) * scaleYRight;

                const offsetYRightInnerColor = colorkeyOffsetYRight[aggregatedRow.right][aggregatedRow.colorkey];
                const offsetYRightFull = offsetYRight + offsetYRightInner + offsetYRightInnerColor;

                const blockHeightLeftInner = aggregatedRow.weight * scaleYLeft;
                const blockHeightRightInner = aggregatedRow.weight * scaleYRight;


                let path = `M ${0} ${offsetYLeft + offsetYLeftInner} `
                path += `L ${0} ${offsetYLeft + offsetYLeftInner + blockHeightLeftInner}`
                path += `L ${this.props.width} ${offsetYRightFull + blockHeightRightInner} `
                path += `L ${this.props.width} ${offsetYRightFull} z`


                offsetYLeftInner += blockHeightLeftInner;
                colorkeyOffsetYRight[aggregatedRow.right][aggregatedRow.colorkey] += blockHeightRightInner
                return <path
                    key={v4()}
                    d={path}
                    fill={aggregatedRow.color}
                    fillOpacity={0.5}
                />
            })

            offsetYLeft += blockHeightLeft + this.padding;
            return <g key={v4()}> {edges} </g>
        }))
    }

    render() {
        return (
            <g transform={`translate(${this.props.offsetX}, 0)`} shapeRendering="geometricPrecision">
                {this.getEdges()}
            </g>
        )
    }
}
PartiteEdge.propTypes = {
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    offsetX: PropTypes.number.isRequired,
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    columnpair: PropTypes.object.isRequired,
    columns: PropTypes.arrayOf(PropTypes.string).isRequired,
    valueField: PropTypes.string.isRequired,
    other_keyword: PropTypes.string.isRequired
}