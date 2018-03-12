import _ from "lodash";
import {
    SET_COLUMN_FILTER,
    SET_ALL_FILTERS,
    SET_DATE_FILTER,
    FETCH_PATHS_SUCCESS,
    SET_LANGUAGE
} from "./types";

const columnNames = ["party", "author", "topic", "department"];

const getColumnOptions = (data, filters, dates) => {

    const filter_func = (row, columnName) => {
        if ((dates.start && row.date < dates.start) || (dates.end && dates.end < row.date)) return false;
        for (const filter of filters) {
            if (filter.columnName !== columnName && !filter.values.includes(row[filter.columnName]))
                return false;
        }

        return true;
    }

    const options = columnNames.map(columnName => ({
        columnName: columnName,
        options: _(data)
            .filter(row => filter_func(row, columnName))
            .map(item => item[columnName]).uniq().value()
    }));
    return options;
}

const getDateMinMax = (data) => {
    const min = _.minBy(data, "date").date;
    const max = _.maxBy(data, "date").date;
    return { min: min, max: max };
}

export default function reducer(state = {
    data: [], // contains the path data
    columns: [], // contains all the current column filters
    column_options: [], // contains all the options with the current filters
    dates: { start: undefined, end: undefined }, //contains the current date filters
    date_options: { min: undefined, max: undefined }
}, action) {
    switch (action.type) {
        case SET_LANGUAGE: {
            return Object.assign({}, state, { data: [], column_options: [], columns: [] });
        }

        case SET_COLUMN_FILTER: {
            const columns = action.filter.values.length !== 0 ?
                state.columns
                    .filter(filter => filter.columnName !== action.filter.columnName)
                    .concat([action.filter]) :
                state.columns
                    .filter(filter => filter.columnName !== action.filter.columnName);

            const column_options = getColumnOptions(state.data, columns, state.dates);
            return Object.assign({}, state, { column_options, columns: columns });
        }

        case SET_ALL_FILTERS: {
            const filters = action.filters
                .filter(filter => filter.values.length !== 0);
            const column_options = getColumnOptions(state.data, filters, state.dates);
            return Object.assign({}, state, { column_options, columns: filters });
        }

        case SET_DATE_FILTER: {
            const column_options = getColumnOptions(state.data, state.columns, action.dates);
            return Object.assign({}, state, { column_options, dates: action.dates });
        }

        case FETCH_PATHS_SUCCESS: {
            const date_options = getDateMinMax(action.items);
            const column_options = getColumnOptions(action.items, state.columns, state.dates);
            return Object.assign({}, state, { data: action.items, column_options, date_options });
        }
        default:
            return Object.assign({}, state);
    }
}

export const setColumnFilters = (filters) => ({ type: SET_ALL_FILTERS, filters: filters });

export const setColumnFilter = (filter) => ({ type: SET_COLUMN_FILTER, filter: filter });

export const setDateFilter = (dates) => ({ type: SET_DATE_FILTER, dates: dates });

export const filterData = (data, columnFilters, dateFilters) => {
    const filter_func = (row) => {
        if (row.date < dateFilters.start || dateFilters.end < row.date)
            return false;
        for (const filter of columnFilters) {
            if (!filter.values.includes(row[filter.columnName]))
                return false;
        }
        return true;
    }
    return data.filter(filter_func);
}
