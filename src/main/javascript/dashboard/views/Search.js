import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Well, Row, Col, Button } from 'react-bootstrap'
import request from 'superagent'

import { store } from '../reducers/store'


@connect(state => ({
    language: state.locale.current_language.code,
    search: state.locale.current_language.translation.search,
    example_question: state.locale.current_language.translation.example_question,
    current_question: state.filter.current_question
}))
export const Search = class Search extends Component {
    constructor(props) {
        super(props);
        this.state = {
            inputValue: ""
        };
    }

    search() {
        const question = this.state.inputValue
        this.props.dispatch({
            type: 'GET_FILTERS_FROM_TEXT',
            question: question,
            lang: this.props.language,
            next: this.props.dispatch
        })
        this.props.dispatch({ type: 'SET_CURRENT_QUESTION', question: question })
    }



    render() {
        const value = this.state.inputValue === "" && this.props.current_question !== "" ? this.props.current_question : this.state.inputValue;
        return (
            <Well className="Search" >
                <Row>
                    <Col sm={10} xs={9}>
                        <input
                            id="question"
                            type="text"
                            className="form-control"
                            placeholder={this.props.example_question}
                            value={value}
                            onChange={event => {
                                if (this.props.current_question !== "") this.props.dispatch({ type: 'SET_CURRENT_QUESTION', question: "" });
                                this.setState({ inputValue: event.target.value });
                            }}
                            onKeyPress={event => { if (event.key === "Enter") this.search() }}
                        />
                    </Col>
                    <Col sm={2} xs={3}> <Button onClick={this.search.bind(this)}>{this.props.search}</Button></Col>
                </Row>
            </Well>
        );
    }
};
