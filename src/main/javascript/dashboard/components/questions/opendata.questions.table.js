import React, {Component} from "react";
import {connect} from "react-redux";
import moment from "moment";
import Pagination from "rc-pagination";
import Modal from "react-bootstrap4-modal";

import {filterData} from "../../reducers/filter";

export class QuestionTable extends Component {
    constructor(props) {
        super(props);
        this.state = {
            page: 0,
            items_per_page: 10,
            show_modal: false,
            data_for_modal: undefined
        }
    }


    header() {
        const columns = this.props.columns
            .map(column => (
                <th key={`opendate-table-header-${column}`}>
                    {this.props.columnTranslations[column]}
                </th>
            ));
        return (
            <thead>
            <tr>
                {columns}
            </tr>
            </thead>
        )
    }

    body() {
        const rows = this.props.questions
            .slice(this.state.page * this.state.items_per_page, (this.state.page + 1) * this.state.items_per_page)
            .map((row, index) => (
                <tr key={`opendata-table-row-${index}`}
                    onClick={() => this.setState({show_modal: true, data_for_modal: row})}>
                    {
                        this.props.columns.map(column => (
                            <td key={`opendata-table-row-${index}-${column}`}>
                                {row[column]}
                            </td>
                        ))
                    }
                </tr>
            )).toArray();

        return (
            <tbody>
            {rows}
            </tbody>
        );
    }

    pages() {
        return (
            <div style={{textAlign: "center"}}>
                <Pagination className="ant-pagination" style={{display: "inline-block"}}
                            current={this.state.page + 1}
                            total={this.props.questions.count()}
                            pageSize={this.state.items_per_page}
                            showLessItems={true}
                            onChange={(current, size) => this.setState({page: current - 1, items_per_page: size})}
                />
            </div>
        )
    }

    modal() {
        if (this.state.show_modal)
            return (
                <Modal visible={true}
                       onClickBackdrop={() => this.setState({show_modal: false, data_for_modal: undefined})}>
                    <div className="modal-header">
                        <h5 className="modal-title">
                            {this.state.data_for_modal.title}
                        </h5>
                        <button type="button" className="close"
                                onClick={() => this.setState({show_modal: false, data_for_modal: undefined})}>
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div className="modal-body">
                        <table className="table">
                            <tbody>
                            <tr>
                                <td style={{valign: "top"}}>{this.props.columnTranslations["party"]}</td>
                                <td>{this.state.data_for_modal.party}</td>
                            </tr>
                            <tr>
                                <td style={{valign: "top"}}>{this.props.columnTranslations["author"]}</td>
                                <td>{this.state.data_for_modal.author}</td>
                            </tr>
                            <tr>
                                <td style={{valign: "top"}}>{this.props.columnTranslations["topic"]}</td>
                                <td>{this.state.data_for_modal.topic}</td>
                            </tr>
                            <tr>
                                <td style={{valign: "top"}}>{this.props.columnTranslations["department"]}</td>
                                <td>{this.state.data_for_modal.department}</td>
                            </tr>
                            <tr>
                                <td style={{valign: "top"}}>{this.props.columnTranslations["date"]}</td>
                                <td>{this.state.data_for_modal.date}</td>
                            </tr>
                            <tr>
                                <td style={{valign: "top"}}>{this.props.columnTranslations["question"]}</td>
                                <td>{this.state.data_for_modal.question}</td>
                            </tr>
                            <tr>
                                <td style={{valign: "top"}}>{this.props.columnTranslations["answer"]}</td>
                                <td>{this.state.data_for_modal.answer}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary"
                                onClick={() => this.setState({show_modal: false, data_for_modal: undefined})}>
                            {this.props.close}
                        </button>
                    </div>
                </Modal>
            )
    }

    render() {
        return (
            <div className="opendata-question-table">
                <table className="table table-hover">
                    {this.header()}
                    {this.body()}
                </table>
                {this.pages()}
                {this.modal()}
            </div>
        )
    }
}

export default connect(
    state => ({
        questions: filterData(state.data.questions.items, state.filter.columns, state.filter.dates, state.search.question)
            .sortBy(question => question.date)
            .reverse(),
        columns: state.data.columns.concat(["title", "date"]),
        columnTranslations: state.locale.translation.columns,
        columnFilters: state.filter.columns,
        dateFilters: state.filter.dates,
        close: state.locale.translation.tour_buttons.close
    })
)(QuestionTable)