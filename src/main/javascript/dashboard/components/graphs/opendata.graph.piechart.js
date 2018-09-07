import React, {Component} from "react";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import * as d3 from "d3";
import {ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Sector} from "recharts";
import {filterData, setColumnFilter} from "../../reducers/filter";
import {filterLowOccurences} from "../../reducers/data";


export class GraphPieChart extends Component {

    columnFilter(columnName, values) {
        this.props.setColumnFilter({
            columnName: columnName,
            values: values.map(d => d.value)
        });
    }

    getPies(data, rows = false) {
        const getValues = (colummn, data) => d3.nest()
            .key(d => d[colummn])
            .rollup(d => d.reduce((acc, row) => acc + row.question_count, 0))
            .entries(data)
            .sort((a, b) => {
                const a_value = a.key !== this.props.otherKeyword ? a.value : -1;
                const b_value = b.key !== this.props.otherKeyword ? b.value : -1;
                return a_value - b_value;
            }).reverse();

        const getColor = (column, value) => this.props.colors[column][value] || "#989898";

        return this.props.columns.map((column, index) => {
            const pie_data = getValues(column, data).map(value => ({
                name: value.key,
                value: value.value,
                color: getColor(column, value.key)
            }));
            const renderActiveShape = (props) => {
                const RADIAN = Math.PI / 180;
                const {
                    cx, cy, midAngle, innerRadius, outerRadius, startAngle, endAngle,
                    fill, payload, percent, value
                } = props;
                const sin = Math.sin(-RADIAN * midAngle);
                const cos = Math.cos(-RADIAN * midAngle);
                const sx = cx + (outerRadius + 3) * cos;
                const sy = cy + (outerRadius + 3) * sin;
                const mx = cx + (outerRadius + 8) * cos;
                const my = cy + (outerRadius + 8) * sin;
                const ex = mx + (cos >= 0 ? 1 : -1) * 9;
                const ey = my;
                const textAnchor = cos >= 0 ? 'start' : 'end';

                const getname = (x, textAnchor) => payload.name.split(" ").map((value, index) =>
                    <tspan key={v4()} x={x} textAnchor={textAnchor} dy={index === 0 ? "0em" : "1.2em"}>{value}</tspan>
                );

                return (
                    <g>
                        <Sector
                            cx={cx}
                            cy={cy}
                            innerRadius={innerRadius}
                            outerRadius={outerRadius}
                            startAngle={startAngle}
                            endAngle={endAngle}
                            onClick={(item) => {
                                if (item.name !== this.props.otherKeyword) this.columnFilter(column, [{value: item.name}])
                            }}
                            fill={fill}
                        />
                        <Sector
                            cx={cx}
                            cy={cy}
                            startAngle={startAngle}
                            endAngle={endAngle}
                            innerRadius={outerRadius + 2}
                            outerRadius={outerRadius + 3}
                            fill={fill}
                        />
                        <path d={`M${sx},${sy}L${mx},${my}L${ex},${ey}`} stroke={fill} fill="none"/>
                        <circle cx={ex} cy={ey} r={2} fill={fill} stroke="none"/>
                        <text x={ex + (cos >= 0 ? 1 : -1) * 3} y={ey} textAnchor={textAnchor} fill="#333" fontSize={11}>
                            {payload.name}
                        </text>
                    </g>
                );
            };
            const activeIndexes = _.range(Math.min(3, pie_data.length))
            const cxs = ["15%", "38.3%", "61.7%", "85%"];
            return (
                <Pie
                    key={`pie-${column}`}
                    cx={rows ? "50%" : cxs[index]}
                    data={pie_data}
                    dataKey="value"
                    nameKey="name"
                    startAngle={90}
                    endAngle={450}
                    onClick={(item) => {
                        if (item.name !== this.props.otherKeyword) this.columnFilter(column, [{value: item.name}])
                    }}
                    activeIndex={activeIndexes}
                    activeShape={renderActiveShape}
                >
                    {pie_data.map(entry => <Cell key={`cell-${column}-${entry.value}`} fill={entry.color}/>)}
                </Pie>
            );
        });
    }

    getSingleRow(data) {
        return (
            <div className="d-none d-md-block" style={{paddingTop: 40}}>
                <ResponsiveContainer width="100%" height={200}>
                    <PieChart margin={{top: 20, right: 0, bottom: 20, left: 0}}>
                        {this.getPies(data, false)}
                        <Tooltip/>
                    </PieChart>
                </ResponsiveContainer>
            </div>
        );
    }

    getMultipleRows(data) {
        return this.getPies(data, true).map((pie, index) =>
            <div className="d-xs-block d-sm-block d-md-none" key={`pie-row-${index}`} style={{paddingTop: 40}}>
                <ResponsiveContainer width="100%" height={200}>
                    <PieChart margin={{top: 20, right: 0, bottom: 20, left: 0}}>
                        {pie}
                        <Tooltip/>
                    </PieChart>
                </ResponsiveContainer>
            </div>
        )
    }

    render() {
        const filtered_low_occ = filterLowOccurences(this.props.data, this.props.columns, this.props.otherKeyword);

        return (
            <div className="opendata-graph-piechart" style={{paddingTop: 40}}>
                {this.getSingleRow(filtered_low_occ)}
                {this.getMultipleRows(filtered_low_occ)}
            </div>
        );
    }
}


export default connect(
    state => ({
        data: filterData(state.data.paths.items, state.filter.columns, state.filter.dates, state.search.question).toArray(),
        colors: state.data.colors,
        columns: state.data.columns,
        otherKeyword: state.locale.translation.other
    }),
    dispatch => bindActionCreators({setColumnFilter}, dispatch)
)(GraphPieChart)