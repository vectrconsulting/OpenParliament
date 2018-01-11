const webpack = require('webpack')

module.exports = {
  entry: {
    dasboard: './src/main/javascript/dashboard/client.js',
    admin: './src/main/javascript/admin/client.js',
  },
  output: {
		filename: "[name].bundle.js",
		chunkFilename: "[id].chunk.js",
    path: __dirname + '/src/main/resources/app-ui/dist',
    publicPath: '/'
  },
  plugins: [
    new webpack.DefinePlugin({ // <-- key to reducing React's size
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin(), //minify everything
    new webpack.optimize.AggressiveMergingPlugin()//Merge chunks 
  ],
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
