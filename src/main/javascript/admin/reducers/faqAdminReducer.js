import _ from 'lodash'

export default function reducer(state = {questions: []}, action) { 
    switch (action.type) {
        case 'FETCH_ALL_FAQ_SUCCES': {
            return { ...state, questions: action.questions }
        }
        case 'UPDATE_FAQ_PUBLIC': {
            const new_questions = _.cloneDeep(state.questions)
            _.find(new_questions, d => d.id===action.id).public = action.state;
            return {... state, questions:new_questions}
        }
        default: return { ...state }
    }
}