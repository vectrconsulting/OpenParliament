import React, { Component } from 'react'
import { connect } from 'react-redux'
import Joyride from 'react-joyride'

import { tour } from '../tour/Welcome'

@connect(state => ({ current_language: state.locale.current_language.translation, running: state.tour.running }))
export const JoyRideWrapper = class JoyRideWrapper extends Component {
    callback(data) {
        if (data === undefined) return;
        switch (data.type) {
            case 'finished':{
                this.props.dispatch({ type: 'STOP_TOUR' });
                this.joyride.reset(false);
                break;
            }
            default:
                break;
        }
    }
    render() {
        const steps = tour(this.props.current_language.tour_welcome)
        const buttons = this.props.current_language.tour_buttons
        return (
            <Joyride
                 ref={c => {this.joyride=c}}
                 type="continuous"
                 steps={steps}
                 stepIndex={0}
                 run={this.props.running}
                 autoStart={true}
                 locale={buttons}
                 showSkipButton={true}
                 callback={this.callback.bind(this)}
                 showStepsProgress={true}
             />
        );
    }
}