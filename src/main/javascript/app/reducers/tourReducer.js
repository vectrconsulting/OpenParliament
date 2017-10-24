export default function reducer(state = {
    running: false
}, action) {
    switch (action.type) {
        case 'START_TOUR': {
            return { ...state, running: true }
        }
        case 'STOP_TOUR': {
            return { ...state, running: false }
        }
        default:
            return state
    }
}