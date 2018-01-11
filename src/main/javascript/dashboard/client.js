import React from 'react'
import { render } from 'react-dom'
import { createStore, combineReducers } from 'redux'
import { Provider } from 'react-redux'
import request from 'superagent'
import 'babel-polyfill' //ie11 support

import { store } from './reducers/store'
import { Page } from './views/Page'

store.dispatch({ type: 'FETCH_PQ', lang: store.getState().locale.current_language.code, next: store.dispatch })
store.dispatch({ type: 'FETCH_FAQ', lang: store.getState().locale.current_language.code, next: store.dispatch })

var url = new URL(window.location.href);
if (url.searchParams.has("lang")) {
  const lang = url.searchParams.get("lang");
  if (lang) {
    store.dispatch({ type: "SET_LANGUAGE", lang: lang })
  }
}
if (url.searchParams.has("columns")) {
  const columns = JSON.parse(url.searchParams.get("columns"));
  if (columns.length) {
    store.dispatch({ type: "SET_ALL_FILTERS", filters: columns })
  }
}
if (url.searchParams.has("date")) {
  const date = JSON.parse(url.searchParams.get("date"));
  if (date.length) {
    store.dispatch({ type: "SET_DATE_FILTER", date: date })
  }
}

render(
  <Provider store={store}>
    <Page />
  </Provider>,
  document.getElementById("app")
)
