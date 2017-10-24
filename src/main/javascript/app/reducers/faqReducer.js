export default function reducer(state = {
    faq: { questions: [], sidebar_title: "" }
}, action) {
    switch (action.type) {
        case 'SET_FAQ':
            return {...state, faq: action.faq}
        default:
            return {...state}
    }
}
