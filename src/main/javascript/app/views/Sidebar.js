import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Well, Row, Col } from 'react-bootstrap'
import request from 'superagent'
import { v4 } from 'uuid'

import { store } from '../reducers/store'

@connect(state => ({ faq: state.faq.faq }))
export const Sidebar = class Sidebar extends Component {
    filter(i, event) {
        var filtermap = {}
        this.props.faq.questions[i].entities.map( (entity) => {
            if (filtermap[entity.type] == undefined) {
                filtermap[entity.type] = []
            }
            filtermap[entity.type].push(entity.value)
        })
        var filters = []
        for (var key in filtermap) {
            filters.push({key:key, values:filtermap[key]})
        }
        this.props.dispatch({type: "SET_ALL_FILTERS", filters:filters})
    }

    render() {
        const questions = this.props.faq.questions.map( (faq, i) =>
            <li className="list-group-item question" onClick={(event) => this.filter(i, event)} key={v4()} >{faq.question}</li>
        );
        return (
            <Well className="Sidebar" >
                <h4>{this.props.faq.sidebar_title}</h4>
                <ul className="list-group">
                  {questions}
                </ul>

            </Well>
        )
    }
}
