import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Well, Row, Col } from 'react-bootstrap'
import request from 'superagent'

import { store } from '../reducers/store'


@connect(state => ({}))
export const Search = class Search extends Component {
    search(event) {
        if (event.keyCode === 13 && event.target.value != '') {
            const question = event.target.value;
            this.props.dispatch( {
                type: 'GET_FILTERS_FROM_TEXT',
                question: question,
                next: this.props.dispatch
            })
        }
    }

    render() {
        return (
            <Well className="Search" >
                <Row>
                    <Col >
                        <input id="question" type="text" className="form-control" placeholder="question filter" onKeyDown={this.search.bind(this)} />
                    </Col>
                </Row>
            </Well>
        );
    }
};
