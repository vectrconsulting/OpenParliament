import React, {Component} from "react";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import Select from "react-select";
import ContainerDimensions from "react-container-dimensions";
import {setColumnFilter, filterData} from "../../reducers/filter";
import {filterLowOccurences} from "../../reducers/data";
import Npartite from "./vectr.consulting.npartite";

export class GraphNpartite extends Component {
    constructor(props) {
        super(props);
        this.state = {
            columns: this.props.columns
        }
    }

    limitColumn(columns) {
        this.setState({
            columns: columns.map(column => column.value)
        })
    }

    columnFilter(columnName, value) {
        this.props.setColumnFilter({
            columnName: columnName,
            values: [value]
        });
    }

    getNpartite() {
        if (this.state.columns.length !== 0) {
            const filtered_low_occ = filterLowOccurences(this.props.data, this.props.columns, this.props.otherKeyword);
            const npartite_data = filtered_low_occ.map(row => Object.assign(
                {},
                row,
                {color: this.props.colors[this.state.columns[0]][row[this.state.columns[0]]] || "#989898"}
            ));
            return (
                <ContainerDimensions>
                    {({width}) =>
                        <Npartite
                            width={width - 30}
                            height={800}
                            data={npartite_data}
                            columns={this.state.columns}
                            colorKey={"color"}
                            weightKey={"question_count"}
                            otherKey={this.props.otherKeyword}
                            onClick={(column, value) => {
                                if (value !== this.props.otherKeyword) this.columnFilter(column, value)
                            }}
                            total={this.props.total}
                        />
                    }
                </ContainerDimensions>
            );
        }
    }

    render() {

        return (
            <div className="opendata-graph-npartite">
                <div className="row" style={{paddingTop: 20}}>
                    <div className="col">
                        <Select
                            multi={true}
                            options={this.props.columns.map(column => ({value: column, label: column}))}
                            onChange={columns => this.limitColumn(columns)}
                            value={this.state.columns.map(column => ({value: column, label: column}))}
                        />
                    </div>
                </div>
                <div className="row" style={{paddingTop: 20}}>
                    <div className="col">
                        {this.getNpartite()}
                    </div>
                </div>
            </div>
        );
    }
}

export default connect(
    state => ({
        data: filterData(state.data.paths.items, state.filter.columns, state.filter.dates, state.search.question).toArray(),
        total: state.data.paths.items.count(),
        colors: state.data.colors,
        columns: state.data.columns,
        party: state.locale.translation.columns.party,
        author: state.locale.translation.columns.author,
        topic: state.locale.translation.columns.topic,
        department: state.locale.translation.columns.department,
        otherKeyword: state.locale.translation.other,
    }),
    dispatch => bindActionCreators({setColumnFilter}, dispatch)
)(GraphNpartite)