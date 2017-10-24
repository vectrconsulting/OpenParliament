import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Navbar, Row, Col, Button, Glyphicon } from 'react-bootstrap'
import { v4 } from 'uuid'
import request from 'superagent'
import { tour } from '../tour/Welcome'

@connect(state => ({ current_language: state.locale.current_language, languages: state.locale.languages.map(language => language.code) }))
export const Navigation = class Navigation extends Component {
    getLangOptions() {
        const onclick = (lang) => {
            this.props.dispatch({ type: 'SET_LANGUAGE', lang: lang })
            this.props.dispatch({ type: 'FETCH_PQ' })
            request.get('/pq?lang=' + lang).then(
                res => this.props.dispatch({ type: 'FETCH_PQ_SUCCES', items: res.body }),
                err => this.props.dispatch({ type: 'FETCH_PQ_ERROR', error: err })
            )
        }
        return this.props.languages.map(lang =>
            <Button key={v4()} bsStyle="link" onClick={() => onclick(lang)}>{lang}</Button>
        )
    }

    render() {
        return (
            <div className="Navigation">
                <Navbar>
                    <Row>
                        <Col md={9} xs={9}>
                            <Navbar.Header id="brand">
                                <Navbar.Brand >
                                    <a href="/ui/index.html" >
                                        {this.props.current_language.translation.header}<font className="BeBrand">.be</font>
                                    </a>
                                </Navbar.Brand>
                            </Navbar.Header>
                        </Col>
                        <Col md={3} xs={3} className="LanguageChooser" id="lang_choose">
                            {this.getLangOptions()}
                            <Button
                                id="help"
                                bsStyle="link"
                                onClick={() => this.props.dispatch({type:'START_TOUR'})}
                                >
                                <Glyphicon glyph="question-sign" />
                            </Button>
                        </Col>
                    </Row>
                </Navbar>
            </div>
        );
    }
};