import React, { Component } from 'react'
import { Well, Row, Col } from 'react-bootstrap'
import BrowserDetection from 'react-browser-detection'
import Notifications from 'react-notify-toast'

import { JoyRideWrapper } from './JoyRideWrapper'
import { Navigation } from './Navigation'
import { Footer } from './Footer'
import { Search } from './Search'
import { GraphWrapper } from './GraphWrapper'
import { Sidebar } from './Sidebar'
import { QuestionOverview } from './QuestionOverview'

export const Page = class Page extends Component {
    render() {
        const browserhandler = {
            ie: () => <div />,
            default: () => <JoyRideWrapper />,
        };
        return (
            <div className="Page">
                {/* <BrowserDetection>
                    {browserhandler}
                </BrowserDetection> */}

                <Navigation />
                <Notifications />
                <Row>
                    <Col lg={10} md={12} xs={12} lgOffset={1}><Search /></Col>
                </Row>
                <Row>
                    <Col lg={10} lgOffset={1}><GraphWrapper /></Col>
                </Row>
                <Row>
                    <Col lg={10} md={12} lgOffset={1}><QuestionOverview /></Col>
                </Row>
                <Footer />
            </div>
        );
    }
};
