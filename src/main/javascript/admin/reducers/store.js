import { createStore, combineReducers, applyMiddleware } from 'redux'

import locale from './localeReducer'
import http from './httpReducer'
import faqAdmin from './faqAdminReducer'

export const store = createStore(
    combineReducers({
        locale,
        http,
        faqAdmin
    }),
    window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()
)
