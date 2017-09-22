import React from 'react'
import { render } from 'react-dom'
import { createStore } from 'redux'

import { NPartiteGraph } from './lib/graph'
import { Provider } from 'react-redux'
import { Page } from './app/Page'
import {storeManager} from './app/storeManager'



render(
  <Provider store={createStore(storeManager)}>
    <Page />
  </Provider>,
  document.getElementById("app")
)
