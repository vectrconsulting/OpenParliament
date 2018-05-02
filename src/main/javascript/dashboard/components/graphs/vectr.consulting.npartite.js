import React, {Component} from "react";
import PropTypes from "prop-types";
import * as d3 from "d3";
import {List} from "immutable";

export class Npartite extends Component {
    constructor(props) {
        super(props);
        this.columnWidth = 3; // relative to spacing
        this.columnSpacing = 2; // relative to width
        this.padding = 5; // absolute in pixels
    }

    getColumns(data, colorOrder, scaleX, total) {
        const columns = this.props.columns.map((columnName, index) => {
            const columnValues = d3.nest()
                .key(d => d[columnName])
                .rollup(d => d3.nest()
                    .key(e => e[this.props.columns[0]])
                    .rollup(e => ({
                        count: e.reduce((acc, row) => acc + row.count, 0),
                        color: e[0][this.props.colorKey]
                    }))
                    .entries(d)
                    .sort((a, b) => {
                        const a_value = a.key !== this.props.otherKey ? colorOrder.get(a.key) : -1;
                        const b_value = b.key !== this.props.otherKey ? colorOrder.get(b.key) : -1;
                        return b_value - a_value
                    })
                    .map(e => Object.assign({}, {key: e.key, count: e.value.count, color: e.value.color}))
                )
                .entries(data)
                .map(d => Object.assign({}, d, {count: d.value.reduce((acc, row) => acc + row.count, 0)}))
                .sort((a, b) => {
                    const a_value = a.key !== this.props.otherKey ? a.count : -1;
                    const b_value = b.key !== this.props.otherKey ? b.count : -1;
                    return b_value - a_value
                });

            const scaleY = (this.props.height - (columnValues.length - 1) * this.padding) / total;
            const blocks = columnValues.map(({key: columnValue, value: children, count: weight}, index, columnValues) => {

                const outer_parts = children.map(({key, count, color}, index, children) => {
                    const offsetY = children.slice(0, index).reduce((acc, row) => acc + row.count, 0) * scaleY;
                    return (
                        <g key={`npartite-column#${columnName}-block#${columnValue}-part#${key}`}
                           transform={`translate(0, ${offsetY})`}>
                            <rect
                                x={0}
                                y={0}
                                width={(this.columnWidth * scaleX) * .1}
                                height={count * scaleY}
                                fill={color}
                            />
                            <rect
                                x={(this.columnWidth * scaleX) * .9}
                                y={0}
                                width={(this.columnWidth * scaleX) * .1}
                                height={count * scaleY}
                                fill={color}
                            />
                        </g>
                    );
                });

                const inner_parts = children.map(({key, count, color}, index, children) => {
                    const offsetY = children.slice(0, index).reduce((acc, row) => acc + row.count, 0) * scaleY;
                    return (
                        <g key={`npartite-column#${columnName}-block#${columnValue}-part#${key}`}
                           transform={`translate(0, ${offsetY})`}>
                            <rect
                                x={(this.columnWidth * scaleX) * .1}
                                y={0}
                                width={(this.columnWidth * scaleX) * .8}
                                height={count * scaleY}
                                fill={color}
                            />
                        </g>
                    );
                });

                const getLabel = () => {
                    const width = this.columnWidth * scaleX * .9 - this.padding / 2;
                    const offsetX = this.columnWidth * scaleX * .1 + this.padding / 2;
                    const height = weight * scaleY;
                    const offsetY = height / 2;
                    const lpercentage = (weight / total * 100).toFixed(2);
                    const gpercentage = (weight / this.props.total * 100).toFixed(2);
                    const fontSize = Math.min(height, 10);
                    const textlength = columnValue.length * fontSize / 1.8;
                    if (textlength > width * .6) {
                        const maxwidth = (this.props.width * .6 / fontSize * 2) - 3;
                        const label = columnValue.slice(0, maxwidth) + "...";
                        return (
                            <g>
                                <text x={offsetX} y={offsetY} fontSize={fontSize} textAnchor="start"
                                      alignmentBaseline="middle">
                                    {label}
                                </text>
                                <text x={width} y={offsetY} fontSize={fontSize} textAnchor="end"
                                      alignmentBaseline="middle">
                                    {`${lpercentage}/${gpercentage}%`}
                                </text>

                            </g>
                        );
                    } else {
                        return (
                            <g>
                                <text x={offsetX} y={offsetY} fontSize={fontSize} textAnchor="start"
                                      alignmentBaseline="middle">
                                    {columnValue}
                                </text>
                                <text x={width} y={offsetY} fontSize={fontSize} textAnchor="end"
                                      alignmentBaseline="middle">
                                    {`${lpercentage}/${gpercentage}%`}
                                </text>

                            </g>
                        );
                    }
                };

                const offsetY = columnValues.slice(0, index).reduce((acc, row) => acc + row.count, 0) * scaleY + index * this.padding;
                return (
                    <g key={`npartite-column#${columnName}-block#${columnValue}`}
                       transform={`translate(0, ${offsetY})`}
                       onClickCapture={() => this.props.onClick(columnName, columnValue)}
                    >
                        <g shapeRendering="crispEdges">
                            {outer_parts}
                        </g>
                        <g shapeRendering="crispEdges" opacity={.1}>
                            {inner_parts}
                        </g>
                        {getLabel()}
                    </g>
                );
            });

            const offsetX = index * (this.columnWidth + this.columnSpacing) * scaleX;
            return (
                <g key={`npartite-column#${columnName}`} transform={`translate(${offsetX}, 0)`}>
                    {blocks}
                </g>
            )
        });
        return (
            <g key={`npartite-columns`}>
                {columns}
            </g>
        );
    }

    getEdges(data, colorOrder, scaleX, total) {
        const columnOrder = new Map(this.props.columns.map((columnName) => {
            const columnValues = d3.nest()
                .key(d => d[columnName])
                .rollup(d => d.reduce((acc, row) => acc + row.count, 0))
                .entries(data)
                .map(d => [d.key, d.key !== this.props.otherKey ? d.value : -1]);
            return [columnName, new Map(columnValues)];
        }));

        const sortFunc = (path1, path2) => {
            for (const columnName of this.props.columns) {
                if (path1[columnName] !== path2[columnName])
                    return columnOrder.get(columnName).get(path2[columnName]) - columnOrder.get(columnName).get(path1[columnName]);
            }
            return 0;
        };

        const edgeMaps = new Map(this.props.columns.map((columnName) => {
            const columnValues = d3.nest()
                .key(d => d[columnName])
                .rollup(d => d
                    .sort(sortFunc)
                    .map(e => ({key: e.key, count: e.count, color: e.color}))
                )
                .entries(data)
                .sort((a, b) => columnOrder.get(columnName).get(b.key) - columnOrder.get(columnName).get(a.key))
                .map(d => Object.assign({}, d, {count: d.value.reduce((acc, row) => acc + row.count, 0)}));
            const scaleY = (this.props.height - (columnValues.length - 1) * this.padding) / total;

            const keyOffsets = columnValues
                .map(({key: columnValue, value: children, weight: count}, index, columnValues) => {
                    const columnValueOffsetY = columnValues.slice(0, index).reduce((acc, row) => acc + row.count, 0) * scaleY + index * this.padding;
                    return children.map(({key, count, color}, index, children) => {
                        const offsetY = children.slice(0, index).reduce((acc, row) => acc + row.count, 0) * scaleY + columnValueOffsetY;
                        const height = count * scaleY;
                        return [key, {upper: offsetY, lower: offsetY + height, color}]
                    })
                }).reduce((x, y) => x.concat(y), []);
            return [columnName, new Map(keyOffsets)]
        }));

        const edgeColumns = this.props.columns
            .slice(0, this.props.columns.length - 1)
            .map((columnName, index) => {
                const columnNameOther = this.props.columns[index + 1];
                const offsetX = (index * (this.columnWidth + this.columnSpacing) + this.columnWidth) * scaleX;
                const width = this.columnSpacing * scaleX;

                const edges = Array.from(edgeMaps.get(columnName).entries()).map(([key, {upper, lower, color}]) => {
                    const otherSide = edgeMaps.get(columnNameOther).get(key);
                    return (
                        <path
                            key={`npartite-edges-columns[${columnName},${columnNameOther}]-${key}`}
                            d={`M 0 ${upper} V ${lower} L ${width} ${otherSide.lower} V ${otherSide.upper} Z`}
                            fill={color}
                            opacity={.5}
                        />
                    )
                });

                return (
                    <g key={`npartie-edge-columns[${columnName},${columnNameOther}]`}
                       transform={`translate(${offsetX}, 0)`}
                       shapeRendering="geometricPrecision">
                        {edges}
                    </g>
                )
            });

        return (<g> {edgeColumns} </g>);
    }

    render() {
        // const new_aggregatedData = List(this.props.data)
        //     .map(d => Object.assign({}, d, {key: this.props.columns.map(column => d[column]).join("#")}))
        //     .groupBy(d => d.key).toList()
        //     .map(d => Object.assign({}, d.first(), {[this.props.weightKey]: d.countBy(d => d[this.props.weightKey])}));
        // console.log(new_aggregatedData.toJS());

        const aggregatedData = d3.nest()
            .key(d => this.props.columns.map(column => d[column]).join("#"))
            .rollup(d => Object.assign({}, d[0], {count: d.reduce((acc, row) => acc + row[this.props.weightKey], 0)}))
            .entries(this.props.data)
            .map(d => Object.assign({}, d.value, {key: d.key}));
        const colorOrder = new Map(d3.nest()
            .key(d => d[this.props.columns[0]])
            .rollup(d => d.reduce((acc, row) => acc + row.count, 0))
            .entries(aggregatedData)
            .map(d => [d.key, d.value]));

        const total = this.props.data.length;
        const scaleX = this.props.width / (this.props.columns.length * (this.columnWidth + this.columnSpacing) - this.columnSpacing);

        return (
            <svg className="vectr-consulting-npartite" width={this.props.width} height={this.props.height}>
                {this.getColumns(aggregatedData, colorOrder, scaleX, total)}
                {this.getEdges(aggregatedData, colorOrder, scaleX, total)}
            </svg>
        )
    }
}

Npartite.propTypes = {
    width: PropTypes.number,
    height: PropTypes.number,
    data: PropTypes.arrayOf(PropTypes.object),
    columns: PropTypes.arrayOf(PropTypes.string),
    colorKey: PropTypes.string,
    weightKey: PropTypes.string,
    otherKey: PropTypes.string,
    onClick: PropTypes.func,
    total: PropTypes.number
};

export default Npartite;