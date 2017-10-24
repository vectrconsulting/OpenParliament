import React, { Component } from 'react';


export const Provider = class Provider extends Component {
    getChildContext() {
        return {
            store: this.props.store
        };
    }

    render() {
        return history.props.children;
    }
}

Provider.childContextTypes = {
    store: React.PropTypes.object
};

export const storeManager = (state = {}, action) => {
    //Object.assign({}, state, changes)
    switch (action.type){
        default:
            return state;
    }
        
}