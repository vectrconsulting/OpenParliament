import React from 'react'
import { render } from 'react-dom'
import { createStore, combineReducers } from 'redux'
import { Provider } from 'react-redux'
import request from 'superagent'
import 'babel-polyfill' //ie11 support

import { store } from './reducers/store'
import { Page } from './views/Page'

render(
  <Provider store={store}>
    <Page />
  </Provider>,
  document.getElementById("app")
)
