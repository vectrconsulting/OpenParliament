import React, { Component } from 'react'
import _ from 'lodash'
import * as d3 from 'd3'
import { Row, Col } from 'react-bootstrap'
import Select from 'react-select'
import ContainerDimensions from 'react-container-dimensions'
import PropTypes from 'prop-types'
import { v4 } from 'uuid'


export const NPartiteGraph = class NPartitGraph extends Component {
    constructor(props) {
        super(props);

        this.original_data = _.cloneDeep(this.props.data);
        this.colorMap = this.props.colorMap; // dictionary that maps the first column to a color
        this.height = this.props.height; // height of drawing in pixels
        this.threshold = props.threshold !== undefined ? props.threshold : 0.01; // threshold for values to omit
        this.columnWidth = props.columnWidth !== undefined ? props.columnWidth : 2; // relative column size
        this.columnSpacing = props.columnSpacing !== undefined ? props.columnSpacing : 1; // relative columnspacing (edges)
        this.padding = props.padding !== undefined ? props.padding : 10; // padding in pixels
        this.valueField = props.valueField !== undefined ? props.valueField : "question_count"; // property containing value, defaults to question_count
        this.hasFilters = props.filters !== undefined ? props.filters : false;
        this.filters = [];
        this.columns = this.props.columns !== undefined ? this.props.columns : Object.keys(this.original_data[0]);

        this.globalTotal = _.sumBy(this.original_data, this.valueField);

        let grouped = {}
        this.columns.forEach(column => 
            grouped[column] = _.groupBy(this.original_data, column)
        )

        this.globalPercentages = {}
        Object.keys(grouped).forEach( column => {
            this.globalPercentages[column] = {}
            Object.keys(grouped[column]).forEach(value => {
                this.globalPercentages[column][value] = _.sumBy(grouped[column][value], this.valueField)/this.globalTotal * 100;
            })
        })


        this.filtered_data = _.cloneDeep(this.original_data);
        this.filterLowOccurences();
        this.aggregateData();
    }

    aggregateData() {
        // let columnpairs = this.columns.slice(0, -1).map((value, index) => ({ left: value, right: this.columns[index + 1] }))
        const getColor = (party) => {
            if (_.find(this.colorMap, ['partij', party]) !== undefined) {
                return _.find(this.colorMap, ['partij', party]).color;
            }
            else {
                return _.find(this.colorMap, ['partij', 'default']).color;
            }
        }
        this.coloredPaths = _.orderBy(this.low_occurences_fixed.map(d => Object.assign(d, {color: getColor(d.party)})),
            this.columns.map(column => d => _.sumBy(_.filter(this.low_occurences_fixed, e => e[column] === d[column]), this.valueField)),
            this.columns.map(column => 'desc'));
    }

    /**
     * Filters out values per column that do not have high enough values
     * This is done for each filtered set so entries can change when filters are applied
     */
    filterLowOccurences() {
        this.low_occurences_fixed = _.cloneDeep(this.filtered_data)
        this.columns.forEach(column => {
            let nested = d3.nest()
                .key(d => d[column])
                .rollup(d => d3.sum(d, d2 => d2.question_count))
                .entries(this.filtered_data);
            const total = _.sumBy(nested, "value");
            const ok_keys = _.filter(nested, d => d.value / total > this.threshold)
                .map(d => d.key);

            this.low_occurences_fixed = _.filter(this.low_occurences_fixed, d => _.includes(ok_keys, d[column]))
        })
    }

    /**
     * Will filter the data given a column and values to include, will overwrite previous filters on that column
     * To reset a filter on a column pass an empty array for that array
     * @param {string} column column to filter
     * @param {string[]} values list of values to filter on
     */
    dataFilter(column, values) {
        this.filters = _.filter(this.filters, d => d.column !== column)
        this.filters.push({ column: column, value: values })


        this.filtered_data = _.cloneDeep(this.original_data);

        this.filters.forEach((filter) => {
            if (filter.value.length !== 0) {
                const column = filter.column;
                const values = filter.value.map(d => d.value);

                this.filtered_data = _.filter(this.filtered_data, d => _.includes(values, d[column]));
            }
        })
        this.forceUpdate();
    }

    componentWillUpdate() {
        this.filterLowOccurences();
        this.aggregateData();
    }

    /**
     * Returns a row of multiselect dropdowns for the filter row
     */
    getFilters() {
        const filters = [];
        this.columns.forEach((column) => {
            const values = _.uniq(this.original_data.map(d => d[column]))
                .map(d => ({ value: d, label: d }))

            filters.push(
                <Col key={column + "filtercolumn"} md={Math.floor(12 / this.columns.length)}>
                    <Select
                        placeholder={column.cap}
                        key={column + "filter"}
                        multi={true}
                        options={values}
                        onChange={val => this.dataFilter(column, val)}
                        disabled={false}
                        value={
                            _.find(this.filters, d => d.column === column) !== undefined ?
                                _.find(this.filters, d => d.column === column).value :
                                []}
                    />
                </Col>
            )
        })
        return filters;
    }

    /**
     * Returns all the parts of the NPartite graph: columns and edges
     * @param {number} width width of the current view, passed down by ContainerDimensions
     */
    getColumns(width) {
        const scaleX = width / (this.columns.length * (this.columnWidth + this.columnSpacing) - this.columnSpacing)

        const offsetX = (index) => (index * (this.columnWidth + this.columnSpacing)) * scaleX;

        return this.columns.map((column, index, columns) => {
            const onClickFilter = (value) => {
                this.dataFilter(column, value);
            }

            return <PartiteColumn
                key={index}
                width={this.columnWidth * scaleX}
                height={this.height}
                offsetX={offsetX(index)}
                padding={this.padding}
                data={this.coloredPaths}
                columns={columns}
                columnIndex={index}
                onClickFilter={onClickFilter}
                valueField={this.valueField}
                globalPercentages={this.globalPercentages[column]}
            />
        })
    }

    getEdges(width) {
        const scaleX = width / (this.columns.length * (this.columnWidth + this.columnSpacing) - this.columnSpacing)
        const offsetX = (index) => (index * (this.columnWidth + this.columnSpacing) + this.columnWidth) * scaleX

        const columnpairs = this.columns.slice(0, -1).map((value, index) => ({ left: value, right: this.columns[index + 1] }))

        return columnpairs.map((columnpair, index) =>
            <PartiteEdge
                key={v4()}
                width={this.columnSpacing * scaleX}
                height={this.height}
                offsetX={offsetX(index)}
                data={this.coloredPaths}
                columnpair={columnpair}
                columns={this.columns}
                valueField={this.valueField}
            />
        );
    }

    render() {
        return (
            <div>
                {this.hasFilters &&
                    <Row>
                        {this.getFilters()}
                    </Row>
                }
                <br />
                <ContainerDimensions>
                    {({ width }) =>
                        <svg width={width} height={this.height}>
                            {this.getColumns(width)}
                            {this.getEdges(width)}
                        </svg>}
                </ContainerDimensions>
            </div>

        )
    }
}

class PartiteColumn extends Component {
    constructor(props) {
        super(props)
        this.padding = props.padding !== undefined ? props.padding : 10;
        this.filterActive = false;
    }

    clickCaptured(key) {
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
        const uniqColumnValues = _.orderBy(_.uniq(this.props.data.map(d => d[columnname]))
            .map(d => ({ key: d, weight: _.sumBy(_.filter(this.props.data, e => e[columnname] === d), this.props.valueField) })), ['weight'], ['desc']);

        const scaleY = (this.props.height - (uniqColumnValues.length - 1) * this.padding) / totalY;

        let offsetY = 0;
        return uniqColumnValues.map(({ key, weight }) => {
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
                        x={this.props.width * .1}
                        y={offsetYInner}
                        width={this.props.width * .8}
                        height={blockheightInner}
                        fill={d.color}
                        fillOpacity={0.1}
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
            }
            )
            offsetY += blockheight + this.padding;
            return (
                <g key={v4()}
                    transform={`translate(0,${offsetY - blockheight - this.padding})`}
                    onClickCapture={() => this.clickCaptured(key)}
                    shapeRendering="crispEdges"
                >
                    {rects}
                    <PartiteLabel 
                        width={this.props.width*.8-this.padding/2}
                        height={blockheight}
                        offsetX={this.props.width*.1+this.padding/2} 
                        label={key}
                        percentage={percentage.toPrecision(3)+"%"}
                        globalPercentage={this.props.globalPercentages[key].toPrecision(3)+"%"}
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
    globalPercentages: PropTypes.object.isRequired
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
            {this.props.percentage+"/" +this.props.globalPercentage}
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
        this.padding = props.padding !== undefined ? props.padding : 10;
        this.filterActive = false;
    }

    getEdges() {
        const totalY = _.sumBy(this.props.data, 'question_count');
        const uniqColumnValuesLeft = _.orderBy(_.uniq(this.props.data.map(d => d[this.props.columnpair.left]))
            .map(d => ({ key: d, weight: _.sumBy(_.filter(this.props.data, e => e[this.props.columnpair.left] === d), this.props.valueField) })), ['weight'], ['desc']);
        const uniqColumnValuesRight = _.orderBy(_.uniq(this.props.data.map(d => d[this.props.columnpair.right]))
            .map(d => ({ key: d, weight: _.sumBy(_.filter(this.props.data, e => e[this.props.columnpair.right] === d), this.props.valueField) })), ['weight'], ['desc']);

        const scaleYLeft = (this.props.height - (uniqColumnValuesLeft.length - 1) * this.padding) / totalY;
        const scaleYRight = (this.props.height - (uniqColumnValuesRight.length - 1) * this.padding) / totalY;

        let offsetYLeft = 0;
        let colorkeyOffsetYRight = {};
        return _.flatMap(uniqColumnValuesLeft.map(({ key, weight }, index) => {
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
                const offsetYRight = _.sumBy(_.takeWhile(uniqColumnValuesRight, d => d.key !== aggregatedRow.right), d => d.weight * scaleYRight + this.padding);
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
    valueField: PropTypes.string.isRequired
}