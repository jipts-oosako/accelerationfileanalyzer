# build
## fileanalyzer
fileanalyzer は、Eclipse 4.7 Oxygen を使ってビルドできます。

### jarファイルの生成
Eclipse メニュー > 実行 > `Maven install`
target直下にjarファイルが生成されます。

## visualizer
visualizer は、Node.js(v6.11.2) を使ってビルドできます。

### nw、nw-builderのインストール
visualizer> `npm i nw --save-dev`
visualizer> `npm i nw-builder --save-dev`

### starting method
visualizer> `npm run dev`

### build
visualizer> `npm run build`
