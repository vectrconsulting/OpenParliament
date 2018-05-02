import React, { Component } from 'react';

import Filters from "./opendata.graph.filters";
import GraphPieChart from "./opendata.graph.piechart";


export class PieChartWrapper extends Component {
    render() {
        return (
            <div className="opendata-piechart-wrapper">
                <Filters />
                <GraphPieChart />
            </div>
        );
    }
}

export default PieChartWrapper