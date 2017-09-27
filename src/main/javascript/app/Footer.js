import React, { Component } from 'react';
import { Well, Row, Col, Image } from 'react-bootstrap';

export const Footer = class Navigation extends Component {
    render() {
        return (
            <div className="Footer">
                <Well width="100%" height={30}>
                    <Row>
                        <Col mdOffset={7} smOffset={2} xsOffset={1}>Gerealiseerd door:
                            <a href="http://www.vectr.consulting"><Image src="logos/VectrConsulting.svg" height={30}/></a>
                            <a href="http://www.vereenvoudiging.be/" ><Image src="logos/DAV.png" height={30}/></a>
                            <a href="http://www.dekamer.be/" ><Image src="logos/dekamer.jpg" height={30}/></a>
                            <a href="https://neo4j.com/" ><Image src="logos/Neo4j.png" height={30}/></a>
                        </Col>
                    </Row>
                </Well>
            </div>
        );
    }
};