import { createStore, combineReducers, applyMiddleware } from 'redux'
import promiseMiddleware from 'redux-promise'

import filter from './filterReducer'
import pq from './pqReducer'
import locale from './localeReducer'
import http from './httpReducer'
import faq from './faqReducer'
import tour from './tourReducer'

export const store = createStore(
    combineReducers({
        locale,
        filter,
        pq,
        http,
        faq,
        tour
    }),
    window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()
)
