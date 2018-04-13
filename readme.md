# accelerationfileanalyzer
AccelerationFileanalyzer helps efficient storage of large volume data by searching characteristic areas from the acceleration measurement file measured over a long time or extracting data in a specified time zone.
Also, by visualizing acceleration graphs for each node in the same time zone for the extracted acceleration data, visualization of these data is realized.

This software is released under the MIT License, see LICENSE.md.

# Build method
## fileanalyzer
fileanalyzer can build using Eclipse 4.7 Oxygen.

### Generation of the jar file
Eclipse menu > run > `Maven install`
Jar file is generated right under target.

## visualizer
Visualizer can build using Node.js(v6.11.2).

### installation of nw and nw-builder
visualizer> `npm i nw --save-dev`
visualizer> `npm i nw-builder --save-dev`

### starting method
visualizer> `npm run dev`

### build
visualizer> `npm run build`
