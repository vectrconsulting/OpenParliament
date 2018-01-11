/*
filters: [{key: columnname, values:[list of possible values]}]
*/

export default function reducer(state = {
    loading: false,
    column: [],
    date: [],
    current_question: ""
}, action) {
    switch (action.type) {
        case 'SET_COLUMN_FILTER':
            return  { ...state, current_question: "", column: state.column.filter(el => el.key !== action.filter.key).concat([action.filter]), loading: false };
        case 'SET_ALL_FILTERS': {
            return { ...state, current_question: "", column: action.filters, loading: false }
        }
        case 'GET_FILTERS_FROM_TEXT':
            return { ...state, loading: true }

        case 'SET_DATE_FILTER':
            return { ...state, date: action.date }

        case 'SET_CURRENT_QUESTION':
            return { ...state, current_question: action.question }

        default:
            return { ...state }
    }
}
