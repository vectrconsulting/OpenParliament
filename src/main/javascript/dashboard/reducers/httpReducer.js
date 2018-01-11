import request from 'superagent'

export default function reducer(state = {}, action) {
    switch (action.type) {
        case 'FETCH_PQ': {
            request.get('/pq').query({ lang: action.lang }).then(
                res => action.next({ type: 'FETCH_PQ_SUCCES', items: res.body }),
                err => action.next({ type: 'FETCH_PQ_ERROR', error: err })
            )
            return { ...state }
        }
        case 'GET_FILTERS_FROM_TEXT': {
            request.get("/questionfilter")
                .query({ q: action.question, lang: action.lang })
                .then(
                res => {
                    const filters = Object.keys(res.body).map(key => ({ key: key, values: res.body[key] }));
                    action.next({ type: 'SET_ALL_FILTERS', filters: filters })
                },
                err => console.log(err)
                )
            return { ...state }
        }
        case 'FETCH_FAQ': {
            request.get("/topquestions").query({ lang: action.lang })
                .then(
                res => action.next({ type: 'FETCH_FAQ_SUCCES', questions: res.body }),
                err => console.error(err)
                )
            return { ...state }
        }
        default:
            return { ...state }
    }
}
