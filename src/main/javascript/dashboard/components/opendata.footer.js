import React, { Component } from 'react';
import { connect } from 'react-redux';

export class Footer extends Component {
    render() {
        return (
            <footer className="opendata-footer footer">
                <nav className="navbar bottom navbar-expand-lg navbar-dark bg-primary">
                    <div>
                        <span style={{ color: "#fff" }}>{this.props.text}:</span>
                        <a href="http://www.vectr.consulting"><img src="logos/VectrConsulting.svg" height={30} /></a>
                        <a href="http://www.vereenvoudiging.be/" ><img src="logos/DAV.png" height={30} /></a>
                        <a href="http://www.dekamer.be/" ><img src="logos/dekamer.jpg" height={30} /></a>
                        <a href="https://neo4j.com/" ><img src="logos/Neo4j.png" height={30} /></a>
                    </div>
                </nav>
            </footer>
        );
    }
}

export default connect(
    state => ({ text: state.locale.translation.footer })
)(Footer)