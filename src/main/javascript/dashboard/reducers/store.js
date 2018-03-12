import { createStore, combineReducers, applyMiddleware } from "redux";
import thunk from "redux-thunk";

import locale from "./locale";
import search from "./search";
import filter from "./filter";
import data from "./data";

export const store = createStore(
    combineReducers({ locale, search, filter, data }),
    applyMiddleware(thunk)
)
