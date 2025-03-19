const webpack = require('webpack');

module.exports = {
    plugins: [
        new webpack.EnvironmentPlugin({
            "FABX_VERSION": "0.0.0-LOCAL-SNAPSHOT"
        })
    ]
}
