import request from 'superagent'

export default function reducer(state = {}, action) {
    switch (action.type) {
        case 'FETCH_ALL_FAQ': {
            request.get("/allfilters").query({ lang: action.lang }).then(
                res => action.next({ type: 'FETCH_ALL_FAQ_SUCCES', questions: res.body }),
                err => console.error(err)
            )
            return { ...state }
        }
        case 'CHANGE_FAQ_PUBLIC': {
            request.post("/updateFilter").query({ id: action.id, public: action.state }).then(
                res => action.next({ type: 'UPDATE_FAQ_PUBLIC', id: action.id, state: action.state }),
                err => console.error(err)
            )
            return { ...state }
        }
        default:
            return { ...state }
    }
}
