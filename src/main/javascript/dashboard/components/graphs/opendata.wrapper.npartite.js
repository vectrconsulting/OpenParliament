import React, { Component } from 'react';

import Filters from "./opendata.graph.filters";
import GraphNpartite from "./opendata.graph.npartite";

import { filterData, setColumnFilter } from "../../reducers/filter";
import { filterLowOccurences } from "../../reducers/data";


export class NpartiteWrapper extends Component {

    render() {
        return (
            <div className="opendata-linechart-wrapper">
                <Filters />
                <GraphNpartite />
            </div>
        );
    }
}

export default NpartiteWrapper