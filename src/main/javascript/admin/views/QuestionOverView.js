import React, { Component } from "react";
import { Well, Row, Col, Table, Button, Pagination } from "react-bootstrap";
import { connect } from "react-redux";
import { v4 } from 'uuid'

@connect(state => ({
  current_language: state.locale.current_language
}))
export const QuestionOverView = class QuestionOverView extends Component {
  componentWillMount() {
    this.props.dispatch({
      type: "FETCH_ALL_FAQ",
      next: this.props.dispatch,
      lang: this.props.current_language.code
    });
  }

  render() {
    return (
      <div className="PageBody">
        <Row>
          <Col lg={10} md={12} lgOffset={1}>
            <QuestionList />
          </Col>
        </Row>
      </div>
    );
  }
};

@connect(state => ({
  questions: state.faqAdmin.questions,
  current_language: state.locale.current_language.translation.admin_page
}))
class QuestionList extends Component {
  constructor(props) {
    super(props)
    this.state = {
      page: 1
    }
  }

  onSelect(eventKey) {
    this.setState({
      page: eventKey,
    });
  }

  render() {
    const number_of_pages = Math.ceil(this.props.questions.length / 20)
    const start_question = (this.state.page - 1) * 20;
    const end_question = Math.min((this.state.page) * 20, this.props.questions.length);
    const questions = this.props.questions
      .slice(start_question, end_question)
      .map(question => <QuestionItem key={v4()} question={question} />);
    return (
      <div height={800}>
        <Table striped bordered condensed hover>
          <thead>
            <tr>
              <th>{this.props.current_language.id}</th>
              <th>{this.props.current_language.count}</th>
              <th>{this.props.current_language.question}</th>
              <th>{this.props.current_language.public}</th>
            </tr>
          </thead>
          <tbody>
            {questions}
          </tbody>
        </Table><br />
        <center><Pagination
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
        /></center>
      </div>
    );
  }
}

@connect(state => ({
  current_language: state.locale.current_language.translation.admin_page
}))
class QuestionItem extends Component {
  changePublicButton() {
    const changePublic = () => this.props.dispatch({
      type: 'CHANGE_FAQ_PUBLIC',
      id: this.props.question.id,
      state: this.props.question.public ? false : true,
      next: this.props.dispatch
    })

    return (
      <Button bsStyle="link" onClick={changePublic}>
        {
          this.props.question.public ?
            this.props.current_language.make_hidden :
            this.props.current_language.make_public
        }
      </Button>
    );


  }

  render() {
    return (
      <tr>
        <td>{this.props.question.id}</td>
        <td>{this.props.question.count}</td>
        <td>{this.props.question.question}</td>
        <td>{this.changePublicButton()}</td>
      </tr>
    );
  }
}
