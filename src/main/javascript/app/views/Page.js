import React, { Component } from 'react'
import { Well, Row, Col } from 'react-bootstrap'
import { connect } from 'react-redux'
import request from 'superagent'

import { JoyRideWrapper } from './JoyRideWrapper'
import { Navigation } from './Navigation'
import { Footer } from './Footer'
import { GraphWrapper } from './GraphWrapper'
import { Search } from './Search'
import { Sidebar } from './Sidebar'

@connect()
export const Page = class Page extends Component {
    render() {
        return (
            <div className="Page">
                <JoyRideWrapper />
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
