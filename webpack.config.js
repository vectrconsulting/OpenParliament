const webpack = require('webpack')

module.exports = {
  entry: './src/main/javascript/client.js',
  output: {
    filename: "bundle.js",
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
    }]
  }
}
