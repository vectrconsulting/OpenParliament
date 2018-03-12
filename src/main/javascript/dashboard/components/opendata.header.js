import React, { Component } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import copy from "copy-to-clipboard";
import { ToastContainer, toast } from "react-toastify";


import { setLocale } from '../reducers/locale';


const share_square = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAq1BMVEUAAAD////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Nr6iZAAAAOHRSTlMAAQIDBAUGCAoLDg8QERITFCMoKis0OENPUFZbXF5iZGlwcXR3houPmp6isM/R0+Lm6Onr7fP3+xd6RYoAAACSSURBVBgZBcGJIgIBGAbA2dLfiih37kVuohXf+z+ZGQA4AExmswbadNC+95+XAzR7yT1eFgxOX/skSR74HdtfJUmSJGdSzSpJkiT5mUodJfl6vDnvkn5K6iJZNNjOehepqzyD0ccOpI5zAmghtfU3B2wKKbeHgBS+W8MJIEN0y7aqqqrG12/Q3K2TJMnmaQQAAP+/3RQal91zbgAAAABJRU5ErkJggg=="

export class Header extends Component {

    switchLang(lang) {
        this.props.setLocale(lang);
    }

    shareFilters() {
        const url = window.location.href.split('?')[0] + this.props.shareString;
        if (copy(url)) toast.success(this.props.copySucces);
        else toast.error(this.props.copyError);
    }

    render() {
        return (
            <div className="opendata-header">
                <nav className="navbar navbar-expand-lg fixed-top navbar-dark bg-primary" >
                    <div className="row justify-content-center" style={{ width: "100%" }}>
                        <div className="col-lg-11 col-xl-10">
                            <div className="row" style={{ width: "100%" }}>
                                <div className="col-sm-10">
                                    <a className="navbar-brand" href="#">{this.props.brand}</a>
                                </div>
                                <div className="col-sm-2">
                                    <div style={{ float: "right" }}>
                                        <button type="button" className="btn btn-secondary bg-primary" onClick={() => this.switchLang("nl")}>NL</button>
                                        <button type="button" className="btn btn-secondary bg-primary" onClick={() => this.switchLang("fr")}>FR</button>
                                        <button type="button" className="btn btn-secondary bg-primary" style={{ marginLeft: 5 }} onClick={() => this.shareFilters()}>
                                            <center>
                                                <img src={share_square} />
                                            </center>
                                        </button>
                                        <ToastContainer autoClose={3000} />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </ nav>
            </div>
        );
    }
}

export default connect(
    state => ({
        current_language_code: state.locale.code,
        brand: state.locale.translation.header,
        shareString: `?columns=${JSON.stringify(state.filter.columns)}&date=${JSON.stringify(state.filter.dates)}&lang=${state.locale.code}`,
        copySucces: state.locale.translation.share.toast_succes,
        copyError: state.locale.translation.share.toast_error,
    }),
    dispatch => bindActionCreators({ setLocale }, dispatch)
)(Header)