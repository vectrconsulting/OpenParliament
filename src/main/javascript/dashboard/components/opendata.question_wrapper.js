import React, { Component } from "react";
import { connect } from "react-redux";

import QuestionTable from "./questions/opendata.questions.table";

export class QuestionWrapper extends Component {
    body() {
        if (this.props.data_loading) return (<div>Loading...</div>);
        else return (<QuestionTable />);

    }

    render() {
        return (
            <div className="opendata-question-wrapper row justify-content-center" style={{ paddingTop: 10, paddingBottom: 10 }}>
                <div className="col-lg-11 col-xl-10">
                    <div className="card opendata-question-card border-0">
                        {this.body()}
                    </div>
                </div>
            </div>
        );
    }
}

export default connect(
    state => ({ data_loading: state.data.questions.loading })
)(QuestionWrapper)