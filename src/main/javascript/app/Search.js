import React, { Component } from 'react';
import { Row, Col } from 'react-bootstrap';


export const Search = class Search extends Component {
    render() {
        return (
            <div className="Search" >
                <Row>
                    <Col xs={8} xsOffset={2}>
                        <input placeholder="Search for Products, Brands and more" name=""/>
                    </Col>

                </Row>

            </div>
        );
    }
};