/*
filters: [{key: columnname, values:[list of possible values]}]
*/

export default function reducer(state = {
    loading: false,
    column: [],
    date: []
}, action) {
    switch (action.type) {
        case 'SET_COLUMN_FILTER':
            return { ...state, column: state.column.filter(el => el.key !== action.filter.key).concat([action.filter]), loading: false }
        case 'SET_ALL_FILTERS': {
            return { ...state, column: action.filters, loading: false }
        }
        case 'GET_FILTERS_FROM_TEXT':
            return { ...state, loading: true }

        case 'SET_DATE_FILTER':
            return {...state, date: action.date}

        default:
            return {...state}
    }
}
