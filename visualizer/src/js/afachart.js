/**
Wave Chart Visualizer for Acceleration File Analyzer

Copyright (c) 2016 Syouhei Oosako

This software is released under the MIT License.
http://opensource.org/licenses/mit-license.php
*/
var afachart = {};
(function(global) {
    "use strict";

	var margin = {top: 20, right: 30, bottom: 30, left: 80};
	var width = 700 - margin.left - margin.right;
	var height = 300 - margin.top - margin.bottom;
	var noselectString = "---";
	var READMAXROW = (typeof process.env.READMAXROW === "undefined")? 60000 : process.env.READMAXROW;
	var SEPARATOR = (typeof process.env.FILE_SEPARATOR === "undefined")? " " : process.env.FILE_SEPARATOR;
	
	var fs = require('fs');
	var path = require('path');
	var gui = require('nw.gui');

	var datainitdir;
	if (gui.App.argv.length > 0){
		datainitdir = gui.App.argv[0];
	}else{
		datainitdir = path.resolve(path.join(path.dirname(process.cwd()), "data"));
	}
	var datafilefolder;
	
	var dropdownFolders;
	var dropdownContent;
	var dropdownFiles;
	var dropdownAxis;
	var inputBaseFolders;
	var inputInfomation;
	var inputAxisyValue;
	
	var svglist;
	
	// 初期処理
	afachart.main = function(){
		dropdownFolders = document.getElementById("dropdownFoldersID");
		dropdownContent = document.getElementById("dropdownContentID");
		dropdownFiles = document.getElementById("dropdownFilesID");
		dropdownAxis = document.getElementById("dropdownAxisID");
		inputBaseFolders = document.getElementById("inputBaseFoldersID");
		inputInfomation = document.getElementById("inputInfomationID");
		inputAxisyValue = document.getElementById("inputAxisyValueID");
		svglist = document.getElementById("svglistID");
		
		datafilefolder = datainitdir;
		inputBaseFolders.value = datafilefolder;
		inputInfomation.value="";

		initview();
	}
	
	// 選択フォルダ変更イベント
	afachart.folderlistChangeEvent = function(){
		var selectFileName = null;
		if (dropdownFiles.selectedIndex>=0){
			selectFileName = dropdownFiles.options[dropdownFiles.selectedIndex].value;
		}
		removeChildren(svglist);
		var searchfilelist = getfilelist(dropdownFolders.options[dropdownFolders.selectedIndex].value);
		createdropdownFiles(searchfilelist, selectFileName);
		onDraw();
	}
	
	// 選択ファイル変更イベント
	afachart.filelistChangeEvent = function(){
		removeChildren(svglist);
		onDraw();
	}

	// 描画パラメータ変更イベント
	afachart.viewparamaterChangeEvent = function(removedl){
		if (removedl) removeChildren(svglist);
		
		// 表示最大値が負値の場合、0に変更
		if (inputAxisyValue.value < 0) inputAxisyValue.value = 0;
		
		// 全ファイル描画か否かにより、ファイル選択リスト及び描画軸の有効／無効を切り換える
		d3.selectAll(".dropdownFilesClass").style("display", (isAllfileDraw())? "none" : "");
		d3.selectAll(".dropdownAxisClass").style("display", (isAllfileDraw())? "" : "none");
		onDraw();
	}

	// ベースフォルダ変更イベント
	afachart.changeBaseFolder = function(){
		removeChildren(svglist);
		inputInfomation.value="";
		var fullpath = inputBaseFolders.value;
	    fs.stat(fullpath, function (err, stats) {
	        if (err) {
	            console.error(err);
				inputInfomation.value="有効なフォルダが指定されていません.["+fullpath+"]";
				inputBaseFolders.value = datafilefolder;
	        }
	        else if (stats.isDirectory()) {
				datafilefolder = fullpath;
	            initview();
	        }
	    });

	}

	// 画面初期化処理
	function initview() {
		removeChildren(svglist);
		
		// 全ファイル描画か否かにより、ファイル選択リスト及び描画軸の有効／無効を切り換える
		d3.selectAll(".dropdownFilesClass").style("display", (isAllfileDraw())? "none" : "");
		d3.selectAll(".dropdownAxisClass").style("display", (isAllfileDraw())? "" : "none");
		
		// 選択リストを作成する
		var searchfilelist = getfilelist("");
		if (searchfilelist==null) return;
		createdropdownFolders(searchfilelist);
		createdropdownFiles(searchfilelist, null);

		// 選択ファイル項目が存在しない場合、有効なフォルダを選択する
		if (dropdownFiles.options.length==1){// "---"のみの場合
			if (dropdownFolders.options.length > 1){
				dropdownFolders.selectedIndex = "1";
				afachart.folderlistChangeEvent();
			}
		}else{
			onDraw();
		}
	}

	// データファイル格納フォルダからファイル一覧を取得する
	function getfilelist(subfolder) {
		try{
			var searchfolder = (subfolder=="") ? datafilefolder : path.join(datafilefolder, subfolder);
			return fs.readdirSync(searchfolder).sort(function(file1, file2) {return file1 < file2 ? -1 : 1;})
		}
		catch(e){
			console.error(e);
			inputInfomation.value="有効なフォルダが指定されていません.["+searchfolder+"]";
			inputBaseFolders.value = datafilefolder;
		}
		return;
	}
	
	// ディレクトリか否か判定する
	function isDir(filepath) {  
	  return fs.existsSync(filepath) && fs.statSync(filepath).isDirectory();
	}
	
	// 全ファイルを描画するか判定する
	function isAllfileDraw() {
		return (dropdownContent.options[dropdownContent.selectedIndex].value == "allfile");
	}

	// 指定要素の子要素を全て削除
	function removeChildren(item)
	{
		if (item.hasChildNodes()) {
			while (item.childNodes.length > 0) {
				item.removeChild(item.firstChild)
			}
		}
	}
	
	// フォルダの選択リストを作成する
	function createdropdownFolders(searchfilelist){
		removeChildren(dropdownFolders);
		dropdownFolders.options.add(new Option(noselectString,""));
		
		if (searchfilelist==null) return;
		for(var i=0;i<searchfilelist.length;i++){
			var file = searchfilelist[i];
			if(isDir(path.join(datafilefolder, file))){
				dropdownFolders.options.add(new Option(file,file));
			}
		}
		dropdownFolders.selectedIndex = "0";
	}

	// ファイルの選択リストを作成する
	function createdropdownFiles(searchfilelist, selectFileName){
		removeChildren(dropdownFiles);
		dropdownFiles.options.add(new Option(noselectString,""));
		
		if (searchfilelist==null) return;
		for(var i=0;i<searchfilelist.length;i++){
			var file = searchfilelist[i];
			if(!isDir(path.join(datafilefolder, file)) && file.match(/csv$/i)) {	// CSVファイルに限定
				dropdownFiles.options.add(new Option(file,file));
			}
		}
		
		if(dropdownFiles.options.length > 1){
			dropdownFiles.selectedIndex = "1";
			if (selectFileName!=null){
				for( var i = 0; i < dropdownFiles.length; i++ ){
					if( dropdownFiles.options[i].value == selectFileName){
						dropdownFiles.selectedIndex = i;
						break;
					}
				}
			}
		}
	}
	
	// ファイル情報の描画
	function onDraw() {
		d3.selectAll(".svgclass").remove();
		
		if (isAllfileDraw()){
			// 選択中のフォルダ内の全ファイル
			var selectfolder = dropdownFolders.options[dropdownFolders.selectedIndex].value;
			var searchfilelist = getfilelist(selectfolder);
			
			if (searchfilelist==null) return;
			for(var i=0;i<searchfilelist.length;i++){
				var selectfile = searchfilelist[i];
				if (selectfile.match(/csv$/i)) {	// CSVファイルに限定する
					var fullpath = "";
					if (selectfile==""){
						// nothing
					}else if (selectfolder==""){
						fullpath = path.join(datafilefolder, selectfile);
					}else{
						fullpath = path.join(datafilefolder, selectfolder, selectfile);
					}
			        var axis = dropdownAxis.options[dropdownAxis.selectedIndex].value;
					drawfile(fullpath, axis);
				}
			}
		}else{
			// 選択中のファイル
			var selectfolder = dropdownFolders.options[dropdownFolders.selectedIndex].value;
			var selectfile = dropdownFiles.options[dropdownFiles.selectedIndex].value;
			var fullpath = "";
			if (selectfile==""){
				// nothing
			}else if (selectfolder==""){
				fullpath = path.join(datafilefolder, selectfile);
			}else{
				fullpath = path.join(datafilefolder, selectfolder, selectfile);
			}
			drawfile(fullpath, "x");
			drawfile(fullpath, "y");
			drawfile(fullpath, "z");
		}
	}
	
	// ファイル情報の描画(ファイルのフルパス指定)
	function drawfile(fullpath, axis) {
		if (fullpath=="") return;
		
		// id要素を順番に作成
		var id = "ID"+path.basename(fullpath, '.csv');
		var dd = null;
		if (d3.selectAll("#"+id).empty()){
			var dt = d3.select("#svglistID").append("dt").attr("class", "svgcanvas").text(path.basename(fullpath));
			dt.on("click", function() {$(this).next().animate({
					opacity: 1, left: "+=50", height: "toggle"
				}, 500);
			});
		    dd = d3.select("#svglistID").append("dd").attr("class", "svgcanvas").attr("id", id);
		}else{
		    dd = d3.select("#"+id);
		}
	    var svg = dd.append("svg")
			.attr("class", "svgclass")
			.attr("width", width + margin.left + margin.right)
			.attr("height", height + margin.top + margin.bottom)
			.append("g")
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	    fs.stat(fullpath, function (err, stats) {
	        if (err) {
	            console.error(err);
	        }
	        else if (stats.isFile()) {
	            drawGraph(fullpath, axis, svg);
	        }
	    });
	}
	
	// グラフ描画描画
	function drawGraph(fullpath, axis, svg) {
	    console.log(fullpath);
	    d3.text(fullpath, function(error, text) {
	        var data = d3.dsv(SEPARATOR, "text/plain").parseRows(text, function(d, i) {
	        		if (i > READMAXROW) return;
	                return { n: d[0], t: d[1], x: d[2], y: d[3], z: d[4] }; // 1行分パースする
	            });

	        var xMin = d3.min(data,function(d){ return +d["t"]; });
	        var xMax = d3.max(data,function(d){ return +d["t"]; });

			var input_yvalue = inputAxisyValue.value;
	        var yMin = -1.0*input_yvalue;
	        var yMax = input_yvalue;
			if (input_yvalue == 0){
		        // yScaleのために、closeの最大値を出す
		        yMin = d3.min(data,function(d){ return +d[axis]; });
		        yMax = d3.max(data,function(d){ return +d[axis]; });
		        // スケール調整
		        var delta = 0.1;
		        yMax = yMax + (yMax-yMin) * delta;
		        yMin = yMin - (yMax-yMin) * delta;
		    }
		    
	        // xScaleには、データレコードの数を最大値に使う
	        var xScale = d3.scale.linear()
	                        .domain([xMin,xMax])
	                        .range([0,width]);

	        var yScale = d3.scale.linear()
	                        .domain([yMin,yMax])
	                        .range([height,0]);

	        var xAxis = d3.svg.axis()
	                        .scale(xScale)
	                        .ticks(6)
	                        .orient("bottom")
	                        .tickSize(6, -height);

	        var yAxis = d3.svg.axis()
	                        .ticks(5)
	                        .scale(yScale)
	                        .orient("left")
	                        .tickSize(6, -width);

	        var line = d3.svg.line()
	            .x( function(d){ return xScale(d["t"]); })
	            .y( function(d){ return yScale(d[axis]); })
	            .interpolate("linear"); // 線の形を決める

	        // line表示
	        svg.append("path")
	                .datum(data)
	                .attr("class", "line")
	                .attr("d", line); // 上で作ったlineを入れて、ラインpathを作る
	/*
	        svg.selectAll("circle")
	            .data(data)
	            .enter()
	                .append("circle")
	                .attr("r",0)
	                .attr("fill", "steelBlue")
	                .attr("cx", function(d){ return xScale(d["t"]); })
	                .attr("cy", function(d){ return yScale(d[axis]); });
	*/
	        svg.append("g")
	            .attr("class", "y axis")
	            .call(yAxis)
	            .append("text")
	                .attr("y", -10)
	                .attr("x",10)
	                .style("text-anchor", "end")
	                .text("(G)");

	        svg.append("g")
	            .attr("class", "x axis")
	            .attr("transform", "translate(0," + height + ")")
	            .call(xAxis)
	            .append("text")
	                .attr("y", 20)
	                .attr("x",width+15)
	                .style("text-anchor", "end")
	                .text("(s)");

	        svg.append("g")
	            .attr("class", "title")
	            .append("text")
	                .attr("y", 0)
	                .attr("x", width/2)
	                .style("text-anchor", "middle")
	                .text(path.basename(fullpath)+" ["+axis+"軸]");
	    })
	}
/*
	$(function(){
		var mousewheelevent = 'onwheel' in document ? 'wheel' : 'onmousewheel' in document ? 'mousewheel' : 'DOMMouseScroll';
		$(document).on(mousewheelevent,"#dropdownFoldersID", function(e){
			e.preventDefault();
			var delta = e.originalEvent.deltaY ? -(e.originalEvent.deltaY) : e.originalEvent.wheelDelta ? e.originalEvent.wheelDelta : -(e.originalEvent.detail);
			if (delta < 0){
				dropdownFolders.selectedIndex = Math.min(dropdownFolders.selectedIndex+1, dropdownFolders.options.length-1);
			} else {
				dropdownFolders.selectedIndex = Math.max(dropdownFolders.selectedIndex-1, 0);
			}
			afachart.folderlistChangeEvent();
		});
	});
*/

})((this || 0).self || global);