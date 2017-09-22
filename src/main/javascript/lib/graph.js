import React, { Component } from 'react'
import _ from 'lodash'

const VIEWBOX = [1000, 500]

export const NPartiteGraph =  class NPartiteGraph extends Component {
  getCols () {
    return this.props.data.map((c, i) => <PartiteCol index={i} col={c} key={`${Math.random()}`} padding={10} height={this.props.height} />)
  }

  render () {
    return (
      <svg viewBox={`0 0 ${VIEWBOX[0]} ${VIEWBOX[1]}`} width="100%" height={this.props.height}>
        {this.getCols()}
      </svg>
    )
  }
}

class PartiteCol extends Component {
  constructor (props) {
    super(props)
    this.total = _.sum(props.col.values.map(v => _.sum(v)))
  }

  getFrom() {
    const blocks = []
    let spent = 0
    const normalizer = (this.props.height - this.props.col.values.length * (this.props.padding || 10))
    for (let i = 0; i < this.props.col.values.length; i++) {
      const blockHeight = _.sum(this.props.col.values[i])/this.total * normalizer
      blocks.push(<PartiteBlock
        data={this.props.col.values[i].map(v => ({value: v/this.total * normalizer, color: this.props.col.keys.from[i].color}))}
        y={spent} x={0}
      />)
      spent += blockHeight + (this.props.padding || 10)
    }
    return blocks
  }

  getTo() {
    const blocks = []
    let spent = 0
    const normalizer = (this.props.height - this.props.col.values.length * (this.props.padding || 10))
    for (let i = 0; i < this.props.col.values[0].length; i++) {
      const blockList = this.props.col.values.map(val => val[i])
      const blockHeight = _.sum(blockList)/this.total*normalizer
      blocks.push(<PartiteBlock
        data={this.props.col.values.map((v, j) => ({value: this.props.col.values[j][i]/this.total*normalizer, color: this.props.col.keys.from[j].color}))}
        y={spent} x={0}
      />)
      spent += blockHeight + (this.props.padding || 10)
    }
    return blocks
  }

  render () {
    return (
      <g transform={`translate(${this.props.index*350}, 0)`}>
        <g>
          {this.getFrom()}
        </g>
        <g transform="translate(300, 0)" >
          {this.getTo()}
        </g>
      </g>
    )
  }
}

class PartiteBlock extends Component {
  getRects() {
    let spent = 0
    return this.props.data.map(d => {
      const r = <rect x="0" y={spent} width="50" height={d.value} fill={d.color}/>
      spent += d.value
      return r
    })
  }

  render () {
    return (
      <g transform={`translate(${this.props.x||0}, ${this.props.y||0})`}>
        {this.getRects()}
      </g>
    )
  }
}
