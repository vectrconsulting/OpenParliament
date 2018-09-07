import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import Select from "react-select";
import DatePicker from "react-datepicker";
import Slider from "rc-slider";
import moment from "moment";
import { extendMoment } from "moment-range";

import { setColumnFilter, setDateFilter } from "../../reducers/filter";

export class Filters extends Component {

    columnFilter(columnName, values) {
        this.props.setColumnFilter({
            columnName: columnName,
            values: values.map(d => d.value)
        });
    }

    getColumnFilters() {
        return this.props.column_options.map(column => {
            const filter = _.find(this.props.columns, { columnName: column.columnName }) || { values: [] };
            const compare = (column.columnName == "author" || column.columnName == "department")?
                (a,b) => a.split(/ (.+)/)[1].toLowerCase().localeCompare(b.split(/ (.+)/)[1].toLowerCase()):
                (a,b) => a.localeCompare(b);

            return (
                <div className="col-md" key={`${column.columnName}filter`}>
                    <Select
                        placeholder={this.props.translations[column.columnName]}
                        multi={true}
                        options={
                            column.options
                                .sort(compare)
                                .map(d => ({ value: d, label: d }))
                        }
                        onChange={val => this.columnFilter(column.columnName, val)}
                        value={filter.values.map(d => ({ value: d, label: d }))}
                    />
                </div>
            );
        })
    }

    dateFilter(start, end) {
        this.props.setDateFilter({ start, end })
    }

    getDateFilter() {
        const extended_moment = extendMoment(moment);
        const range = Array.from(extended_moment
            .range(moment(this.props.date_options.min), moment(this.props.date_options.max))
            .by("day"))
            .map(day => day.format("YYYY-MM-DD"));

        const createSliderWithTooltip = Slider.createSliderWithTooltip;
        const Range = createSliderWithTooltip(Slider.Range);
        return (
            <div className="row" style={{ width: "100%", marginLeft: 0, marginRight: 0, paddingTop: 10 }}>
                <div className="col-md-3" style={{ textAlign: "center" }}>
                    <DatePicker
                        customInput={<DatePickerInput />}
                        dateFormat="YYYY-MM-DD"
                        selected={moment(this.props.dates.start || this.props.date_options.min)}
                        minDate={moment(this.props.date_options.min)}
                        maxDate={moment(this.props.date_options.max)}
                        onChange={(date) => this.dateFilter(date.format("YYYY-MM-DD"), this.props.dates.end || this.props.date_options.max)}
                    />
                </div>
                <div className="col-md-6">
                    <Range
                        min={0}
                        max={range.length - 1}
                        defaultValue={[range.findIndex(item => item === (this.props.dates.start || this.props.date_options.min)), range.findIndex(item => item === (this.props.dates.end || this.props.date_options.max))]}
                        onAfterChange={(indexes) => this.dateFilter(range[indexes[0]], range[indexes[1]])}
                        tipFormatter={index => moment(range[index]).format("DD-MM-YYYY")}
                    />
                </div>
                <div className="col-md-3" style={{ textAlign: "center" }}>
                    <DatePicker
                        customInput={<DatePickerInput />}
                        dateFormat="YYYY-MM-DD"
                        selected={moment(this.props.dates.end || this.props.date_options.max)}
                        minDate={moment(this.props.date_options.min)}
                        maxDate={moment(this.props.date_options.max)}
                        onChange={(date) => this.dateFilter(this.props.dates.start || this.props.date_options.min, date.format("YYYY-MM-DD"))}
                    />
                </div>
            </div>
        );
    }

    render() {
        return (
            <div className="opendata-filters">
                <div className="row" style={{ width: "100%", marginLeft: 0, marginRight: 0 }}>
                    {this.getColumnFilters()}
                </div>
                {this.getDateFilter()}
            </div>
        )
    }
}

class DatePickerInput extends Component {
    render() {
        return (
            <input
                className="form-control"
                style={{ width: "100%" }}
                onClick={this.props.onClick}
                value={moment(this.props.value).format("DD-MM-YYYY")}
                readOnly={true}
            />
        );
    }
}


export default connect(
    state => ({
        column_options: state.filter.column_options,
        columns: state.filter.columns,
        date_options: state.filter.date_options,
        dates: state.filter.dates,
        translations: state.locale.translation.columns
    }),
    dispatch => bindActionCreators({ setColumnFilter, setDateFilter }, dispatch)
)(Filters)