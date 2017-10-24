import request from 'superagent'

export default function reducer(state = {}, action) {
    switch (action.type) {
        case 'GET_FILTERS_FROM_TEXT':
            request.get("/questionfilter?q=" + action.question).then(
                res => {
                    const entities = JSON.parse(res.text)
                    const filters = Object.keys(entities).map(key => ({ key: key, values: entities[key] }));
                    action.next({ type: 'SET_ALL_FILTERS', filters: filters })
                },
                err => console.log(err)
            )
            return {...state}
        case 'GET_FAQ':
            request.get("/topquestions").then(
                res => {
                    const questions = JSON.parse(res.text)
                    action.next({ type: 'SET_FAQ', faq: questions })
                },
                err => console.log(err)
            )
            return {...state}
        default:
            return {...state}
    }
}
