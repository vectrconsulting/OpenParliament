import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Row, Col } from 'react-bootstrap'
import Slider from 'rc-slider'
import Moment from 'moment'
import { extendMoment } from 'moment-range'
import dateFormat from 'dateformat'
import DatePicker from 'react-datepicker';
import _ from 'lodash'

@connect(state => ({
    data: state.pq.data.items,
    columns: state.pq.columns.items,
    column_colors: state.pq.colors.items,
    column_filters: state.filter.column,
    date_filters: state.filter.date,
    current_language: state.locale.current_language.translation
}))
export const DateFilter = class DateFilter extends Component {
    constructor(props) {
        super(props);
        this.minimum_date = _.minBy(this.props.data, 'date').date
        this.maximum_date = _.maxBy(this.props.data, 'date').date
        const moment = extendMoment(Moment);
        this.range = moment.range(new Date(this.minimum_date), new Date(this.maximum_date))
        this.days = Array.from(this.range.by('day')).map(d => d._d).map(d => dateFormat(d, "yyyy-mm-dd"));
    }
    writeFilterToRedux(values) {
        this.props.dispatch({ type: 'SET_DATE_FILTER', date: values.map(value => this.days[value]) })
    }

    handleDatePicker(value) {
        this.props.dispatch({ type: 'SET_DATE_FILTER', date: value.map(d => dateFormat(d, "yyyy-mm-dd")) });
    }
    /**
     * Returns a slider to adjust the date range of the data
     */
    getDateSlider() {
        const min = 0;
        const max = this.days.length - 1;
        const default_min = this.props.date_filters.length ? _.indexOf(this.days, _.min(this.props.date_filters)) : min;
        const default_max = this.props.date_filters.length ? _.indexOf(this.days, _.max(this.props.date_filters)) : max;

        const createSliderWithTooltip = Slider.createSliderWithTooltip;
        const Range = createSliderWithTooltip(Slider.Range);
        return <div id="date_filter">
            <Range
                min={min} max={max}
                defaultValue={[default_min, default_max]}
                onAfterChange={value => this.writeFilterToRedux(value)}
                tipFormatter={value => this.days[value]} />
        </div>

    }
    render() {
        const start_date = Moment(this.props.date_filters.length ? _.min(this.props.date_filters) : this.minimum_date);
        const end_date = Moment(this.props.date_filters.length ? _.max(this.props.date_filters) : this.maximum_date);
        const selectable_date = date => {
            return this.range.contains(date);
        }
        return <Row id="date_filter">
            <Col xs={3}>
                <center>
                    <DatePicker
                        dateFormat="YYYY-MM-DD"
                        selected= { start_date }
                        selectsStart
                        startDate={start_date}
                        endDate={end_date}
                        onChange={new_date => this.handleDatePicker([new_date, end_date])}
                        filterDate={selectable_date} />
                </center>
            </Col>
            <Col xs={6}>
                {this.getDateSlider()}
            </Col>
            <Col xs={3}>
                <center>
                    <DatePicker
                        dateFormat="YYYY-MM-DD"
                        selected={end_date}
                        selectsEnd
                        startDate={start_date}
                        endDate={end_date}
                        onChange={new_date => this.handleDatePicker([start_date, new_date])}
                        filterDate={selectable_date} />
                </center>
            </Col>
        </Row>
    }
}