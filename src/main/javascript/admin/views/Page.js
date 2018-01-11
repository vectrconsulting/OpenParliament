import React, { Component } from 'react'
import { Well, Row, Col } from 'react-bootstrap'
import { connect } from 'react-redux'

import { Navigation } from './Navigation'
import { Footer } from './Footer'
import { QuestionOverView } from './QuestionOverView'



@connect()
export const Page = class Page extends Component {
    render() {
        return (
            <div className="Page">
                <Navigation />
                <QuestionOverView />
                <Footer />
            </div>
        );
    }
};
