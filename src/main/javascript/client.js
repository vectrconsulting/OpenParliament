import React from 'react'
import { render } from 'react-dom'
import { createStore, combineReducers } from 'redux'
import { Provider } from 'react-redux'
import request from 'superagent'
import 'babel-polyfill' //ie11 support

import { store } from './app/reducers/store'
import { Page } from './app/views/Page'

store.dispatch({ type: 'FETCH_PQ' })
request.get('/pq?lang=' + store.getState().locale.current_language.code).then(
  res => store.dispatch({ type: 'FETCH_PQ_SUCCES', items: res.body }),
  err => store.dispatch({ type: 'FETCH_PQ_ERROR', error: err })
)
store.dispatch({ type: 'GET_FAQ', next: store.dispatch })

render(
  <Provider store={store}>
    <Page />
  </Provider>,
  document.getElementById("app")
)
