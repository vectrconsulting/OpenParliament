import React, { Component } from 'react'
import { Well, Row, Col, Table, Pagination, Modal, Button, Glyphicon } from 'react-bootstrap'
import { connect } from 'react-redux'
import { v4 } from 'uuid'
import _ from 'lodash'

@connect(state => ({
    questions: state.pq.data.items,
    current_language: state.locale.current_language.translation,
    columns: state.pq.columns.items,
    column_filters: state.filter.column,
    date_filters: state.filter.date
}))
export const QuestionOverview = class QuestionOverview extends Component {
    constructor(props) {
        super(props);
        this.state = { page: 1 };
        this.pagesize = 20;
    }

    onSelect(eventKey) {
        this.setState({ page: eventKey });
    }

    /**
     * Filters the data based on the column and date filters
     */
    filterData(data) {
        const filter_row = (row) => {
            const start_date = this.props.date_filters[0]
            const end_date = this.props.date_filters[1]
            if (start_date > row.date || row.date > end_date) return false
            return _.filter(this.props.column_filters, filter => _.includes(this.props.columns, filter.key))
                .map(filter => (filter.values.length && !_.includes(filter.values, row[filter.key])) ? false : true)
                .reduce((l, r) => l && r, true);
        }
        return _.filter(data, d => filter_row(d))
    }

    orderData(data) {
        return _.orderBy(data, 'date', 'desc');
    }

    render() {
        const filtered_questions = this.filterData(this.props.questions);

        const number_of_pages = Math.ceil(filtered_questions.length / this.pagesize)
        const start_question = (this.state.page - 1) * this.pagesize;
        const end_question = Math.min((this.state.page) * this.pagesize, filtered_questions.length);
        const questions = this.orderData(filtered_questions)
            .slice(start_question, end_question)
            .map(question => <Question key={v4()} question={question} />);
        return (
            <Well>
                <Table striped bordered condensed hover>
                    <thead>
                        <tr>
                            <th>{this.props.current_language.columns.party}</th>
                            <th>{this.props.current_language.columns.author}</th>
                            <th>{this.props.current_language.columns.topic}</th>
                            <th>{this.props.current_language.columns.department}</th>
                            <th>{this.props.current_language.columns.title}</th>
                            <th>{this.props.current_language.columns.date}</th>
                        </tr>
                    </thead>
                    <tbody>
                        {questions}
                    </tbody>
                </Table>
                <center>
                    <Pagination
                        prev
                        next
                        first
                        last
                        ellipsis
                        boundaryLinks
                        items={number_of_pages}
                        maxButtons={5}
                        activePage={this.state.page}
                        onSelect={this.onSelect.bind(this)}
                    />
                </center>
            </Well>
        );
    }
}

@connect(state => ({
    current_language: state.locale.current_language.translation
}))
class Question extends Component {
    constructor(props) {
        super(props);
        this.state = { showModal: false };
    }
    openModal() {
        this.setState({ showModal: true });
    }
    closeModal() {
        this.setState({ showModal: false })
    }

    render() {
        return (
            <tr onClick={this.openModal.bind(this)} style={{cursor: "pointer"}}>
                <td>{this.props.question.party}</td>
                <td>{this.props.question.author}</td>
                <td>{this.props.question.topic}</td>
                <td>{this.props.question.department}</td>
                <td>{this.props.question.title}</td>
                <td>{this.props.question.date}</td>
                <Modal show={this.state.showModal} onHide={this.closeModal.bind(this)} bsSize="large">
                    <Modal.Body>
                        <div><h6>{this.props.current_language.columns.party}:</h6> {this.props.question.party}</div>
                        <div><h6>{this.props.current_language.columns.author}:</h6> {this.props.question.author}</div>
                        <div><h6>{this.props.current_language.columns.topic}:</h6> {this.props.question.topic}</div>
                        <div><h6>{this.props.current_language.columns.department}:</h6> {this.props.question.department}</div>
                        <div><h6>{this.props.current_language.columns.department2}:</h6> {this.props.question.department_long}</div>
                        <div><h6>{this.props.current_language.columns.title}:</h6> {this.props.question.title}</div>
                        <div><h6>{this.props.current_language.columns.date}:</h6> {this.props.question.date}</div>
                        <div><h6>{this.props.current_language.columns.question}:</h6> {this.props.question.question}</div>
                        <div><h6>{this.props.current_language.columns.answer}:</h6> {this.props.question.answer}</div>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.closeModal.bind(this)}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </tr>
        );
    }
}