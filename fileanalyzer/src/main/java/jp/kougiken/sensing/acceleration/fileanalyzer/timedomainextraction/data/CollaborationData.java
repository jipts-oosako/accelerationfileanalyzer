package jp.kougiken.sensing.acceleration.fileanalyzer.timedomainextraction.data;

import java.util.HashMap;
import java.util.Map;

import jp.kougiken.sensing.acceleration.common.Constants;
import jp.kougiken.sensing.acceleration.fileanalyzer.featurepointsearch.data.MinMaxData;

public class CollaborationData {
	/** 加速度計測ファイルID（＝ファイルのハッシュ値）*/
	private String measurefileid;
	/** 計測条件ID（※計測条件は別コレクションで保有）*/
	private String conditionid;
	/** 加速度計測ファイルのファイルパス*/
	private String filepath;
	/** ノード番号*/
	private String nodeno;
	/** 記録開始日時*/
	private String startdatetime;
	/** 記録時間（s）*/
	private long recordingtime;
	/** 記録行数 */
	private long recordcount;
	/** 軸毎の情報[0:X軸, 1:Y軸, 2:Z軸] */
	private Map<String, AxisData> axisData;
	// 「ピーク値PEEK（G）」「実効値RMS」等*/
	// スペクトル解析の結果（応答周波数）等*/

	/**
	 * コンストラクタ
	 */
	public CollaborationData(){
		this.axisData = new HashMap<String, AxisData>();
		this.axisData.put(Constants.AXIS_StrX, new AxisData());
		this.axisData.put(Constants.AXIS_StrY, new AxisData());
		this.axisData.put(Constants.AXIS_StrZ, new AxisData());
	}

	/**
	 * 加速度計測ファイルIDをセットする
	 * @param measurefileid 加速度計測ファイルの16進数表示のハッシュ値
	 */
	public void setMeasurefileid(String measurefileid) {
		this.measurefileid = measurefileid;
	}
	/**
	 * 計測条件IDをセットする（※計測条件は別コレクションで保有）
	 * @param conditionid 計測条件ID
	 */
	public void setConditionid(String conditionid) {
		this.conditionid = conditionid;
	}
	/**
	 * 加速度計測ファイルのファイルパスをセットする
	 * @param filepath 基準フォルダ以下のフォルダ名とファイル名を組み合わせた文字列
	 */
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	/**
	 * ノード番号をセットする
	 * @param nodeno ノード番号を示す文字列
	 */
	public void setNodeno(String nodeno) {
		this.nodeno = nodeno;
	}
	/**
	 * 記録開始日時をセットする
	 * @param startdatetime 記録開始日時をISO_LOCAL_DATE_TIMEで設定
	 */
	public void setStartdatetime(String startdatetime) {
		this.startdatetime = startdatetime;
	}

	/**
	 * 記録時間をセットする
	 * @param recordingtime  記録時間（s）
	 */
	public void setRecordingtime(long recordingtime) {
		this.recordingtime = recordingtime;
	}
	/**
	 * 記録行数をセットする
	 * @param recordcount 記録行数
	 */
	public void setRecordcount(long recordcount) {
		this.recordcount = recordcount;
	}

	/**
	 * 軸毎の情報をセットする
	 * @param localAverages 全計測データの軸毎の平均値（G）[0:X軸, 1:Y軸, 2:Z軸]
	 * @param averages 軸毎の平均値（G）[0:X軸, 1:Y軸, 2:Z軸]
	 * @param mmvalues 軸毎の最小値、最大値（G）[0:X軸, 1:Y軸, 2:Z軸]
	 * */
	public void setAxisData(double[] localAverages, double[] averages, MinMaxData[] mmvalues) {
		AxisData axisX = this.axisData.get(Constants.AXIS_StrX);
		AxisData axisY = this.axisData.get(Constants.AXIS_StrY);
		AxisData axisZ = this.axisData.get(Constants.AXIS_StrZ);

		axisX.setGlobalaverage(localAverages[Constants.AXIS_X]);
		axisY.setGlobalaverage(localAverages[Constants.AXIS_Y]);
		axisZ.setGlobalaverage(localAverages[Constants.AXIS_Z]);

		axisX.setAverage(averages[Constants.AXIS_X]);
		axisY.setAverage(averages[Constants.AXIS_Y]);
		axisZ.setAverage(averages[Constants.AXIS_Z]);

		axisX.setMinvalue(mmvalues[Constants.AXIS_X].getMin());
		axisY.setMinvalue(mmvalues[Constants.AXIS_Y].getMin());
		axisZ.setMinvalue(mmvalues[Constants.AXIS_Z].getMin());

		axisX.setMaxvalue(mmvalues[Constants.AXIS_X].getMax());
		axisY.setMaxvalue(mmvalues[Constants.AXIS_Y].getMax());
		axisZ.setMaxvalue(mmvalues[Constants.AXIS_Z].getMax());
	}

	/**
	 * 加速度計測ファイルIDを取得する
	 * @return ファイルのハッシュ値
	 */
	public String getMeasurefileid() {
		return measurefileid;
	}
	/**
	 * 計測条件IDを取得する（※計測条件は別コレクションで保有）
	 * @return 計測条件IDを示す文字列
	 */
	public String getConditionid() {
		return conditionid;
	}
	/**
	 * 加速度計測ファイルのファイルパスを取得する
	 * @return 基準フォルダ以下のフォルダ名とファイル名を組み合わせた文字列
	 */
	public String getFilepath() {
		return filepath;
	}
	/**
	 * ノード番号を取得する
	 * @return ノード番号を示す文字列
	 */
	public String getNodeno() {
		return nodeno;
	}
	/**
	 * 記録開始日時を取得する
	 * @return 記録開始日時を示すISO_LOCAL_DATE_TIMEの書式文字列
	 */
	public String getStartdatetime() {
		return startdatetime;
	}
	/**
	 * 記録時間を取得する
	 * @return 記録時間（s）
	 */
	public long getRecordingtime() {
		return recordingtime;
	}
	/**
	 * 記録行数を取得する
	 * @return 記録行数
	 */
	public long getRecordcount() {
		return recordcount;
	}
	/**
	 * 軸毎の情報を取得する
	 * @return AxisData[0:X軸, 1:Y軸, 2:Z軸]
	 */
	public AxisData[] getAxisData() {
		AxisData[] ret = {	this.axisData.get(Constants.AXIS_StrX),
							this.axisData.get(Constants.AXIS_StrY),
							this.axisData.get(Constants.AXIS_StrZ)};
		return ret;
	}
}
