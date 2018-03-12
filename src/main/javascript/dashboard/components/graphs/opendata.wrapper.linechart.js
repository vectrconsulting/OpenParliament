import React, { Component } from 'react';

import Filters from "./opendata.graph.filters";
import GraphLineChart from "./opendata.graph.linechart";


export class LineChartWrapper extends Component {

    render() {
        return (
            <div className="opendata-linechart-wrapper">
                <Filters />
                <GraphLineChart />
            </div>
        );
    }
}

export default LineChartWrapper