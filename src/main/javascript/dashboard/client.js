import React from "react";
import { render } from "react-dom";
import { Provider } from "react-redux";

import { store } from "./reducers/store";
import { setLocale } from "./reducers/locale";
import { fetchPaths, fetchQuestions } from "./reducers/data";
import { setColumnFilters, setDateFilter } from "./reducers/filter";

import Page from "./components/opendata.page";

const search = window.location.href.split("?")[1] || "";
if (search !== "") {
  const items = Object.assign({}, ...search.split("&")
    .map(item => ({ [item.split("=")[0]]: (decodeURIComponent(item.split("=")[1])) }))
  );
  if (items.columns && items.date && items.lang) {
    store.dispatch(setLocale(items.lang, true));
    store.dispatch(setColumnFilters(JSON.parse(items.columns)));
    store.dispatch(setDateFilter(JSON.parse(items.date)));
  }
} else {
  store.dispatch(fetchPaths());
  store.dispatch(fetchQuestions());
}

render(
  <Provider store={store}>
    <Page />
  </Provider>,
  document.getElementById("app")
);
