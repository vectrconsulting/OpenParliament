import _ from 'lodash'
import { nl_lang } from '../locale/nl_lang'
import { fr_lang } from '../locale/fr_lang'

export default function reducer(state = {
    languages: [nl_lang, fr_lang],
    current_language: (window.navigator.userLanguage || window.navigator.language).substring(0, 2) === 'fr' ? fr_lang : nl_lang
}, action) {
    switch (action.type) {
        case "ADD_LANGUAGE": {
            return { ...state, languages: state.languages.concat([action.payload]) }
        }
        case "SET_LANGUAGE": {
            if (action.lang == state.current_language.code) return state;
            return { ...state, current_language: _.find(state.languages, lang => lang.code === action.lang) };
        }
        default: return {...state}
    }
}