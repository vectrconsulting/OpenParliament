import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import * as d3 from "d3";
import moment from "moment";

import { filterData, setColumnFilter } from "../../reducers/filter";

export class GraphLineChart extends Component {
    constructor(props) {
        super(props);
        this.party = "party";
        this.author = "author";
        this.topic = "topic";
        this.department = "department";

        this.state = {
            column: this.party,
            party: true,
            author: false,
            topic: false,
            department: false
        }
    }

    setTab(tab) {
        switch (tab) {
            case this.party:
                this.setState({
                    column: this.party,
                    party: true,
                    author: false,
                    topic: false,
                    department: false
                })
                break;
            case this.author:
                this.setState({
                    column: this.author,
                    party: false,
                    author: true,
                    topic: false,
                    department: false
                })
                break;
            case this.topic:
                this.setState({
                    column: this.topic,
                    party: false,
                    author: false,
                    topic: true,
                    department: false
                })
                break;
            case this.department:
                this.setState({
                    column: this.department,
                    party: false,
                    author: false,
                    topic: false,
                    department: true
                })
                break;
            default:
                this.setState({
                    column: this.party,
                    party: true,
                    author: false,
                    topic: false,
                    department: false
                })
                break;
        }
    }

    renderGraph(data) {
        const getLineKeys = (data) => {
            const keys = d3.nest()
                .key(d => d[this.state.column])
                .rollup(d => d.reduce((acc, row) => acc + row.question_count, 0))
                .entries(data)
                .sort((a, b) => b.value - a.value)
                .map(d => d.key);
            if (this.props.columnFilters[this.state.current_tab]) return keys;
            else return keys.slice(0, 10);
        };

        const lineKeys = getLineKeys(data);
        const lineKeysZeros = Object.assign({}, ...lineKeys.map(key => ({ [key]: 0 })));
        const lineData = d3.nest()
            .key(d => moment(d.date).format("YYYY-MM"))
            .rollup(d =>
                Object.assign({}, lineKeysZeros, ...d3.nest()
                    .key(e => e[this.state.column])
                    .rollup(e => e.reduce((acc, row) => acc + row.question_count, 0))
                    .entries(d)
                    .map(e => ({ [e.key]: e.value })))
            )
            .entries(data.filter(d => lineKeys.includes(d[this.state.column])))
            .sort((a, b) => a.key.localeCompare(b.key))
            .map(d => Object.assign({}, { name: moment(d.key).format("MMM Y") }, d.value))

        const lineCharts = lineKeys.map(value =>
            <Line key={`line-${value}`} type="monotone" dataKey={value} stroke={this.props.colors[this.state.column][value]} />
        );

        return (
            <ResponsiveContainer width="100%" height={600}>
                <LineChart data={lineData} margin={{ top: 10, right: 40, bottom: 40, left: 40 }}>
                    <XAxis dataKey="name" />
                    <YAxis />
                    <CartesianGrid strokeDasharray="3 3" />
                    <Legend />
                    <Tooltip />
                    {lineCharts}
                </LineChart>
            </ResponsiveContainer>
        );
    }

    render() {
        return (
            <div className="opendata-graph-linechart row justify-content-center" style={{ paddingTop: 20 }}>
                <div className="col">
                    <div className="card border-0">
                        <ul className="nav nav-tabs nav-fill">
                            <li className="nav-item">
                                <a className={`nav-link ${this.state.party ? "active bg-primary" : "bg-dark"}`} onClick={() => this.setTab(this.party)} style={{ color: "#fff" }}>
                                    {this.props.party}
                                </a>
                            </li>
                            <li className="nav-item">
                                <a className={`nav-link ${this.state.author ? "active bg-primary" : "bg-dark"}`} onClick={() => this.setTab(this.author)} style={{ color: "#fff" }}>
                                    {this.props.author}
                                </a>
                            </li>
                            <li className="nav-item">
                                <a className={`nav-link ${this.state.topic ? "active bg-primary" : "bg-dark"}`} onClick={() => this.setTab(this.topic)} style={{ color: "#fff" }}>
                                    {this.props.topic}
                                </a>
                            </li>
                            <li className="nav-item">
                                <a className={`nav-link ${this.state.department ? "active bg-primary" : "bg-dark"}`} onClick={() => this.setTab(this.department)} style={{ color: "#fff" }}>
                                    {this.props.department}
                                </a>
                            </li>
                        </ul>
                        <div style={{ paddingTop: 10 }}>
                            {this.renderGraph(this.props.data)}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default connect(
    state => ({
        data: filterData(state.data.paths.items, state.filter.columns, state.filter.dates, state.search.question).toArray(),
        colors: state.data.colors,
        columns: state.data.columns,
        columnFilters: state.filter.columns,
        party: state.locale.translation.columns.party,
        author: state.locale.translation.columns.author,
        topic: state.locale.translation.columns.topic,
        department: state.locale.translation.columns.department,
    }),
    dispatch => bindActionCreators({ setColumnFilter }, dispatch)
)(GraphLineChart)