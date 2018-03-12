import _ from "lodash";
import * as d3 from "d3";
import request from "superagent";
import { List } from "immutable";

const partyColors = {
    ["N-VA"]: "gold",
    ["VB"]: "brown",
    ["Open Vld"]: "blue",
    ["SP.A"]: "red",
    ["Vuye&Wouters"]: "DimGray",
    ["CD&V"]: "orange",
    ["Ecolo-Groen"]: "green",
    ["MR"]: "blue",
    ["PS"]: "red",
    ["CDH"]: "orange",
    ["FDF"]: "pink",
    ["PTB-GO!"]: "red",
    ["DEFI"]: "GreenYellow",
    ["VUWO"]: "lightcyan",
    ["UNKN"]: "moccasin",
    ["default"]: "pink"
}
const threshold = 0.01;


import {
    FETCH_PATHS,
    FETCH_PATHS_ERROR,
    FETCH_PATHS_SUCCESS,
    FETCH_QUESTIONS,
    FETCH_QUESTIONS_ERROR,
    FETCH_QUESTIONS_SUCCESS
} from "./types";

const getHash = str => str.split('').reduce((hash, c) => ((hash << 5) - hash) + c.charCodeAt(0), 5381);
const getRandomColor = (str) => {
    const c = (getHash(str) & 0x00FFFFFF).toString(16).toUpperCase();
    return "#" + "00000".substring(0, 6 - c.length) + c
}
function computeColors(paths, columns, colors) {
    const new_colors = columns.map(function (column) {
        if (colors[column] !== undefined) return { [column]: colors[column] }
        else {
            const columnValues = _.uniq(paths.map(path => path[column]).concat(["default"]));
            const columnColors = columnValues.map(value => ({ [value]: getRandomColor(value) }))
            return { [column]: Object.assign({}, ...columnColors) }
        }
    });
    return Object.assign({}, ...new_colors);
}

export default function reducer(state = {
    paths: { items: List(), loading: false, error: undefined },
    questions: { items: List(), loading: false, error: null },
    columns: ["party", "author", "topic", "department"],
    colors: { party: partyColors }
}, action) {
    switch (action.type) {
        case FETCH_PATHS:
            return Object.assign({}, state, { paths: { items: List(), loading: true, error: undefined } });

        case FETCH_PATHS_ERROR:
            console.error(action.error);
            return Object.assign({}, state, { paths: { items: List(), loading: false, error: action.error } });

        case FETCH_PATHS_SUCCESS:
            const colors = computeColors(action.items, state.columns, { party: partyColors });
            return Object.assign({}, state, { paths: { items: List(action.items), loading: false, error: undefined } }, { colors });

        case FETCH_QUESTIONS:
            return Object.assign({}, state, { questions: { items: List(), loading: true, error: undefined } });

        case FETCH_QUESTIONS_ERROR:
            console.error(action.error);
            return Object.assign({}, state, { questions: { items: List(), loading: false, error: action.error } });

        case FETCH_QUESTIONS_SUCCESS:
            return Object.assign({}, state, { questions: { items: List(action.items), loading: false, error: undefined } });

        default:
            return Object.assign({}, state);
    }
}


export function fetchPaths() {
    return function (dispatch, getState) {
        dispatch({ type: FETCH_PATHS });
        request.get("/paths")
            .query({ lang: getState().locale.code })
            .then((response) => dispatch({ type: FETCH_PATHS_SUCCESS, items: response.body }))
            .catch(error => {
                console.log(error);
                dispatch({ type: FETCH_PATHS_ERROR, error: error });
            })
    }
}

export function fetchQuestions() {
    return function (dispatch, getState) {
        dispatch({ type: FETCH_QUESTIONS });
        request.get("/questions")
            .query({ lang: getState().locale.code })
            .then(response => dispatch({ type: FETCH_QUESTIONS_SUCCESS, items: response.body }))
            .catch(error => dispatch({ type: FETCH_QUESTIONS_ERROR, error: error }))
    }
}

export function filterLowOccurences(paths, columns, otherKeyword) {
    function notAllowed(paths, column) {
        const nested = d3.nest()
            .key(d => d[column])
            .rollup(d => d.reduce((acc, row) => acc + row.question_count, 0))
            .entries(paths);
        return [column, nested.filter(({ value }) => value / total < threshold).map(({ key }) => key)]
    }

    function pathMapper(path, notAllowedValues, otherKeyword) {
        function pathOk(path, notAllowedValues) {
            for (const [key, value] of notAllowedValues.entries()) {
                if (value.includes(path[key])) return false
            }
            return true
        }

        if (pathOk(path, notAllowedValues)) return path;
        else {
            const withOther = Array.from(notAllowedValues.entries()).map(([key, value]) => {
                if (value.includes(path[key])) return { [key]: otherKeyword };
                else return { [key]: path[key] }
            })
            return Object.assign({}, path, ...withOther);
        }
    }

    const total = paths.map(path => path.question_count).reduce((x, y) => x + y, 0);
    const notAllowedValues = new Map(columns.map(column => notAllowed(paths, column)));
    return paths.map(path => pathMapper(path, notAllowedValues, otherKeyword));



}