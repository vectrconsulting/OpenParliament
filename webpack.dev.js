const webpack = require('webpack')

module.exports = {
  entry: {
    dasboard: [
      'babel-polyfill',
      './src/main/javascript/dashboard/client.js'
    ],
  },
  output: {
    filename: "[name].bundle.js",
    chunkFilename: "[id].chunk.js",
    path: __dirname + '/src/main/resources/app-ui/dist',
    publicPath: '/'
  },
  module: {
    loaders: [{
      test: /\.css$/,
      loader: 'style!css'
    }, {
      test: /\.js$/,
      loader: 'babel-loader',
      exclude: /(node_modules)/
    }, {
      test: /\.json$/,
      loader: 'json-loader'
    }],
  }
}
