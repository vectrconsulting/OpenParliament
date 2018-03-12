import React, { Component } from 'react';

import Header from "./opendata.header";
import Search from "./opendata.search";
import GraphWrapper from "./opendata.graph_wrapper";
import QuestionWrapper from "./opendata.question_wrapper";
import Footer from "./opendata.footer";

export default class Page extends Component {
    render() {
        return (
            <div className="opendata-page">
                <Header />
                <div className="opendata-body" style={{ paddingTop: 60 }}>
                    <Search />
                    <GraphWrapper />
                    <QuestionWrapper />
                </div>
                <Footer />
            </div>
        );
    }
}