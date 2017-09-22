import React, { Component } from 'react'
import _ from 'lodash'
import * as d3 from 'd3'
import { Row, Col } from 'react-bootstrap'
import Select from 'react-select'
import ContainerDimensions from 'react-container-dimensions'


export const NPartiteGraph = class NPartitGraph extends Component {
    constructor(props) {
        super(props);
        if (this.props.data === undefined) throw Error("NPartiteGraph needs data to work");
        this.original_data = _.cloneDeep(this.props.data);
        this.filtered_data = _.cloneDeep(this.props.data);
        this.low_occurences_fixed = _.cloneDeep(this.props.data);
        this.total_count = this.props.data.length;
        this.colorMap = this.props.colorMap;
        if (this.props.height === undefined) throw Error("NPartiteGraph needs an heigth to work");
        this.height = this.props.height;
        this.threshold = this.props.threshold !== undefined ? this.props.threshold : 0.01;

        this.filters = [];

        this.columns = this.props.columns !== undefined ?
            this.props.columns :
            Object.keys(this.filtered_data[0]);
        this.filterLowOccurences();
        this.aggregateData();
    }

    aggregateData() {
        let columnpairs = this.columns.slice(0, -1).map((value, index) => ({ left: value, right: this.columns[index + 1] }))
        const getColor = (party) => {
            if (_.find(this.colorMap, ['partij', party]) !== undefined) {
                return _.find(this.colorMap, ['partij', party]).color;
            }
            else {
                return _.find(this.colorMap, ['partij', 'default']).color;
            }
        }

        this.split_data = columnpairs.map(pair => {
            let nested = d3.nest().key(d => d[pair.left])
                .key(d => d[pair.right])
                .rollup(d => ({ length: _.sumBy(d, 'question_count'), color: getColor(d[0].party) }))
                .entries(this.low_occurences_fixed);
            let unnested = _.flatMap(nested.map(o => o.values.map(p => ({ left: o.key, right: p.key, value: p.value }))))
            unnested = unnested.map(nest => ({ left: nest.left, right: nest.right, value: nest.value.length, color: nest.value.color }))
            return unnested;
        });
    }


    filterLowOccurences() {
        const filtercopy = _.cloneDeep(this.filtered_data)
        this.columns.forEach(column => {
            let nested = d3.nest()
                .key(d => d[column])
                .rollup(d => d3.sum(d, d2 => d2.question_count))
                .entries(this.filtered_data);
            const total = _.sumBy(nested, "value");
            const ok_keys = _.filter(nested, d => d.value / total > this.threshold)
                .map(d => d.key);

            this.low_occurences_fixed = _.filter(filtercopy, d => _.includes(ok_keys, d[column]))
        })

    }

    hasFilters() {
        return this.props.filters !== undefined ? this.props.filters : false;
    }

    numberOfColumns() {
        return this.props.columns !== undefined ? this.props.columns.length :
            this.props.data !== undefined ? this.props.data[0].keys().length : 0;
    }

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

    getParts(width) {
        const scaleX = width / (this.numberOfColumns() * 70 - 40)


        const offsetX = (index) => (index * 70) * scaleX;

        return this.split_data.map((data, index) => {
            const onClickFilterl = (value) => {
                this.dataFilter(this.columns[index], value);
            }
            const onClickFilterr = (value) => {
                this.dataFilter(this.columns[index + 1], value);
            }

            return <PartiteColumn
                key={index}
                data={data}
                drawLeft={index === 0}
                offsetX={offsetX(index)}
                offsetX2={offsetX(index + 1)}
                blockWidth={30 * scaleX}
                onClickFilterl={onClickFilterl}
                onClickFilterr={onClickFilterr}
                height={this.height}
            />
        })
    }


    render() {
        return (
            <div>
                {this.hasFilters() &&
                    <Row>
                        {this.getFilters()}
                    </Row>
                }
                <br />
                <ContainerDimensions>
                    {({ width }) =>
                        <svg width={width} height={this.height}> {/* Graph */}
                            {this.getParts(width)}
                        </svg>}
                </ContainerDimensions>
            </div>

        )
    }
}

class PartiteColumn extends Component {
    constructor(props) {
        super(props)
        this.padding = props.padding !== undefined ? props.padding : 5;
        this.height = props.height;
    }

    computeMatrixes() {
        this.left = _.uniq(this.props.data.map(d => d.left)).sort((a, b) => {
            return _.sumBy(_.filter(this.props.data, d => (d.left === b)), 'value') -
                _.sumBy(_.filter(this.props.data, d => (d.left === a)), 'value');
        });;
        this.right = _.uniq(this.props.data.map(d => d.right)).sort((a, b) => {
            return _.sumBy(_.filter(this.props.data, d => (d.right === b)), 'value') -
                _.sumBy(_.filter(this.props.data, d => (d.right === a)), 'value');
        });;
        this.color = _.uniq(this.props.data.map(d => ({ left: d.left, color: d.color })));

        this.matrix = []
        this.left.forEach((leftv, index) => {
            this.matrix.push([])
            this.right.forEach((rightv) => {
                let filtered = _.filter(this.props.data, d => (d.left === leftv && d.right === rightv));
                if (filtered.length > 0) {
                    this.matrix[index].push(_.sumBy(filtered, 'value'));
                } else {
                    this.matrix[index].push(0);
                }
            })
        });
        this.matrixT = _.zip.apply(_, this.matrix);
    }

    getLeft() {
        const totalY = _.sum(this.matrix.map(row => _.sum(row)))
        const scaleY = (this.height - this.matrix.length * this.padding) / totalY;
        this.scaleYL = scaleY;
        const transform = (values, index) => values.map((value, index2) => ({
            value: value * scaleY,
            color: _.find(this.color, o => o.left === this.left[index]).color,
            key: this.left[index],
        }))

        const offsetX = this.props.offsetX;
        let offsetY = 0;
        let rects = []
        if (!this.props.drawLeft) return rects;

        this.matrix.forEach((values, index) => {
            const blockheight = _.sum(values) * scaleY;
            const percentage = _.sum(values) / totalY * 100;
            rects.push(
                <PartiteBlock
                    key={this.left[index] + 'column'}
                    data={transform(values, index)}
                    x={offsetX}
                    y={offsetY}
                    width={this.props.blockWidth}
                    onClickFilter={this.props.onClickFilterl}
                />
            );
            if (blockheight > 5) {
                rects.push(
                    <text
                        key={this.left[index] + "label"}
                        x={offsetX + this.padding}
                        y={offsetY + blockheight / 2}
                        fontSize={Math.min(blockheight, 20) > 40 ?
                            40 : Math.min(blockheight, 20)}
                        textAnchor="start"
                        alignmentBaseline="middle">
                        {this.left[index]}
                    </text>,
                    <text
                        key={this.left[index] + "perc"}
                        x={offsetX + this.props.blockWidth - this.padding}
                        y={offsetY + blockheight / 2}
                        fontSize={Math.min(blockheight, 20) > 40 ?
                            40 : Math.min(blockheight, 20)}
                        textAnchor="end"
                        alignmentBaseline="middle">
                        {percentage.toPrecision(3) + "%"}
                    </text>
                )
            }
            offsetY += blockheight + this.padding;
        })

        return rects
    }

    getRight() {
        const totalY = _.sum(this.matrix.map(row => _.sum(row)))
        const scaleY = (this.height - this.matrixT.length * this.padding) / totalY;
        this.scaleYR = scaleY;

        const offsetX = this.props.offsetX2;
        let offsetY = 0;
        let rects = []

        const transform = (values, index) => values.map((value, index2) => ({
            value: value * scaleY,
            color: _.find(this.color, o => o.left === this.left[index2]).color,
            key: this.right[index],
        }));

        this.matrixT.forEach((values, index) => {
            const blockheight = _.sum(values) * scaleY;
            const percentage = _.sum(values) / totalY * 100;
            rects.push(
                <PartiteBlock
                    key={this.right[index] + 'column'}
                    data={transform(values, index)}
                    x={offsetX}
                    y={offsetY}
                    width={this.props.blockWidth}
                    onClickFilter={this.props.onClickFilterr}
                />
            );
            if (blockheight > 5) {
                rects.push(
                    <text
                        key={this.right[index] + "label"}
                        x={offsetX + this.padding}
                        y={offsetY + blockheight / 2}
                        fontSize={Math.min(blockheight, 20) > 40 ?
                            40 : Math.min(blockheight, 20)}
                        textAnchor="start"
                        alignmentBaseline="middle">
                        {this.right[index]}
                    </text>,
                    <text
                        key={this.right[index] + "perc"}
                        x={offsetX + this.props.blockWidth - this.padding}
                        y={offsetY + blockheight / 2}
                        fontSize={Math.min(blockheight, 20) > 40 ?
                            40 : Math.min(blockheight, 20)}
                        textAnchor="end"
                        alignmentBaseline="middle">
                        {percentage.toPrecision(3) + "%"}
                    </text>
                )
            }
            offsetY += blockheight + this.padding;
        })
        return rects
    }

    getEdges() {
        const xL = this.props.offsetX + this.props.blockWidth;
        const xR = this.props.offsetX2;

        const scaleYL = this.scaleYL;
        const scaleYR = this.scaleYR;

        let edges = []

        this.left.forEach((l, indexL) =>
            this.right.forEach((r, indexR) => {
                if (this.matrix[indexL][indexR] !== 0) {
                    const blockHeightL = this.matrix[indexL][indexR] * scaleYL;
                    const blockHeightR = this.matrix[indexL][indexR] * scaleYR;

                    const yOffsetL = (_.sum(this.matrix.slice(0, indexL).map(v => _.sum(v)))
                        + _.sum(this.matrix[indexL].slice(0, indexR))) * scaleYL
                        + indexL * this.padding;

                    const yOffsetR = (_.sum(this.matrixT.slice(0, indexR).map(v => _.sum(v)))
                        + _.sum(this.matrixT[indexR].slice(0, indexL))) * scaleYR
                        + indexR * this.padding;

                    let path = `M ${xL} ${yOffsetL} L ${xL} ${yOffsetL + blockHeightL}`
                    path += `L ${xR} ${yOffsetR + blockHeightR} L ${xR} ${yOffsetR} Z`

                    edges.push(
                        <path key={l + "path" + r} d={path} fill={_.find(this.color, o => o.left === l).color} fillOpacity={0.5} />
                    )
                }
            }));

        return edges;
    }

    render() {
        this.computeMatrixes();
        return (
            <g >
                {this.getLeft()}
                {this.getRight()}
                {this.getEdges()}
            </g>
        );
    }
}

class PartiteBlock extends Component {
    constructor(props){
        super(props)
        this.filtered = false;
    }

    filter(d){
        if(this.filtered){
            this.filtered = false;
            this.props.onClickFilter([]) 
        }else{
            this.filtered = true;
            this.props.onClickFilter([{
                label: d.key, value: d.key
            }])
        }
    }

    getRects() {
        let yOffset = 0
        let i = 0;
        return _.filter(this.props.data, d => d.value !== 0).map(d => {
            const r = <rect
                x={0}
                y={yOffset}
                width={this.props.width}
                height={d.value + 1}
                fill={d.color}
                key={d.key + i}
                onClickCapture={() => this.filter(d)}
            />
            yOffset += d.value;
            i++;
            return r
        });
    }

    render() {
        return (
            <g
                transform={`translate(${this.props.x || 0}, ${this.props.y || 0})`}>
                {this.getRects()}
            </g>
        );
    }
}
