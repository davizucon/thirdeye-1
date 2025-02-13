/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
const path = require("path");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const { RetryChunkLoadPlugin } = require("webpack-retry-chunk-load-plugin");
const WebpackBar = require("webpackbar");
const BundleAnalyzerPlugin =
    require("webpack-bundle-analyzer").BundleAnalyzerPlugin;

const outputPath = path.join(__dirname, "dist");

module.exports = {
    // Production mode
    mode: "production",

    // Input configuration
    entry: path.join(__dirname, "src/app/index.tsx"),

    // Output configuration
    output: {
        path: outputPath,
        filename: "thirdeye-ui.[contenthash].js",
        chunkFilename: "[name].[contenthash].js",
        publicPath: "/", // Ensures bundle is served from absolute path as opposed to relative
    },

    // Loaders
    module: {
        rules: [
            // .ts and .tsx files to be handled by ts-loader
            {
                test: /\.(ts|tsx)$/,
                exclude: /node_modules/, // Just the source code
                use: [
                    {
                        loader: "ts-loader",
                        options: {
                            configFile: "tsconfig.json",
                            transpileOnly: false,
                        },
                    },
                ],
            },
            // .css and .scss files to be handled by sass-loader
            {
                test: /\.(css|scss)$/,
                use: ["style-loader", "css-loader", "sass-loader"],
            },
            // .svg files to be handled by @svgr/webpack
            {
                test: /\.svg$/,
                use: ["@svgr/webpack", "url-loader"],
            },
            // Font files to be handled by file-loader
            {
                test: /\.ttf$/,
                use: [
                    {
                        loader: "file-loader",
                        options: {
                            name: "[name].[ext]",
                            outputPath: "fonts/",
                        },
                    },
                ],
            },
            // images files to be handled by file-loader
            {
                test: /\.png$/,
                use: [
                    {
                        loader: "file-loader",
                        options: {
                            name: "[name].[ext]",
                            outputPath: "images/",
                        },
                    },
                ],
            },
        ],
    },

    // Module resolution
    resolve: {
        // File types to be handled
        extensions: [
            ".ts",
            ".tsx",
            ".js",
            ".css",
            ".scss",
            ".svg",
            ".ttf",
            ".png",
        ],
    },

    plugins: [
        // Clean webpack output directory
        new CleanWebpackPlugin({
            verbose: true,
        }),
        // Generate index.html from template
        new HtmlWebpackPlugin({
            template: path.join(__dirname, "src/public/index.html"),
            scriptLoading: "defer",
            hash: true,
        }),
        // Copy favicon, logo and manifest for index.html
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: path.join(__dirname, "src/public/favicon.ico"),
                    to: outputPath,
                },
                {
                    from: path.join(
                        __dirname,
                        "src/public/thirdeye-512x512.png"
                    ),
                    to: outputPath,
                },
                {
                    from: path.join(__dirname, "src/public/manifest.json"),
                    to: outputPath,
                },
            ],
        }),
        // Configure multiple attempts to load chunks
        new RetryChunkLoadPlugin({
            retryDelay: 500,
            maxRetries: 3,
        }),
        // Build progress bar
        new WebpackBar({
            name: "@startree-ui/thirdeye-ui [prod]",
            color: "#E37475",
        }),
        // Bundle analyzer
        new BundleAnalyzerPlugin({
            analyzerMode: "static",
            reportFilename: path.join(
                __dirname,
                "webpack/bundle-analyzer-report.html"
            ),
            openAnalyzer: false,
        }),
    ],

    // Source map
    devtool: "source-map",
};
