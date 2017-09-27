import React, { Component } from 'react'
import { Well, Row, Col } from 'react-bootstrap'
import Request from 'react-http-request'

import { NPartiteGraph } from '../lib/graph2'
import { Navigation } from './Navigation'
import { Footer } from './Footer'

// eslint-disable-next-line
const columns =  ["party", "author", "topic_nl", "department_nl"]
// eslint-disable-next-line
const partijKleuren = [
    { partij: "N-VA", color: "gold" },
    { partij: "VB", color: "brown" },
    { partij: "Open Vld", color: "DodgerBlue" },
    { partij: "SP.A", color: "red" },
    { partij: "Vuye&Wouters", color: "DimGray" },
    { partij: "CD&V", color: "orange" },
    { partij: "Ecolo-Groen", color: "green" },
    { partij: "MR", color: "blue" },
    { partij: "PS", color: "red" },
    { partij: "CDH", color: "orange" },
    { partij: "FDF", color: "pink" },
    { partij: "PTB-GO!", color: "red" },
    { partij: "DEFI", color: "GreenYellow" },
    { partij: "VUWO", color: "lightcyan" },
    { partij: "UNKN", color: "moccasin" },
    { partij: "Overige", color: "grey" },
    { partij: "default", color: "pink" },
]


class Search extends Component {
    render() {
        return (
            <Well className="Search" />
        );
    }
}

class GraphWrapper extends Component {
    render() {
        return (
            <Well className="GraphWrapper" ref="graphWrapper">
                <Request
                    url='/pq'
                    method='get'
                    accept='application/json'>
                    {
                        ({ error, result, loading }) => {
                            if (loading) {
                                return <div>loading...</div>;
                            } else if (result) {
                                return (
                                    <NPartiteGraph
                                        data={JSON.parse(result.text)}
                                        columns={columns}
                                        colorMap={partijKleuren}
                                        height={1000}
                                        filters={true} />

                                );
                            } else {
                                return <div> error </div>
                            }
                        }
                    }
                </Request>
            </Well>
        );
    }
}


class Sidebar extends Component {
    render() {
        return (
            <Well className="Sidebar" />
        )
    }
}

export const Page = class Page extends Component {
    render() {
        return (
            <div className="Page">
                <Navigation />
                <div className="Container">
                    <Row>
                        <Col lg={10} md={12} xs={12} lgOffset={1}><Search /></Col>
                    </Row>
                    <Row>
                        <Col lg={8} md={12} lgOffset={1}><GraphWrapper /></Col>
                        <Col lg={2} mdHidden smHidden xsHidden ><Sidebar /></Col>
                    </Row>
                </div>
                <Footer />
            </div>
        );
    }
};
