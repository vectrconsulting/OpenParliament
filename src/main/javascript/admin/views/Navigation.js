import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Navbar, Row, Col, Button, Glyphicon } from 'react-bootstrap'
import { v4 } from 'uuid'
import request from 'superagent'

@connect(state => ({ current_language: state.locale.current_language }))
export const Navigation = class Navigation extends Component {
    render() {
        return (
            <div className="Navigation">
                <Navbar>
                    <Row>
                        <Col md={9} xs={9}>
                            <Navbar.Header id="brand">
                                <Navbar.Brand >
                                    <a href="/ui" >
                                        {this.props.current_language.translation.header}<font className="BeBrand">.be</font>
                                    </a>
                                </Navbar.Brand>
                            </Navbar.Header>
                        </Col>
                        <Col md={3} xs={3} className="LanguageChooser" id="lang_choose">
                            <LangOptions />
                        </Col>
                    </Row>

                </Navbar>
            </div>
        );
    }
};

@connect(state => ({ languages: state.locale.languages.map(language => language.code) }))
class LangOptions extends Component {
    render() {
        const onclick = (lang) => {
            this.props.dispatch({ type: 'SET_LANGUAGE', lang: lang })
            this.props.dispatch({ type: 'SET_ALL_FILTERS', filters: [] })
            this.props.dispatch({ type: 'FETCH_FAQ', next: this.props.dispatch, lang: lang })
            this.props.dispatch({ type: 'FETCH_PQ', next: this.props.dispatch, lang: lang })
            this.props.dispatch({ type: 'FETCH_ALL_FAQ', next: this.props.dispatch, lang: lang })
        }
        return (
            <div>
                {this.props.languages.map(lang => <Button key={v4()} bsStyle="link" onClick={() => onclick(lang)}>{lang}</Button>)}
            </div>
        );
    }
}
