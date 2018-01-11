import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Well, Row, Col } from 'react-bootstrap'
import request from 'superagent'
import { v4 } from 'uuid'
import _ from 'lodash'
import * as d3 from 'd3'

import { store } from '../reducers/store'

@connect(state => ({ questions: state.faq.questions, current_language: state.locale.current_language.translation}))
export const Sidebar = class Sidebar extends Component {
    filter(question, event) {
        const aggregated = d3.nest()
            .key(d => d.type)
            .rollup(d => d.map(e => e.value))
            .entries(question.entities)
            .map(d => ({key:d.key, values: d.value}))
        this.props.dispatch({type: "SET_ALL_FILTERS", filters:aggregated})
        this.props.dispatch({type: "SET_CURRENT_QUESTION", question: question.question})
    }

    render() {
        const questions = _.orderBy(this.props.questions, "count", "desc").map( (question, i) =>
            <li 
                className="list-group-item question" 
                onClick={(event) => this.filter(question, event)} key={v4()} >
                {question.question}
            </li>
        );
        return (
            <Well className="Sidebar" >
                <h6>{this.props.current_language.top_filters}</h6>
                <ul className="list-group">
                  {questions}
                </ul>

            </Well>
        )
    }
}
