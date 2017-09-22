import React, { Component } from 'react';
import { Navbar, Row } from 'react-bootstrap';
import { Search } from './Search'

export const Navigation = class Navigation extends Component {
    render() {
        return (
            <div className="Navigation">
                <Navbar>
                    <Row>
                        <Navbar.Header>
                            <Navbar.Brand>
                                <a href="/ui/index.html">
                                    Dekamer<font className="BeBrand">.be</font>
                                </a>
                            </Navbar.Brand>
                        </Navbar.Header>
                    </Row>
                </Navbar>
            </div>
        );
    }
};