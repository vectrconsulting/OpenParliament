import request from "superagent";

import { setColumnFilters } from "./filter"

import { SET_COLUMN_FILTER, GET_FILTERS_FROM_TEXT } from "./types";


export default function reducer(state = { question: "" }, action) {
    switch (action.type) {
        case GET_FILTERS_FROM_TEXT:
            return Object.assign({}, state, { question: action.search_string });

        case SET_COLUMN_FILTER:
            return Object.assign({}, state, { question: "" });

        default:
            return Object.assign({}, state);
    }
}

export function getFiltersFromText(search_string) {
    return function (dispatch, getState) {
        dispatch({ type: GET_FILTERS_FROM_TEXT, search_string: search_string })
        request.get("/questionfilter")
            .query({ q: search_string, lang: getState().locale.code })
            .then(
                res => dispatch(setColumnFilters(Object.keys(res.body).map(key => ({ columnName: key, values: res.body[key] }))))
            ).catch(err => console.error(err));
    }
}