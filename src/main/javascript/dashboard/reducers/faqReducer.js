export default function reducer(state = {
    questions: []
}, action) {
    switch (action.type) {
        case 'FETCH_FAQ_SUCCES':
            return {...state, questions: action.questions}
        default:
            return {...state}
    }
}
