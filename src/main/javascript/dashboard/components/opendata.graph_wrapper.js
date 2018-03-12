import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";

import PieChartWrapper from "./graphs/opendata.wrapper.piechart";
import LineChartWrapper from "./graphs/opendata.wrapper.linechart";
import NpartiteWrapper from "./graphs/opendata.wrapper.npartite";


export class GraphWrapper extends Component {

    constructor(props) {
        super(props);

        this.pie = "pie"
        this.trending = "trending"
        this.npartite = "npartite"

        this.state = {
            pie: true,
            trending: false,
            npartite: false
        }
    }

    setTab(tab) {
        switch (tab) {
            case this.pie:
                this.setState({
                    pie: true,
                    trending: false,
                    npartite: false
                });
                break;
            case this.trending:
                this.setState({
                    pie: false,
                    trending: true,
                    npartite: false
                });
                break;
            case this.npartite:
                this.setState({
                    pie: false,
                    trending: false,
                    npartite: true
                });
                break;
        }
    }

    renderGraph() {
        if (this.props.data_loading)
            return (<div>Loading...</div>);
        else if (this.state.pie)
            return (<PieChartWrapper />);
        else if (this.state.trending)
            return (<LineChartWrapper />);
        else if (this.state.npartite)
            return (<NpartiteWrapper />);
    }

    render() {
        return (
            <div className="opendata-graph-wrapper row justify-content-center" style={{ paddingTop: 10 }}>
                <div className="col-lg-11 col-xl-10">
                    <div className="card opendata-graph-card border-0">
                        <ul className="nav nav-tabs nav-fill">
                            <li className="nav-item">
                                <a className={`nav-link ${this.state.pie ? "active bg-primary" : "bg-dark"}`} onClick={() => this.setTab(this.pie)} style={{ color: "#fff" }}>
                                    {this.props.pie}
                                </a>
                            </li>
                            <li className="nav-item">
                                <a className={`nav-link ${this.state.trending ? "active bg-primary" : "bg-dark"}`} onClick={() => this.setTab(this.trending)} style={{ color: "#fff" }}>
                                    {this.props.trending}
                                </a>
                            </li>
                            <li className="nav-item">
                                <a className={`nav-link ${this.state.npartite ? "active bg-primary" : "bg-dark"}`} onClick={() => this.setTab(this.npartite)} style={{ color: "#fff" }}>
                                    {this.props.npartite}
                                </a>
                            </li>
                        </ul>
                        <div style={{ paddingTop: 10 }}>
                            {this.renderGraph()}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default connect(
    state => ({
        pie: state.locale.translation.pie_tab,
        trending: state.locale.translation.trending_tab,
        npartite: state.locale.translation.bar_tab,
        data_loading: state.data.paths.loading
    })
)(GraphWrapper);