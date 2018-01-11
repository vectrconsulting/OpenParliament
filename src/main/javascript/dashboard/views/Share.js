import React, { Component } from 'react'
import { Button, Glyphicon } from 'react-bootstrap'
import { connect } from 'react-redux'
import copy from 'copy-to-clipboard'
import { notify } from 'react-notify-toast'
import ReactTooltip from 'react-tooltip'


@connect(state => ({
    url_params: `?columns=${JSON.stringify(state.filter.column)}&date=${JSON.stringify(state.filter.date)}&lang=${state.locale.current_language.code}`,
    share: state.locale.current_language.translation.share.share,
    bookmark: state.locale.current_language.translation.share.bookmark,
    toast_succes: state.locale.current_language.translation.share.toast_succes,
    toast_error: state.locale.current_language.translation.share.toast_error,
}))
export const Share = class Share extends Component {

    bookmark() {
        const base_url = window.location.href.split('?')[0];
        const win = window.open(base_url+ this.props.url_params);
        win.focus();
    }

    share() {
        const base_url = window.location.href.split('?')[0];
        if (copy(base_url + this.props.url_params)) {
            notify.show(this.props.toast_succes, "succes", 3000);
        } else {
            notify.show(this.props.toast_error, "error", 3000);
        }
    }

    render() {
        return (
            <div style={{ textAlign: 'right' }}>
                <Button bsStyle="link" onClick={this.bookmark.bind(this)} data-tip={this.props.bookmark}><Glyphicon glyph="share" /></Button>
                <Button bsStyle="link" onClick={this.share.bind(this)} data-tip={this.props.share}><Glyphicon glyph="copy" /></Button>
                <ReactTooltip />
            </div>
        );
    }
}