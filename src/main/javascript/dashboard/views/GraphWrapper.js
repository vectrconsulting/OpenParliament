import React, { Component } from 'react'
import { Well, Nav, NavItem, Row } from 'react-bootstrap'
import Select from 'react-select'
import { connect } from 'react-redux'
import { request } from 'superagent'

import { NPartiteGraph } from './graphs/NPartiteGraph'
import { PieChartWrapper } from './graphs/PieChart2'
import { TrendingChart } from './graphs/TrendingChart'
import { ColumnFilters } from './ColumnFilters'
import { DateFilter } from './DateFilter'
import { Share } from './Share'

@connect(state => ({
    data: state.pq.data,
    columns: state.pq.columns.items,
    current_language: state.locale.current_language.translation,
}))
export const GraphWrapper = class GraphWrapper extends Component {
    constructor(props) {
        super(props)
        this.state = {
            activeKey: "pie",
        }
    }
    body() {
        if (this.props.data.loading) {
            return <div height={800}> loading... </div>
        } else if (this.props.data.error !== null) {
            console.log(this.props.data.error)
            return <div height={800}> error </div>
        } else if (this.props.data.items) {
            switch (this.state.activeKey) {
                case "pie": return this.renderPieCharts()
                case "trending": return this.renderTrending()
                case "npartite": return this.renderNpartite()
            }
        } else {
            return <div> loading... </div>
        }
    }

    renderPieCharts() {
        return <div id="charts">
            <ColumnFilters /> <br />
            <DateFilter /> <br />
            <Share /> <br />
            <PieChartWrapper />
        </div>
    }

    renderTrending() {
        return <div id="charts">
            <ColumnFilters /> <br />
            <DateFilter /> <br />
            <Share /> <br />            
            <TrendingChart />
        </div>
    }

    renderNpartite() {
        return <div id="charts">
            <ColumnFilters /> <br />
            <DateFilter /> <br />
            <Share /> <br />
            {this.columnfilter()} <br />
            <NPartiteGraph height={800} filters={true} />
        </div>
    }

    columnfilter() {
        const state = this.props.columns
            .map(d => ({ value: d, label: this.props.current_language.columns[d] }));
        const all = ["party", "author", "topic", "department"]
            .map(d => ({ value: d, label: this.props.current_language.columns[d] }));
        return <Select
            multi={true}
            options={all}
            onChange={this.handleColumnSelect.bind(this)}
            disabled={false}
            value={state} />
    }

    handleColumnSelect(values) {
        this.props.dispatch({ type: 'SET_COLUMNS', columns: values.map(d => d.value) })
    }

    handleSelect(eventKey) {
        this.props.dispatch({ type: 'SET_COLUMNS', columns: ["party", "author", "topic", "department"] })
        this.setState({ activeKey: eventKey })
    }

    render() {
        return (
            <Well >
                <Nav bsStyle="tabs" justified activeKey={this.state.activeKey} onSelect={this.handleSelect.bind(this)}>
                    <NavItem eventKey="pie">{this.props.current_language.pie_tab}</NavItem>
                    <NavItem eventKey="trending">{this.props.current_language.trending_tab}</NavItem>
                    <NavItem eventKey="npartite">{this.props.current_language.bar_tab}</NavItem>
                </Nav>
                <br />
                {this.body()}
            </Well>
        );
    }
}