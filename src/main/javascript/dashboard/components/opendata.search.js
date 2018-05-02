import React, {Component} from "react";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";

import {getFiltersFromText} from "../reducers/search";


export class Search extends Component {
    constructor(props) {
        super(props);
        this.state = {value: ""};
    }

    search() {
        this.props.getFiltersFromText(this.state.value);
    }

    render() {
        return (
            <div className="opendata-search row justify-content-center" style={{paddingTop: 10}}>
                <div className="col-md-11 col-lg-10">
                    <div className="card opendata-search-card border-0">
                        <div className="row" style={{width: "100%"}}>
                            <div className="col">
                                <input
                                    className="form-control"
                                    placeholder={this.props.example}
                                    value={this.state.value}
                                    disabled={this.props.loading}
                                    onChange={event => this.setState({value: event.target.value})}
                                    onKeyPress={event => {
                                        if (event.key === "Enter") this.search()
                                    }}
                                />
                            </div>
                            <div className="col-sm-1" style={{float: "right"}}>
                                <button
                                    className="btn btn-secondary"
                                    disabled={this.props.loading}
                                    onClick={() => this.search()}>
                                    {this.props.search}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default connect(
    state => ({
        search: state.locale.translation.search,
        example: state.locale.translation.example_question,
        question: state.search.question,
        loading: state.data.questions.loading,
    }),
    dispatch => bindActionCreators({getFiltersFromText}, dispatch)
)(Search)