package jp.kougiken.sensing.acceleration.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 計測データの1行分の情報
 */
public class MeasurementData {

	private static final Logger logger = LogManager.getLogger();

	/** ノードNo */
	private String nodeNo;
	/** 計測開始からの経過時間(s) */
	private double elapsedTime;
	/** 計測値(X軸)(g) */
	private double measureX;
	/** 計測値(Y軸)(g) */
	private double measureY;
	/** 計測値(Z軸)(g) */
	private double measureZ;
	/** 計測値(Z軸)以降の文字列 */
	private String etcetera;
	/** 計測データ(1行分) */
	private String originalLineData;

	/**
	 * コンストラクタ
	 */
	protected MeasurementData(){

	}

	/** ノードNo */
	public String getNodeNo(){
		return nodeNo;
	}

	/** 計測開始からの経過時間(s) */
	public double getElapsedTime() {
		return elapsedTime;
	}

	/** 計測値(X軸)(g) */
	public double getMeasureX() {
		return measureX;
	}

	/** 計測値(Y軸)(g) */
	public double getMeasureY() {
		return measureY;
	}

	/** 計測値(Z軸)(g) */
	public double getMeasureZ() {
		return measureZ;
	}

	/**
	 * 計測値(軸Index指定)(g)
	 * @param axisIndex X軸:0, Y軸:1、Z軸:2
	 */
	public double getMeasure(int axisIndex) {
		double ret = 0.0;
		switch(axisIndex){
		case 0:	ret = measureX;	break;
		case 1:	ret = measureY;	break;
		case 2:	ret = measureZ;	break;
		}
		return ret;
	}

	/** 計測データ1行分のオリジナルデータ */
	public String getOriginalLineData() {
		return originalLineData;
	}

	/**
	 * ゼロ補正値を適用する
	 * @param 指定軸における、ゼロ点からのずれの平均値(G)[0:X軸, 1:Y軸, 2:Z軸]
	 */
	public void applyingZerocorrections(double[] zerocorrections, String separator) {
		measureX = measureX - zerocorrections[Constants.AXIS_X];
		measureY = measureY - zerocorrections[Constants.AXIS_Y];
		measureZ = measureZ - zerocorrections[Constants.AXIS_Z];
	}

	/**
	 * 1行分の計測データを取得する
	 * @param separator 区切り文字
	 */
	public String getLineData(String separator) {
		String ret = "";
		ret = String.format("%s%s%.2f%s%.6f%s%.6f%s%.6f", nodeNo, separator, elapsedTime, separator, measureX
				                                                                        , separator, measureY
				                                                                        , separator, measureZ);
		if (!etcetera.isEmpty()){
			ret += String.format("%s", etcetera);
		}
		return ret;
	}

	/**
	 * 計測データの1行をパースし、オブジェクトを生成する
     * @param  line 計測データの1行
     * @param  separator 区切り文字
	 * @return このオブジェクト
	 */
	public static MeasurementData parse(String line, String separator) {
		MeasurementData obj = null;

		try{
			if (line!=null && !line.isEmpty()){
				String[] items = line.split(separator, 0);

				obj = new MeasurementData();
				obj.nodeNo = items[0];						// NodeNo
				obj.elapsedTime = Double.valueOf(items[1]);	// 経過時間(0.0～)
				obj.measureX = Double.valueOf(items[2]);	// 計測値(X軸)(g)
				obj.measureY = Double.valueOf(items[3]);	// 計測値(Y軸)(g)
				obj.measureZ = Double.valueOf(items[4]);	// 計測値(Z軸)(g)
				obj.etcetera = "";
				if (items.length > 5){
					for (int idx = 5; idx < items.length; idx++){
						obj.etcetera += String.format("%s%s", separator, items[idx]);
					}
				}
				obj.originalLineData = line;				// オリジナル計測データ
			}
		}catch (Exception e){
			logger.error(String.format(AppMessages.ERROR.get("ERR060"), line, e));
			obj = null;
		}

		return obj;
	}

	/**
	 * 計測データの有効な先頭行をパースし、オブジェクトを生成する
	 */
	public static MeasurementData getFirstData(Path datafile, String separator) throws IOException{
		MeasurementData obj = null;
		try(Stream<String> lines =  Files.lines(datafile)){
			int limit = 10;
			Iterator<String> iterator = lines.iterator();
			while(iterator.hasNext() && limit > 0) {
			    String line = (String)iterator.next();
				if (line != null){
					obj = parse(line, separator);
					if (obj != null){
						break;
					}
				}
				limit--;
	    	}
	    }
		return obj;
	}
	/**
	 * 計測データの有効な末端行をパースし、オブジェクトを生成する
	 */
	public static MeasurementData getLastData(Path datafile, String separator) throws IOException{
		MeasurementData obj = null;
		try (ReversedLinesFileReader reader = new ReversedLinesFileReader(datafile.toFile(), Charset.forName("UTF-8") )) {
			int limit = 10;
			String line = reader.readLine();
			while (line != null && limit > 0){
				obj = parse(line, separator);
				if (obj != null){
					break;
				}

				line = reader.readLine();
				limit--;
	    	}
	    }
		return obj;
	}
}
