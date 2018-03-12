import { nl_lang } from '../locale/nl_lang';
import { fr_lang } from '../locale/fr_lang';

import { fetchPaths, fetchQuestions } from "./data";
import { SET_LANGUAGE } from "./types";

const languages = new Map([
    [nl_lang.code, nl_lang],
    [fr_lang.code, fr_lang]
])

/** 
 * Reads the language of the browser to set the default
 * If the browser is set to french, the default language is french
 * In all other cases we use dutch.
 */
function defaultLanguage() {
    if ((window.navigator.userLanguage || window.navigator.language).substring(0, 2) === 'fr')
        return fr_lang;
    else
        return nl_lang;
}

export default function reducer(state = defaultLanguage(), action) {
    switch (action.type) {
        case SET_LANGUAGE:
            return Object.assign({}, state, languages.get(action.lang));

        default:
            return Object.assign({}, state);
    }
}

export function setLocaleOnly(lang) {
    return function (dispatch, getState) {
        if (lang !== getState().locale.code)
            dispatch({ type: SET_LANGUAGE, lang: lang });
    }
}

export function setLocale(lang, override = false) {
    return function (dispatch, getState) {
        if (lang !== getState().locale.code || override) {
            dispatch({ type: SET_LANGUAGE, lang: lang });
            dispatch(fetchPaths());
            dispatch(fetchQuestions());
        }
    }
}
